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
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class RecentDonationLabel extends GenericLabel {
    @SuppressWarnings("deprecation")
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.recent_donation_label")
        .withIcon("currency-dollar")
        .withCategory(WidgetDetailsCategory.LABELS)
        .withFriendlyName("Recent Donation Label")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.DONATION_ALERT)
        .withTestEvents(KoiEventType.DONATION);

    private User recentDonator;
    private double recentAmount;
    private String recentCurrency;
    private String currHtml = "";

    @Override
    public void onInit() {
        super.onInit();

        this.addKoiListener(this);

        // If this fails then we don't care.
        try {
            JsonElement recentSubscriber = this.settings().get("recent_donator");

            this.recentDonator = Rson.DEFAULT.fromJson(recentSubscriber, User.class);
            this.recentAmount = this.settings().getNumber("recent_amount").doubleValue();
            this.recentCurrency = this.settings().getString("recent_currency");
        } catch (Exception ignored) {}

        this.updateText();
    }

    private void save() {
        this.settings()
            .set("recent_donator", Rson.DEFAULT.toJson(this.recentDonator))
            .set("recent_amount", this.recentAmount)
            .set("recent_currency", this.recentCurrency);
    }

    @Override
    protected List<WidgetSettingsButton> getButtons() {
        return Arrays.asList(
            new WidgetSettingsButton("reset")
                .withIcon("no-symbol")
                .withIconTitle("Reset Counter")
                .withText("Reset Counter")
                .withOnClick(() -> {
                    this.recentDonator = null;
                    this.recentAmount = 0;
                    this.recentCurrency = null;

                    this.save();
                    this.updateText();
                })
        );
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
                .addItem(WidgetSettingsItem.asCurrency("currency", "Currency", "USD", true))
                .addItem(WidgetSettingsItem.asDropdown("style", "Style", "Name with Amount", "Name with Amount", "Name Only", "Amount Only"))
        );

        return layout;
    }

    @KoiEventHandler
    public void onDonation(@Nullable RichMessageEvent e) {
        if (e.donations.isEmpty()) return;

        AsyncTask.create(() -> {
            double total = 0;
            String currency = "USD";

            for (Donation d : e.donations) {
                try {
                    Double convertedAmount = Currencies.convertCurrency(d.amount * d.count, d.currency, Currencies.baseCurrency).await();

                    total += convertedAmount;
                } catch (Throwable t) {
                    FastLogger.logException(t);
                }

                // Is always homogenous.
                currency = d.currency;
            }

            this.recentDonator = e.sender;
            this.recentAmount = total;
            this.recentCurrency = currency;

            this.save();
            this.updateText();
        });
    }

    @SneakyThrows
    private void updateText() {
        if (this.recentDonator == null) {
            this.currHtml = "";
        } else {
            String html = "";

            String formattedTotal = null;
            boolean showName = false;

            switch (this.settings().getString("money.style")) {
                case "Name Only": {
                    showName = true;
                    break;
                }

                case "Name with Amount": {
                    showName = true;
                    // Fall through.
                }

                case "Amount Only": {
                    String targetCurrency = this.settings().getString("money.currency");
                    formattedTotal = Currencies.convertAndFormatCurrency(
                        this.recentAmount,
                        Currencies.baseCurrency,
                        (targetCurrency == "DEFAULT") ? this.recentCurrency : targetCurrency
                    ).await();

                    break;
                }
            }

            if (showName) {
                html = this.recentDonator.displayname + " ";
            }

            if (formattedTotal != null) {
                html += String.format("<span class='highlight'>%s</span>", formattedTotal);
            }

            String prefix = WebUtil.escapeHtml(this.settings().getString("text.prefix")).replace(" ", "&nbsp;");
            String suffix = WebUtil.escapeHtml(this.settings().getString("text.suffix")).replace(" ", "&nbsp;");

            if (!prefix.isEmpty()) {
                html = prefix + ' ' + html;
            }

            if (!suffix.isEmpty()) {
                html = html + ' ' + suffix;
            }

            this.currHtml = html;
        }

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
        return true;
    }

    @Override
    protected boolean enablePlatformOption() {
        return false;
    }

}
