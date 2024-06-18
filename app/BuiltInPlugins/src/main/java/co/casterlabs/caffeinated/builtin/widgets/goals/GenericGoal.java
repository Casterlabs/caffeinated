package co.casterlabs.caffeinated.builtin.widgets.goals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsButton;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsCheckboxBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsColorBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsFileBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsFontBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsNumberBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsPlatformDropdownBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsRangeBuilder;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsTextBuilder;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;

public abstract class GenericGoal extends Widget implements KoiEventListener {
    public static final double DEMO_ASPECT_RATIO = 1 / 10d;

    protected @Getter double count;

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
    @SuppressWarnings("deprecation")
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        String currentStyle = this.settings().has("style.style") ? this.settings().getString("style.style") : "Progress Bar";

        {
            WidgetSettingsSection barStyle = new WidgetSettingsSection("style", "Style")
                .addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("margin")
                        .withName("Margin (px)")
                        .withDefaultValue(0)
                        .withStep(1)
                        .withMin(0)
                        .withMax(Integer.MAX_VALUE)
                        .build()
                )
                .addItem(
                    new WidgetSettingsDropdownBuilder()
                        .withId("style")
                        .withName("Style")
                        .withDefaultValue("Progress Bar (With Text)")
                        .withOptionsList("Progress Bar (With Text)", "Progress Bar", "Text Only")
                        .build()
                )
                .addItem(
                    new WidgetSettingsFontBuilder()
                        .withId("font")
                        .withName("Font")
                        .withDefaultValue("Poppins")
                        .build()
                )
                .addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("font_size")
                        .withName("Font Size")
                        .withDefaultValue(16)
                        .withStep(1)
                        .withMin(0)
                        .withMax(128)
                        .build()
                )
