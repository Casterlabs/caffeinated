package co.casterlabs.caffeinated.app.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.io.IOUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class UIDocksPlugin extends CaffeinatedPlugin {

    public static final WidgetDetails STREAM_CHAT_DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.dock.stream_chat")
        .withIcon("chat-bubble-left")
        .withType(WidgetType.DOCK)
        .withFriendlyName("Stream Chat");

    public static final WidgetDetails VIEWERS_DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.dock.viewers")
        .withIcon("users")
        .withType(WidgetType.DOCK)
        .withFriendlyName("Viewers List");

    public static final WidgetDetails CHANNEL_INFO_DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.dock.channel_info")
        .withIcon("document-text")
        .withType(WidgetType.DOCK)
        .withFriendlyName("Channel Info");

    public static final WidgetDetails ACTIVITY_FEED_DETAILS = new WidgetDetails()
        .withNamespace("co.casterlabs.dock.activity_feed")
        .withIcon("list-bullet")
        .withType(WidgetType.DOCK)
        .withFriendlyName("Activity Feed");

    @Override
    public void onInit() {
        Function<WidgetDetails, Widget> factory = (details) -> {
            switch (details.getNamespace()) {
                case "co.casterlabs.dock.stream_chat":
                    return new DockBase("/popout/chat");

                case "co.casterlabs.dock.viewers":
                    return new DockBase("/popout/viewers");

                case "co.casterlabs.dock.channel_info":
                    return new DockBase("/popout/channel-info");

                case "co.casterlabs.dock.activity_feed":
                    return new DockBase("/popout/activity-feed");

                default:
                    return null; // Shut up Mr. Compiley
            }
        };

        this.getPlugins().registerWidgetFactory(this, STREAM_CHAT_DETAILS, factory);
        this.getPlugins().registerWidgetFactory(this, VIEWERS_DETAILS, factory);
        this.getPlugins().registerWidgetFactory(this, CHANNEL_INFO_DETAILS, factory);
        this.getPlugins().registerWidgetFactory(this, ACTIVITY_FEED_DETAILS, factory);
    }

    @Override
    public void onClose() {}

    @Override
    public @NonNull String getName() {
        return "Casterlabs UI Docks";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.uidocks";
    }

    @Override
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        // Append `index.html` to the end when required.
        if (!resource.contains(".")) {
            if (resource.endsWith("/")) {
                resource += "index.html";
            } else {
                resource += ".html";
            }
        }

        String mimeType = "application/octet-stream";

        String[] split = resource.split("\\.");
        if (split.length > 1) {
            mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
        }

        resource = "app" + resource; // Load from the app's actual resources.
        this.getLogger().debug("Loading resource: %s", resource);

        try (InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream(resource)) {
            return new Pair<>(
                IOUtil.readInputStreamString(in, StandardCharsets.UTF_8),
                mimeType
            );
        } catch (Exception e) {
            this.getLogger().debug("An error occurred whilst loading resource %s:\n%s", resource, e);
            return new Pair<>("", "text/plain");
        }
    }

    @AllArgsConstructor
    public static class DockBase extends Widget {
        private String basePath;

        @Override
        public void onNewInstance(@NonNull WidgetInstance instance) {
            instance.on("openLink", (e) -> {
                String url = e.getAsString();
                Caffeinated.getInstance().openLink(url);
            });

            instance.on("savePreferences", (data) -> {
                this.settings().set("preferences", data);
            });
        }

        @Override
        public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
            return this.basePath;
        }

    }

}
