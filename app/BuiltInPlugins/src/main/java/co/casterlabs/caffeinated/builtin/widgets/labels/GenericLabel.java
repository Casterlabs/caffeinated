package co.casterlabs.caffeinated.builtin.widgets.labels;

import java.util.Collections;
import java.util.List;

import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsColorBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsRangeBuilder;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.TypeToken;
import lombok.NonNull;

public abstract class GenericLabel extends Widget implements KoiEventListener {
    public static final double DEMO_ASPECT_RATIO = 1 / 8d;

    @Override
    public void onInit() {
        this.renderSettingsLayout();

        this.addKoiListener(this);
    }

    @Override
    protected void onSettingsUpdate() {
        this.renderSettingsLayout();
    }

    private void renderSettingsLayout() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();

        this.setSettingsLayout(layout); // Preserve
    }

    /**
     * Override as neeeded.
     */
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        {
            WidgetSettingsSection textStyle = new WidgetSettingsSection("text_style", "Style")
                .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                .addItem(WidgetSettingsItem.asRange("font_size", "Font Size", 16, 1, 0, 128))
                .addItem(WidgetSettingsItem.asRange("font_weight", "Font Weight (boldness)", 400, 100, 100, 1000))
                .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"))
                .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                .addItem(WidgetSettingsItem.asRange("text_shadow", "Text Shadow", -1, 1, -1, 20));

            if (this.hasHighlight()) {
                textStyle.addItem(WidgetSettingsItem.asColor("highlight_color", "Highlight Color", "#5bf599"));
            }

            textStyle.addItem(
                new WidgetSettingsRangeBuilder()
                    .withId("outline_width")
                    .withName("Outline Size")
                    .withDefaultValue(0)
                    .withStep(.01)
                    .withMin(0)
                    .withMax(1)
                    .build()
            );
            if (this.settings().getNumber("text_style.outline_width", 0).doubleValue() > 0) {
                textStyle.addItem(
                    new WidgetSettingsColorBuilder()
                        .withId("outline_color")
                        .withName("Outline Color")
                        .withDefaultValue("#000000")
                        .build()
                );
            }

            layout.addSection(textStyle);
        }

        layout.addSection(
            new WidgetSettingsSection("text", "Text")
                .addItem(WidgetSettingsItem.asText("prefix", "Prefix", "", ""))
                .addItem(WidgetSettingsItem.asText("suffix", "Suffix", "", ""))
        );

        if (this.enablePlatformOption()) {
            layout.addSection(
                new WidgetSettingsSection("platform", "Platform")
                    .addItem(WidgetSettingsItem.asPlatformDropdown("platforms", "Use from", true, this.requiredPlatformFeatures()))
            );
        }

        for (WidgetSettingsButton button : this.getButtons()) {
            layout.addButton(button);
        }

        return layout;
    }

    public List<UserPlatform> getSelectedPlatforms() {
        return this.settings().get("platform.platforms", new TypeToken<List<UserPlatform>>() {
        }, Collections.emptyList());
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/text";
    }

    protected abstract boolean hasHighlight();

    protected abstract boolean enablePlatformOption();

    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[0];
    }

    protected List<WidgetSettingsButton> getButtons() {
        return Collections.emptyList();
    }

}
