package co.casterlabs.caffeinated.app.builtins;

import java.io.IOException;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.commons.functional.tuples.Pair;
import lombok.AllArgsConstructor;
import lombok.NonNull;

class _UIDocksPlugin extends CaffeinatedPlugin {

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
                    return new DockBase("/docks/chat");

                case "co.casterlabs.dock.viewers":
                    return new DockBase("/docks/viewers");

                case "co.casterlabs.dock.channel_info":
                    return new DockBase("/docks/channel-info");

                case "co.casterlabs.dock.activity_feed":
                    return new DockBase("/docks/activity-feed");

                default:
                    return null; // Shut up Mr. Compiley
            }
        };

        Caffeinated.getInstance().getPlugins().registerWidgetFactory(this, STREAM_CHAT_DETAILS, factory);
        Caffeinated.getInstance().getPlugins().registerWidgetFactory(this, VIEWERS_DETAILS, factory);
//        Caffeinated.getInstance().getPlugins().registerWidgetFactory(this, CHANNEL_INFO_DETAILS, factory);
        Caffeinated.getInstance().getPlugins().registerWidgetFactory(this, ACTIVITY_FEED_DETAILS, factory);
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
        return BuiltIns.resolveUIFile(resource);
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

            instance.on("copyText", (e) -> {
                String text = e.getAsString();
                Caffeinated.getInstance().copyText(text, null);
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
