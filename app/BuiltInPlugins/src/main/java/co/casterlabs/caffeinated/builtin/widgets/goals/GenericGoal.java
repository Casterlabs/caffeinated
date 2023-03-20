package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;

public abstract class GenericGoal extends Widget implements KoiEventListener {
    public static final double DEMO_ASPECT_RATIO = 1 / 10d;

    private static String[] platforms;

    private boolean wasMultiPlatform;
    private @Getter double count;

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
        this.addKoiListener(this);

        // We don't care if this fails.
        try {
            this.count = this.settings().getNumber("count").doubleValue();
        } catch (Exception ignored) {}
    }

    private void save() {
        this.settings()
            .set("count", this.count);
    }

    @Override
    protected void onSettingsUpdate() {
        this.renderSettingsLayout();
    }

    protected void renderSettingsLayout() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();

        this.setSettingsLayout(layout); // Preserve
    }

    /**
     * Override as neeeded.
     */
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        String currentStyle = this.settings().has("style.style") ? this.settings().getString("style.style") : "Progress Bar";

        {
            WidgetSettingsSection barStyle = new WidgetSettingsSection("style", "Style")
                .addItem(WidgetSettingsItem.asDropdown("style", "Style", "Progress Bar (With Text)", "Progress Bar", "Text Only"))
                .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"));

            if (currentStyle.equals("Text Only")) {
                barStyle.addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"));
            }

            layout.addSection(barStyle);
        }

        {
            WidgetSettingsSection barGoal = new WidgetSettingsSection("goal", "Goal")
                .addItem(WidgetSettingsItem.asText("title", "Title", "", ""))
                .addItem(WidgetSettingsItem.asNumber("target", "Target", 10, 1, 0, Integer.MAX_VALUE));

            if (currentStyle.equals("Progress Bar")) {
                barGoal
                    .addItem(WidgetSettingsItem.asCheckbox("rounded_edges", "Rounded Edges", false))
                    .addItem(WidgetSettingsItem.asCheckbox("add_numbers", "Show Numbers", true))
                    .addItem(WidgetSettingsItem.asColor("bar_color", "Bar Color", "#31f8ff"));

                boolean isRounded = this.settings().has("goal.rounded_edges") ? this.settings().getBoolean("goal.rounded_edges") : false;
                if (isRounded) {
                    barGoal.addItem(WidgetSettingsItem.asNumber("roundness", "Roundess (px)", 20, 1, 0, 30));
                }
            }

            if (this.enableValueSetting()) {
                barGoal.addItem(WidgetSettingsItem.asNumber("value", "Value", 1, 1, 0, Integer.MAX_VALUE));
            }

            layout.addSection(barGoal);
        }

        {
            WidgetSettingsSection alert = new WidgetSettingsSection("alert", "Alert")
                .addItem(WidgetSettingsItem.asCheckbox("enabled", "Enabled", false));

            if (this.settings().getBoolean("alert.enabled", false)) {
                layout.addButton(
                    new WidgetSettingsButton(
                        "copy-alert-url",
                        "bell",
                        "Copy Alert URL",
                        "Copy Alert URL",
                        () -> {
                            Caffeinated.getInstance().copyText(
                                this.getUrl().replace("=WIDGET", "=WIDGET_ALT"),
                                null
                            );
                        }
                    )
                );

                alert
                    .addItem(WidgetSettingsItem.asNumber("duration", "Duration (Seconds)", 15, 1, 0, 60))
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128))
                    .addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"))
                    .addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"))
                    .addItem(WidgetSettingsItem.asColor("highlight_color", "Highlight Color", "#5bf599"))
                    .addItem(WidgetSettingsItem.asText("text", "Text", "We did it!", ""));

                layout.addSection(alert);

                {
                    WidgetSettingsSection audioSection = new WidgetSettingsSection("alert.audio", "Alert Audio")
                        .addItem(WidgetSettingsItem.asCheckbox("enabled", "Play Audio", true));

                    if (this.settings().getBoolean("audio.enabled", true)) {
                        audioSection
                            .addItem(WidgetSettingsItem.asFile("file", "Audio File", "audio"))
                            .addItem(WidgetSettingsItem.asRange("volume", "Volume", .5, .01, 0, 1));
                    }

                    layout.addSection(audioSection);
                }

                {
                    WidgetSettingsSection imageSection = new WidgetSettingsSection("alert.image", "Alert Image")
                        .addItem(WidgetSettingsItem.asCheckbox("enabled", "Show Image", true));

                    if (this.settings().getBoolean("image.enabled", true)) {
                        imageSection.addItem(WidgetSettingsItem.asFile("file", "Image File", "image", "video"));
                    }

                    layout.addSection(imageSection);
                }
            } else {
                layout.addSection(alert);
            }
        }

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
        if (mode == WidgetInstanceMode.WIDGET_ALT) {
            return "/goal_alert.html";
        } else {
            return "/goal";
        }
    }

    @KoiEventHandler
    public void GenericGoal_onUserUpdate(UserUpdateEvent event) {
        boolean isMultiPlatform = this.isMultiPlatform();

        if (isMultiPlatform != this.wasMultiPlatform) {
            this.wasMultiPlatform = isMultiPlatform;
            this.renderSettingsLayout();
        }
    }

    public void update(double count) {
        this.count = count;
        this.save();

        this.broadcastToAll("count", JsonObject.singleton("count", this.count));
    }

    @Override
    public void onNewInstance(@NonNull WidgetInstance instance) {
        try {
            instance.emit("count", JsonObject.singleton("count", this.count));
        } catch (IOException ignored) {}
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

    protected abstract boolean enablePlatformOption();

    protected List<WidgetSettingsButton> getButtons() {
        return Collections.emptyList();
    }

    protected boolean isMultiPlatform() {
        return Caffeinated.getInstance().getKoi().getUserStates().size() > 1;
    }

    protected boolean enableValueSetting() {
        return false;
    }

}
