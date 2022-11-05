package co.casterlabs.caffeinated.builtin.widgets.labels.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import lombok.NonNull;

public abstract class GenericLabel extends Widget implements KoiEventListener {
    private static String[] platforms;

    private boolean wasMultiPlatform;

    static {
        List<String> platformsList = new ArrayList<>();

        for (UserPlatform platform : UserPlatform.values()) {
            if (platform == UserPlatform.CASTERLABS_SYSTEM) {
                continue;
            }

            String name = platform.name().toLowerCase();

            platformsList.add(name.substring(0, 1).toUpperCase() + name.substring(1));
        }

        platforms = platformsList.toArray(new String[0]);
    }

    @Override
    public void onInit() {
        this.renderSettingsLayout();

        this.addKoiListener(this);
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
                .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"))
                .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"));

            if (this.hasHighlight()) {
                textStyle.addItem(WidgetSettingsItem.asColor("highlight_color", "Highlight Color", "#5bf599"));
            }

            layout.addSection(textStyle);
        }

        layout.addSection(
            new WidgetSettingsSection("text", "Text")
                .addItem(WidgetSettingsItem.asText("prefix", "Prefix", "", ""))
                .addItem(WidgetSettingsItem.asText("suffix", "Suffix", "", ""))
        );

        if (this.enablePlatformOption() && this.isMultiPlatform()) {
            layout.addSection(
                new WidgetSettingsSection("platform", "Platform")
                    .addItem(WidgetSettingsItem.asDropdown("platform", "Platform", platforms[0], platforms))
            );
        }

        for (WidgetSettingsButton button : this.getButtons()) {
            layout.addButton(button);
        }

        return layout;
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/text.html";
    }

    @KoiEventHandler
    public void GenericLabel_onUserUpdate(UserUpdateEvent event) {
        boolean isMultiPlatform = this.isMultiPlatform();

        if (isMultiPlatform != this.wasMultiPlatform) {
            this.wasMultiPlatform = isMultiPlatform;
            this.renderSettingsLayout();
        }
    }

    public @Nullable UserPlatform getSelectedPlatform() {
        if (Caffeinated.getInstance().getKoi().isSignedOut()) {
            return null;
        } else {
            UserPlatform platform = Caffeinated.getInstance().getKoi().getFirstSignedInPlatform();

            if (this.isMultiPlatform()) {
                try {
                    UserPlatform selectedPlatform = UserPlatform.valueOf(this.settings().getString("platform.platform").toUpperCase());

                    // Make sure that platform is signed in.
                    if (Caffeinated.getInstance().getKoi().getUserStates().containsKey(selectedPlatform)) {
                        platform = selectedPlatform;
                    }
                } catch (Exception e) {
                    // INVALID VALUE.
                }
            }

            return platform;
        }
    }

    protected abstract boolean hasHighlight();

    protected abstract boolean enablePlatformOption();

    protected List<WidgetSettingsButton> getButtons() {
        return Collections.emptyList();
    }

    protected boolean isMultiPlatform() {
        return Caffeinated.getInstance().getKoi().getUserStates().size() > 1;
    }

}
