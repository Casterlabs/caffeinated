package co.casterlabs.caffeinated.builtin.widgets.goals.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
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
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class GenericGoal extends Widget implements KoiEventListener {
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
        this.renderSettingsLayout();

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

    private void renderSettingsLayout() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();

        this.setSettingsLayout(layout, true); // Preserve
    }

    /**
     * Override as neeeded.
     */
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        String currentStyle = this.settings().has("style.style") ? this.settings().getString("style.style") : "Progress Bar (With Text)";

        {
            WidgetSettingsSection barStyle = new WidgetSettingsSection("style", "Style")
                .addItem(WidgetSettingsItem.asDropdown("style", "Style", "Progress Bar (With Text)", "Progress Bar (With Text)", "Text", "Progress Bar (Without Text)"));

            if (!currentStyle.equals("Progress Bar (Without Text)")) {
                barStyle
                    .addItem(WidgetSettingsItem.asFont("font", "Font", "Poppins"))
                    .addItem(WidgetSettingsItem.asNumber("font_size", "Font Size (px)", 16, 1, 0, 128));

                if (currentStyle.equals("Text")) {
                    barStyle.addItem(WidgetSettingsItem.asDropdown("text_align", "Text Align", "Left", "Left", "Right", "Center"));
                }

                barStyle.addItem(WidgetSettingsItem.asColor("text_color", "Text Color", "#ffffff"));
            }

            layout.addSection(barStyle);
        }

        {
            WidgetSettingsSection barGoal = new WidgetSettingsSection("goal", "Goal");

            FastLogger.logStatic(currentStyle);;

            if (currentStyle.equals("Progress Bar (Without Text)")) {
                barGoal.addItem(WidgetSettingsItem.asColor("bar_color", "Bar Color", "#31f8ff"));
            } else {
                barGoal
                    .addItem(WidgetSettingsItem.asText("title", "Title", "", ""))
                    .addItem(WidgetSettingsItem.asNumber("target", "Target", 10, 1, 0, Integer.MAX_VALUE));

                if (currentStyle.equals("Progress Bar (With Text)")) {
                    barGoal
                        .addItem(WidgetSettingsItem.asCheckbox("add_numbers", "Add Numbers", true))
                        .addItem(WidgetSettingsItem.asColor("bar_color", "Bar Color", "#31f8ff"));
                }

            }

            if (this.enableValueSetting()) {
                barGoal.addItem(WidgetSettingsItem.asNumber("value", "Value", 1, 1, 0, Integer.MAX_VALUE));
            }

            layout.addSection(barGoal);
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

    @SneakyThrows
    @Override
    public @Nullable String getWidgetHtml() {
        return CaffeinatedDefaultPlugin.resolveResource("/goal.html");
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