//                .addItem(
//                    new WidgetSettingsRangeBuilder()
//                        .withId("font_weight")
//                        .withName("Font Weight (Boldness)")
//                        .withDefaultValue(400)
//                        .withStep(100)
//                        .withMin(100)
//                        .withMax(1000)
//                        .build()
//                )
//                .addItem(
//                    new WidgetSettingsRangeBuilder()
//                        .withId("letter_spacing")
//                        .withName("Letter Spacing")
//                        .withDefaultValue(0)
//                        .withStep(1)
//                        .withMin(-5)
//                        .withMax(5)
//                        .build()
//                )
                .addItem(
                    new WidgetSettingsColorBuilder()
                        .withId("text_color")
                        .withName("Text Color")
                        .withDefaultValue("#ffffff")
                        .build()
                );

            if (currentStyle.equals("Text Only")) {
                barStyle
                    .addItem(
                        new WidgetSettingsDropdownBuilder()
                            .withId("text_align")
                            .withName("Text Align")
                            .withDefaultValue("Left")
                            .withOptionsList("Left", "Right", "Center")
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("text_shadow")
                            .withName("Text Shadow")
                            .withDefaultValue(-1)
                            .withStep(1)
                            .withMin(-1)
                            .withMax(20)
                            .build()
                    );
            }

            barStyle.addItem(
                new WidgetSettingsRangeBuilder()
                    .withId("outline_width")
                    .withName("Outline Size")
                    .withDefaultValue(0)
                    .withStep(.01)
                    .withMin(0)
                    .withMax(1)
                    .build()
            );
            if (this.settings().getNumber("style.outline_width", 0).doubleValue() > 0) {
                barStyle.addItem(
                    new WidgetSettingsColorBuilder()
                        .withId("outline_color")
                        .withName("Outline Color")
                        .withDefaultValue("#000000")
                        .build()
                );
            }

            layout.addSection(barStyle);
        }

        {
            WidgetSettingsSection barGoal = new WidgetSettingsSection("goal", "Goal")
                .addItem(
                    new WidgetSettingsTextBuilder()
                        .withId("title")
                        .withName("Title")
                        .withDefaultValue("")
                        .withPlaceholder("")
                        .build()
                )
                .addItem(
                    new WidgetSettingsNumberBuilder()
                        .withId("target")
                        .withName("Target")
                        .withDefaultValue(10)
                        .withStep(1)
                        .withMin(0)
                        .withMax(Integer.MAX_VALUE)
                        .build()
                );

            if (currentStyle.contains("Progress Bar")) {
                barGoal
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("rounded_edges")
                            .withName("Rounded Edges")
                            .withDefaultValue(false)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsCheckboxBuilder()
                            .withId("add_numbers")
                            .withName("Show Numbers")
                            .withDefaultValue(true)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsColorBuilder()
                            .withId("bar_color")
                            .withName("Bar Color")
                            .withDefaultValue("#31f8ff")
                            .build()
                    );

                boolean isRounded = this.settings().has("goal.rounded_edges") ? this.settings().getBoolean("goal.rounded_edges") : false;
                if (isRounded) {
                    barGoal
                        .addItem(
                            new WidgetSettingsNumberBuilder()
                                .withId("roundness")
                                .withName("Roundess (px)")
                                .withDefaultValue(20)
                                .withStep(1)
                                .withMin(0)
                                .withMax(30)
                                .build()
                        );
                }
            }

            if (this.enableValueSetting()) {
                barGoal
                    .addItem(
                        new WidgetSettingsNumberBuilder()
                            .withId("value")
                            .withName("Value")
                            .withDefaultValue(1)
                            .withStep(1)
                            .withMin(0)
                            .withMax(Integer.MAX_VALUE)
                            .build()
                    );;
            }

            layout.addSection(barGoal);
        }

        {
            WidgetSettingsSection alert = new WidgetSettingsSection("alert", "Alert")
                .addItem(
                    new WidgetSettingsCheckboxBuilder()
                        .withId("enabled")
                        .withName("Enabled")
                        .withDefaultValue(false)
                        .build()
                );

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
                    .addItem(
                        new WidgetSettingsNumberBuilder()
                            .withId("duration")
                            .withName("Duration (Seconds)")
                            .withDefaultValue(15)
                            .withStep(1)
                            .withMin(0)
                            .withMax(60)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsFontBuilder()
                            .withId("font")
                            .withName("Font")
                            .withDefaultValue("Poppins")
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("font_size")
                            .withName("Font Size")
                            .withDefaultValue(16)
                            .withStep(1)
                            .withMin(0)
                            .withMax(128)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("font_weight")
                            .withName("Font Weight (Boldness)")
                            .withDefaultValue(400)
                            .withStep(100)
                            .withMin(100)
                            .withMax(1000)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsDropdownBuilder()
                            .withId("text_align")
                            .withName("Text Align")
                            .withDefaultValue("Left")
                            .withOptionsList("Left", "Right", "Center")
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsColorBuilder()
                            .withId("text_color")
                            .withName("Text Color")
                            .withDefaultValue("#ffffff")
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsColorBuilder()
                            .withId("highlight_color")
                            .withName("Highlight Color")
                            .withDefaultValue("#5bf599")
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsTextBuilder()
                            .withId("text")
                            .withName("Text")
                            .withDefaultValue("We did it!")
                            .withPlaceholder("")
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("vertical_offset")
                            .withName("Text Vertical Offset")
                            .withDefaultValue(1)
                            .withStep(.05)
                            .withMin(0)
                            .withMax(1)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("horizontal_offset")
                            .withName("Text Horizontal Offset")
                            .withDefaultValue(0)
                            .withStep(.05)
                            .withMin(-1)
                            .withMax(1)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("shadow")
                            .withName("Shadow")
                            .withDefaultValue(-1)
                            .withStep(1)
                            .withMin(-1)
                            .withMax(20)
                            .build()
                    );

                alert.addItem(
                    new WidgetSettingsRangeBuilder()
                        .withId("outline_width")
                        .withName("Outline Size")
                        .withDefaultValue(0)
                        .withStep(.01)
                        .withMin(0)
                        .withMax(1)
                        .build()
                );
                if (this.settings().getNumber("alert.outline_width", 0).doubleValue() > 0) {
                    alert.addItem(
                        new WidgetSettingsColorBuilder()
                            .withId("outline_color")
                            .withName("Outline Color")
                            .withDefaultValue("#000000")
                            .build()
                    );
                }

                layout.addSection(alert);

                {
                    WidgetSettingsSection audioSection = new WidgetSettingsSection("alert.audio", "Alert Audio")
                        .addItem(
                            new WidgetSettingsCheckboxBuilder()
                                .withId("enabled")
                                .withName("Play Audio")
                                .withDefaultValue(true)
                                .build()
                        );

                    if (this.settings().getBoolean("audio.enabled", true)) {
                        audioSection
                            .addItem(
                                new WidgetSettingsFileBuilder()
                                    .withId("file")
                                    .withName("Audio File")
                                    .withAllowedTypes("audio")
                                    .build()

                            )
                            .addItem(
                                new WidgetSettingsRangeBuilder()
                                    .withId("volume")
                                    .withName("Volume")
                                    .withDefaultValue(.5)
                                    .withStep(.01)
                                    .withMin(0)
                                    .withMax(1)
                                    .build()
                            );
                    }

                    layout.addSection(audioSection);
                }

                {
                    WidgetSettingsSection imageSection = new WidgetSettingsSection("alert.image", "Alert Image")
                        .addItem(
                            new WidgetSettingsCheckboxBuilder()
                                .withId("enabled")
                                .withName("Show Image")
                                .withDefaultValue(true)
                                .build()
                        );

                    if (this.settings().getBoolean("image.enabled", true)) {
                        imageSection
                            .addItem(
                                new WidgetSettingsFileBuilder()
                                    .withId("file")
                                    .withName("Image File")
                                    .withAllowedTypes("image", "video")
                                    .build()
                            );
                    }

                    layout.addSection(imageSection);
                }
            } else {
                layout.addSection(alert);
            }
        }

        if (this.enablePlatformOption()) {
            layout.addSection(
                new WidgetSettingsSection("platform", "Platform")
                    .addItem(
                        new WidgetSettingsPlatformDropdownBuilder()
                            .withId("platforms")
                            .withName("Use from")
                            .withAllowMultiple(true)
                            .withRequiredFeatures(this.requiredPlatformFeatures())
                            .build()
                    )

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

    public List<UserPlatform> getSelectedPlatforms() {
        return this.settings().get("platform.platforms", new TypeToken<List<UserPlatform>>() {
        }, Collections.emptyList());
    }

    protected abstract boolean enablePlatformOption();

    protected KoiIntegrationFeatures[] requiredPlatformFeatures() {
        return new KoiIntegrationFeatures[0];
    }

    protected List<WidgetSettingsButton> getButtons() {
        return Collections.emptyList();
    }

    protected boolean enableValueSetting() {
        return false;
    }

}
