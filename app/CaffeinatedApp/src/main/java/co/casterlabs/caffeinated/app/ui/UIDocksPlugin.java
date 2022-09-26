package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

// This is so nasty, I love it.
public class UIDocksPlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        this.getPlugins().registerWidget(this, StreamChatDock.DETAILS, StreamChatDock.class);
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

    public static class StreamChatDock extends Widget {
        public static final WidgetDetails DETAILS = new WidgetDetails()
            .withNamespace("co.casterlabs.dock.stream_chat")
            .withIcon("chat-bubble-left")
            .withType(WidgetType.DOCK)
            .withFriendlyName("Stream Chat");

        @SneakyThrows
        @Override
        public void onInit() {
            // NEVER do this.
            WidgetHandle handle = ReflectionLib.getValue(this, "$handle");
            String newFormat;

            if (CaffeinatedApp.getInstance().isDev()) {
                newFormat = "http://localhost:3001";
            } else {
                newFormat = "https://studio.casterlabs.co";
            }

            newFormat += "/popout/stream-chat?pluginId=%s&widgetId=%s&authorization=%s&port=%d&mode=%s";

            ReflectionLib.setValue(handle, "urlFormat", newFormat);
        }

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

    }

}
