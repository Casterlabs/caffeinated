package co.casterlabs.caffeinated.builtin.widgets.alerts.generic;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.DonationEvent;
import co.casterlabs.koi.api.types.events.rich.Donation;

public class DonationAlert extends GenericAlert implements KoiEventListener {
    public static final WidgetDetails DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.donation_alert")
        .withIcon("currency-dollar")
        .withCategory(WidgetDetailsCategory.ALERTS)
        .withFriendlyName("Donation Alert");

    @Override
    public void onInit() {
        this.addKoiListener(this);
    }

    @KoiEventHandler
    public void onDonation(DonationEvent e) {
        // Images.
        Donation donation = e.getDonations().get(0);

        String[] customImages = {
                donation.getImage(),
                donation.getAnimatedImage()
        };

        // Generate the title html.
        String title = String.format("<span class='highlight'>%s</span>", e.getSender().getDisplayname());

        // Generate the ttsText
        String ttsText = e.getMessage();

        for (String link : e.getLinks()) {
            ttsText = ttsText.replace(link, "(link)");
        }

        this.queueAlert(title, e, customImages, e.getMessage());
    }

    @Override
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = super.generateSettingsLayout();

        {
            WidgetSettingsSection imageSection = new WidgetSettingsSection("image", "Alert Image")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Show Image", true));

            if (this.settings().getBoolean("image.enabled", true)) {
                imageSection.addItem(WidgetSettingsItem.asDropdown("source", "Source", "Donation Image (Animated)", "Donation Image", "Donation Image (Animated)", "Custom Image"));

                if (this.settings().getString("image.source", "").equals("Custom Image")) {
                    imageSection.addItem(WidgetSettingsItem.asFile("file", "Image File", "image", "video"));
                }
            }

            layout.addSection(imageSection);
        }

        return layout;
    }

    @Override
    protected @Nullable String getImage(@Nullable String[] customImages) {
        if ((customImages != null) &&
            (customImages.length == 2)) {

            switch (this.settings().getString("image.source", "")) {
                case "Donation Image": {
                    return customImages[0];
                }

                case "Donation Image (Animated)": {
                    return customImages[1];
                }
            }
        }

        return super.getImage(customImages);
    }

    @Override
    protected String defaultPrefix() {
        return "";
    }

    @Override
    protected String defaultSuffix() {
        return "just donated!";
    }

    @Override
    protected boolean hasCustomImageImplementation() {
        return true;
    }

    @Override
    protected boolean hasTTS() {
        return true;
    }

}
