package co.casterlabs.caffeinated.builtin.widgets.labels;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class DonationTotalLabel extends GenericLabel {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.donation_total_label")
        .withIcon("currency-dollar")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Donation Total Label")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.DONATION_ALERT);

    private String currHtml = "";

    private double donationTotal = 0;

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);

        // If this fails then we don't care.
        try {
            this.donationTotal = this.settings().getNumber("donation_total").doubleValue();
        } catch (Exception ignored) {}

        this.updateText();
    }

    @Override
    protected List<WidgetSettingsButton> getButtons() {
        return Arrays.asList(
            new WidgetSettingsButton("reset")
                .withIcon("no-symbol")
                .withIconTitle("Reset Counter")
                .withText("Reset Counter")
                .withOnClick(() -> {
                    this.donationTotal = 0;

                    this.save();
                    this.updateText();
                })
        );
    }

    private void save() {
        this.settings()
            .set("donation_total", this.donationTotal);
    }

    @Override
    protected void onSettingsUpdate() {
        this.updateText();
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
            this.updateText();
        });
    }

    @SneakyThrows
    private void updateText() {
        String html = Currencies.convertAndFormatCurrency(
            this.donationTotal,
            Currencies.baseCurrency,
            this.settings().getString("money.currency")
        ).await();

        String prefix = WebUtil.escapeHtml(this.settings().getString("text.prefix")).replace(" ", "&nbsp;");
        String suffix = WebUtil.escapeHtml(this.settings().getString("text.suffix")).replace(" ", "&nbsp;");

        if (!prefix.isEmpty()) {
            html = prefix + ' ' + html;
        }

        if (!suffix.isEmpty()) {
            html = html + ' ' + suffix;
        }

        this.currHtml = html;

        this.broadcastToAll("html", JsonObject.singleton("html", this.currHtml));
    }

    @Override
    public void onNewInstance(@NonNull WidgetInstance instance) {
        try {
            instance.emit("html", JsonObject.singleton("html", this.currHtml));
        } catch (IOException ignored) {}
    }

    @Override
    protected boolean hasHighlight() {
        return false;
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

}
