package co.casterlabs.caffeinated.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails.WidgetDetailsCategory;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsItem;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsLayout;
import co.casterlabs.caffeinated.pluginsdk.widgets.settings.WidgetSettingsSection;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.listener.KoiEventListener;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class CaffeinatedYoutubePlugin implements KoiEventListener {
    public static final WidgetDetails WIDGET_DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.youtube.widget")
        .withIcon("youtube")
        .withType(WidgetType.WIDGET)
        .withCategory(WidgetDetailsCategory.INTERACTION)
        .withFriendlyName("Youtube Widget");

    public static final WidgetDetails DOCK_DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.youtube")
        .withIcon("youtube")
        .withType(WidgetType.DOCK)
        .withFriendlyName("Youtube Queue");

    private static final WidgetSettingsLayout WIDGET_LAYOUT = new WidgetSettingsLayout()
        .addSection(
            new WidgetSettingsSection("appearance", "Appearance")
                .addItem(WidgetSettingsItem.asColor("bar_color", "Progress Bar Color", "#7a7a7a"))
                .addItem(WidgetSettingsItem.asColor("background_color", "Background Color", "#202020"))
        )
        .addSection(
            new WidgetSettingsSection("player", "Player")
                .addItem(WidgetSettingsItem.asRange("volume", "Volume", .5, .01, 0, 1))
                .addItem(WidgetSettingsItem.asCheckbox("video_only", "Video Only", false))
        );

    private static CaffeinatedDefaultPlugin plugin;

    private static YoutubeDock dock;

    static void init(CaffeinatedDefaultPlugin pl) {
        plugin = pl;

//        plugin.getPlugins().registerWidget(pl, WIDGET_DETAILS, YoutubeWidget.class);
//        plugin.getPlugins().registerWidget(pl, DOCK_DETAILS, YoutubeDock.class);

//        plugin.addKoiListener(new CaffeinatedYoutubePlugin());
    }

    private static void updateVolume(double volume) {
        dock.settings().set("volume", volume);
        dock.broadcastToAll("update", dock.settings().getJson());

        for (Widget widget : plugin.getWidgets()) {
            if (widget.getNamespace().equals(WIDGET_DETAILS.getNamespace())) {
                widget.settings().set("player.volume", volume);
                widget.broadcastToAll("update", widget.settings().getJson());
            }
        }
    }

    private static boolean isActive() {
        if (!dock.getWidgetInstances().isEmpty()) {
            return true;
        }

        // Search for an active youtube widget.
        for (Widget w : plugin.getWidgets()) {
            if (w.getNamespace().equals(WIDGET_DETAILS.getNamespace())) {
                if (!w.getWidgetInstances().isEmpty()) {
                    return true;
                }
            }
        }

        return false;

    }

    @KoiEventHandler
    public void onChat(RichMessageEvent e) {
        try {
            String result = WebUtil.sendHttpRequest(
                new Request.Builder()
                    .url("https://www.youtube.com/oembed?format=json&url=" + WebUtil.escapeHtml(e.raw))
            );

            if (!result.equals("Not Found")) {
                dock.addToQueue(Rson.DEFAULT.fromJson(result, JsonObject.class));
            }
        } catch (IOException ignored) {}
    }

    private static void broadcastAll(String type, JsonElement d) {
        dock.broadcastToAll(type, d);

        for (Widget w : plugin.getWidgets()) {
            if (w.getNamespace().equals(WIDGET_DETAILS.getNamespace())) {
                w.broadcastToAll(type, d);
            }
        }
    }

    public static class YoutubeWidget extends Widget {

        @Override
        public void onInit() {
            this.setSettingsLayout(WIDGET_LAYOUT);
        }

        @Override
        public void onNewInstance(@NonNull WidgetInstance instance) {
            instance.on("seek", (timestamp) -> {
                broadcastAll("seek", timestamp);
            });

            instance.on("play-pause", (state) -> {
                broadcastAll("play-pause", state);
            });

            instance.on("play-start", (id) -> {
                dock.onPlayStart(id.getAsNumber().intValue());
            });

            instance.on("play-end", (id) -> {
                dock.onPlayEnd(id.getAsNumber().intValue());
            });
        }

        @Override
        public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
            if (mode == WidgetInstanceMode.DOCK) {
                return "/youtube-dock.html";
            } else {
                return "/youtube.html";
            }
        }

    }

    public static class YoutubeDock extends YoutubeWidget /* We want the widget code */ {
        private List<JsonObject> queue = new ArrayList<>();
        private boolean isPlaying = false;
        private boolean allowAutoplay = false;

        private int currentPlaybackId = -1;

        void addToQueue(JsonObject video) {
            video.put("index_id", UUID.randomUUID().toString());

            this.queue.add(video);
            this.save();

            if (!this.isPlaying && this.allowAutoplay && isActive() && (this.queue.size() == 1)) {
                this.play(0);
                plugin.getLogger().debug("Attempting to autoplay: %s", video);
            } else {
                plugin.getLogger().debug("Queued: %s", video);
            }
        }

        void onPlayStart(int playbackId) {
            if (this.currentPlaybackId == playbackId) {
                this.isPlaying = true;
            }
        }

        void onPlayEnd(int playbackId) {
            // Make sure we don't get duplicate calls.
            if (this.currentPlaybackId == playbackId) {
                this.currentPlaybackId = ThreadLocalRandom.current().nextInt();

                this.isPlaying = false;

                FastLogger.logStatic("Finished video, autoPlay: %s", this.allowAutoplay);

                if (this.allowAutoplay && isActive() && !this.queue.isEmpty()) {
                    this.play(0);
                }
            }
        }

        void play(int idx) {
            JsonObject video = null;

            // Pop everything before idx off.
            // Basically gets the value at [idx].
            {
                int it = idx;
                do {
                    video = this.queue.remove(it);
                    it--;
                } while (it > 0);
            }

            this.currentPlaybackId = ThreadLocalRandom.current().nextInt();

            broadcastAll(
                "play",
                new JsonObject()
                    .put("video", video)
                    .put("currentPlaybackId", this.currentPlaybackId)
            );
            this.save();
        }

        void remove(String id) {
            JsonObject target = null;

            for (JsonObject jo : this.queue) {
                if (jo.containsKey("index_id") && jo.getString("index_id").equals(id)) {
                    target = jo;
                    break;
                }
            }

            if (target != null) {
                this.queue.remove(target);
                this.save();
            }
        }

        private void save() {
            this.settings().set("queue", Rson.DEFAULT.toJson(this.queue));
            this.settings().set("autoplay", this.allowAutoplay);
            this.broadcastToAll("update", this.settings().getJson());
        }

        @SneakyThrows
        @Override
        public void onInit() {
            // DO NOT CALL SUPER!

            dock = this;

            try {
                this.queue = Rson.DEFAULT.fromJson(this.settings().get("queue"), new TypeToken<List<JsonObject>>() {
                });
            } catch (Exception ignored) {}

            if (!this.settings().has("volume")) {
                this.settings().set("volume", .5);
            }

            this.settings().set("shouldMute", false);

            this.allowAutoplay = this.settings().getBoolean("autoplay", true);

            // NEVER do this.
            WidgetHandle handle = ReflectionLib.getValue(this, "$handle");
            String newFormat;

            if (CaffeinatedPlugin.isDevEnvironment()) {
                newFormat = "http://localhost:3001";
            } else {
                newFormat = "https://studio.casterlabs.co";
            }

            newFormat += "/popout/youtube-queue?pluginId=%s&widgetId=%s&authorization=%s&port=%d&mode=%s";

            ReflectionLib.setValue(handle, "urlFormat", newFormat);
        }

        @Override
        public void onNewInstance(@NonNull WidgetInstance instance) {
            super.onNewInstance(instance);

            instance.on("queue", (e) -> {
                try {
                    String result = WebUtil.sendHttpRequest(
                        new Request.Builder()
                            .url("https://www.youtube.com/oembed?format=json&url=" + WebUtil.escapeHtml(e.getAsString()))
                    );

                    if (!result.equals("Not Found")) {
                        dock.addToQueue(Rson.DEFAULT.fromJson(result, JsonObject.class));
                    }
                } catch (IOException ignored) {}
            });

            instance.on("play", (idx) -> {
                this.play(idx.getAsNumber().intValue());
                throw new RuntimeException(); // ??????
            });

            instance.on("remove", (id) -> {
                this.remove(id.getAsString());
            });

            instance.on("autoplay", (e) -> {
                this.allowAutoplay = e.getAsBoolean();
                this.save();
            });

            instance.on("clear", () -> {
                this.queue.clear();
                this.save();
            });

            instance.on("volume-update", (e) -> {
                double volume = e.getAsNumber().doubleValue();

                updateVolume(volume);
            });
        }

    }

}
