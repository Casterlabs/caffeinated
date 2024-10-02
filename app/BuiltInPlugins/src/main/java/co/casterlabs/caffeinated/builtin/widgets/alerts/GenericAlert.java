package co.casterlabs.caffeinated.builtin.widgets.alerts;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.Evaluater;
import co.casterlabs.caffeinated.pluginsdk.TTS;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
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
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.items.WidgetSettingsTextAreaBuilder;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public abstract class GenericAlert extends Widget {
    public static final double DEMO_ASPECT_RATIO = 3 / 4d;

    @Override
    protected void onSettingsUpdate() {
        this.renderSettingsLayout();
    }

    private void renderSettingsLayout() {
        WidgetSettingsLayout layout = this.generateSettingsLayout();

        this.setSettingsLayout(layout);
    }

    /**
     * Override as neeeded.
     */
    @SuppressWarnings({
            "deprecation"
    })
    protected WidgetSettingsLayout generateSettingsLayout() {
        WidgetSettingsLayout layout = new WidgetSettingsLayout();

        {
            WidgetSettingsSection textStyle = new WidgetSettingsSection("style", "Style")
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
                    new WidgetSettingsRangeBuilder()
                        .withId("letter_spacing")
                        .withName("Letter Spacing")
                        .withDefaultValue(0)
                        .withStep(1)
                        .withMin(-5)
                        .withMax(5)
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
            if (this.settings().getNumber("style.outline_width", 0).doubleValue() > 0) {
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

        {
            WidgetSettingsSection textSection = new WidgetSettingsSection("text", "Text")
                .addItem(
                    new WidgetSettingsTextAreaBuilder()
                        .withId("format")
                        .withName("Format")
                        .withDefaultValue(this.defaultFormat())
                        .withPlaceholder("")
                        .build()
                );

            layout.addSection(textSection);
        }

        if (this.hasTTS()) {
            WidgetSettingsSection ttsSection = new WidgetSettingsSection("tts", "Text To Speech")
                .addItem(
                    new WidgetSettingsCheckboxBuilder()
                        .withId("enabled")
                        .withName("Enabled")
                        .withDefaultValue(true)
                        .build()
                );

            if (this.settings().getBoolean("tts.enabled", true)) {
                ttsSection
                    .addItem(
                        new WidgetSettingsRangeBuilder()
                            .withId("volume")
                            .withName("Volume")
                            .withDefaultValue(.5)
                            .withStep(.01)
                            .withMin(0)
                            .withMax(1)
                            .build()
                    )
                    .addItem(
                        new WidgetSettingsDropdownBuilder()
                            .withId("voice")
                            .withName("Default Voice")
                            .withDefaultValue("Brian")
                            .withOptionsList(TTS.getVoicesAsArray())
                            .build()
                    );

                layout.addButton(
                    new WidgetSettingsButton("skip-tts")
                        .withIcon("forward")
                        .withIconTitle("Skip TTS")
                        .withOnClick(() -> {
                            this.broadcastToAll("skip-tts", new JsonObject());
                        })
                );
            } else {
                // Since tts is disabled, we want to forcibly skip the tts if it's playing.
                this.broadcastToAll("skip-tts", new JsonObject());
            }

            layout.addSection(ttsSection);
        }

        {
            WidgetSettingsSection audioSection = new WidgetSettingsSection("audio", "Alert Audio")
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

        if (!this.hasCustomImageImplementation()) {
            WidgetSettingsSection imageSection = new WidgetSettingsSection("image", "Alert Image")
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

        for (WidgetSettingsButton button : this.getButtons()) {
            layout.addButton(button);
        }

        return layout;
    }

    @Override
    public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
        return "/alert.html";
    }

    public void queueAlert(@NonNull KoiEvent event, @Nullable String customImage, @Nullable String ttsText) {
        String ttsAudio = null;

        // Generate the base64 audio data for TTS if enabled.
        if ((ttsText != null) &&
            this.settings().getBoolean("tts.enabled", true)) {
            try {
                byte[] audioBytes = TTS.getSpeech(this.settings().getString("tts.voice"), ttsText);

                ttsAudio = "data:audio/mp3;base64," + Base64.getMimeEncoder().encodeToString(audioBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String title = Evaluater.replace(
            this.settings().getString("text.format"),
            "<span class='highlight'>", "</span>",
            event
        );

        // Send it on it's way.
        this.broadcastToAll(
            "alert",
            new JsonObject()
                .put("title", title)
                .put("chatEvent", Rson.DEFAULT.toJson(event instanceof RichMessageEvent ? event : null))
                .put("image", this.getImage(customImage))
                .put("audio", this.getAudio())
                .put("ttsAudio", ttsAudio)
        );
    }

    protected List<WidgetSettingsButton> getButtons() {
        return Collections.emptyList();
    }

    protected @Nullable String getImage(@Nullable String customImage) {
        return this.settings().getString("image.file");
    }

    protected @Nullable String getAudio() {
        return this.settings().getString("audio.file");
    }

    protected abstract String defaultFormat();

    protected abstract boolean hasCustomImageImplementation();

    protected abstract boolean hasTTS();

    protected abstract KoiIntegrationFeatures[] requiredPlatformFeatures();

    public List<UserPlatform> getSelectedPlatforms() {
        List<UserPlatform> list = new LinkedList<>();
        list.addAll(this.settings().get("platform.platforms", new TypeToken<List<UserPlatform>>() {
        }, Collections.emptyList()));
        list.add(UserPlatform.CASTERLABS_SYSTEM);
        list.add(UserPlatform.CUSTOM_INTEGRATION);
        return list;
    }

}
