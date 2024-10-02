package co.casterlabs.caffeinated.builtin.widgets.goals;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.rich.Donation;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class DonationGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.donation_goal")
        .withIcon("banknotes")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Donation Goal")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.DONATION_ALERT);

    private double donationTotal = 0;
    private String lastCurrency = null;

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);
    }

    private void save() {
        Currencies.convertCurrency(
            this.donationTotal,
            Currencies.baseCurrency,
            this.settings().getString("money.currency", Currencies.baseCurrency)
        ).then((amount) -> {
            this.settings()
                .set("goal.value", amount);
        });
    }

    @Override
    protected void onSettingsUpdate() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();
        this.setSettingsLayout(layout); // Preserve

        String currency = this.settings().getString("money.currency", Currencies.baseCurrency);

        if ((this.lastCurrency == null) || this.lastCurrency.equals(currency)) {
            // If this fails then we don't care.
            try {
                double currencyValue = this.settings().getNumber("goal.value").doubleValue();
                this.lastCurrency = currency;

                Currencies.convertCurrency(
                    currencyValue,
                    currency,
                    Currencies.baseCurrency
                ).then((amount) -> {
                    this.donationTotal = amount;
                    this.update(this.donationTotal);
                });
            } catch (Exception ignored) {}
        } else {
            this.lastCurrency = currency;
            this.save();
        }
    }

    @Override
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = super.generateSettingsLayout();

        layout.addSection(
            new WidgetSettingsSection("money", "Money")
                .addItem(WidgetSettingsItem.asCurrency("currency", "Currency", Currencies.baseCurrency, false))
        );

        return layout;
    }

    @KoiEventHandler
    public void onDonation(@Nullable RichMessageEvent e) {
        if (e.donations.isEmpty()) return;

        AsyncTask.create(() -> {
            double total = 0;

            for (Donation d : e.donations) {
                try {
                    Double convertedAmount = Currencies.convertCurrency(d.amount * d.count, d.currency, Currencies.baseCurrency).await();

                    total += convertedAmount;
                } catch (Throwable t) {
                    FastLogger.logException(t);
                }
            }

            this.donationTotal += total;

            this.save();
            this.update(this.donationTotal);
        });
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

    @Override
    protected boolean enableValueSetting() {
        return true;
    }

}
