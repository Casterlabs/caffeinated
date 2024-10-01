package co.casterlabs.caffeinated.builtin.widgets.alerts;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.events.rich.fragments.ChatFragment;
import co.casterlabs.koi.api.types.events.rich.fragments.LinkFragment;

public class DonationAlert extends GenericAlert implements KoiEventListener {
    @SuppressWarnings("deprecation")
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.donation_alert")
        .withIcon("currency-dollar")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Donation Alert")
        .withShowDemo(true, DEMO_ASPECT_RATIO)
        .withRequiredFeatures(KoiIntegrationFeatures.DONATION_ALERT)
        .withTestEvents(KoiEventType.DONATION);

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onDonation(RichMessageEvent e) {
        if (!this.getSelectedPlatforms().contains(e.streamer.platform)) return;
        if (e.donations.isEmpty()) return;

        Donation donation = e.donations.get(0);

        // Generate the ttsText
        String ttsText = e.raw;

        for (ChatFragment link : e.fragments) {
            if (link instanceof LinkFragment) {
                ttsText = ttsText.replace(((LinkFragment) link).url, "(link)");
            }
        }

        this.queueAlert(e, donation.image, ttsText);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = super.generateSettingsLayout();

        {
            WidgetSettingsSection imageSection = new WidgetSettingsSection("image", "Alert Image")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Show Image", true));

            if (this.settings().getBoolean("image.enabled", true)) {
                imageSection.addItem(WidgetSettingsItem.asDropdown("source", "Source", "Donation Image", "Donation Image", "Custom Image"));

                if (this.settings().getString("image.source", "").equals("Custom Image")) {
                    imageSection.addItem(WidgetSettingsItem.asFile("file", "Image File", "image", "video"));
                }
            }

            layout.addSection(imageSection);
        }

        return layout;
    }

    @Override
    protected @Nullable String getImage(@Nullable String customImage) {
        if (customImage != null) {
            switch (this.settings().getString("image.source", "")) {
                case "Donation Image (Animated)":
                case "Donation Image": {
                    return customImage;
                }
            }
        }

        return super.getImage(customImage);
    }

    @Override
    protected String defaultFormat() {
        return "${escapeHtml(event.sender.displayname)} just donated ${Currencies.formatCurrency(event.donations.reduce((acc, d) => acc + d.amount, 0), event.donations[0].currency)}!";
    }

    @Override
    protected boolean hasCustomImageImplementation() {
        return true;
    }

    @Override
    protected boolean hasTTS() {
        return true;
    }

    @Override
    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[] {
                KoiIntegrationFeatures.DONATION_ALERT
        };
    }

}
