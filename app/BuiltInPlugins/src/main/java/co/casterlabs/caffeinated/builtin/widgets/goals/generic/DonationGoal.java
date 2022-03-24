package co.casterlabs.caffeinated.builtin.widgets.goals.generic;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.DonationEvent;
import co.casterlabs.koi.api.types.events.DonationEvent.Donation;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class DonationGoal extends GenericGoal {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.donation_goal")
        .withIcon("dollar-sign")
        .withCategory(WidgetDetailsCategory.GOALS)
        .withFriendlyName("Donation Goal");

    private double donationTotal = 0;

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);

        this.onSettingsUpdate();

        this.update(this.donationTotal);
    }

    private void save() {
        this.settings()
            .set("goal.value", this.donationTotal);
    }

    @Override
    protected void onSettingsUpdate() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();
        this.setSettingsLayout(layout, true); // Preserve

        // If this fails then we don't care.
        try {
            this.donationTotal = this.settings().getNumber("goal.value").doubleValue();
            this.update(this.donationTotal);
        } catch (Exception ignored) {}
    }

    @Override
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = super.generateSettingsLayout();

        layout.addSection(
            new WidgetSettingsSection("money", "Money")
                .addItem(WidgetSettingsItem.asCurrency("currency", "Currency", "USD", false))
        );

        return layout;
    }

    @KoiEventHandler
    public void onDonation(@Nullable DonationEvent e) {
        new AsyncTask(() -> {
            double total = 0;

            for (Donation d : e.getDonations()) {
                try {
                    Double convertedAmount = Currencies.convertCurrency(d.getAmount(), d.getCurrency(), Currencies.baseCurrency).await();

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
