package co.casterlabs.caffeinated.localserver.websocket;

import java.io.IOException;

import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rhs.session.Websocket;
import co.casterlabs.rhs.session.WebsocketListener;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class RealtimeHeartbeatListener implements WebsocketListener, RouteHelper {
    private WidgetHandle handle;
    private String connectionId;

    private WidgetInstanceProvider wInstance;
    private RealtimeConnection connInstance;
    private Websocket websocket;

    @SneakyThrows
    public RealtimeHeartbeatListener(Widget widget, String connectionId) {
        this.handle = ReflectionLib.getValue(widget, "$handle");
        this.connectionId = connectionId;
    }

    @Override
    public void onOpen(Websocket websocket) {
        this.websocket = websocket;

        this.connInstance = new ConnectionWrapper();
        this.wInstance = new WidgetInstanceProvider();

        Pair<RealtimeConnection, WidgetInstance> connPair = new Pair<>(this.connInstance, this.wInstance);

        websocket.setAttachment(connPair);
        this.handle.widgetInstances.add(this.wInstance);

        this.sendMessage("INIT", new JsonObject());
    }

    @Override
    public void onText(Websocket websocket, String raw) {
        try {
            JsonObject message = Rson.DEFAULT.fromJson(raw, JsonObject.class);

            String type = message.getString("type").toUpperCase();

            switch (type) {

                case "PONG": {
                    this.connInstance.resetTimeout();
                    return;
                }

            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(Websocket websocket) {
        try {
            this.wInstance.onClose();
        } finally {
            this.handle.widgetInstances.remove(this.wInstance);
        }
    }

    @SneakyThrows
    private void sendMessage(String type, JsonObject payload) {
        String json = new JsonObject()
            .put("type", type.toUpperCase())
            .put("data", payload)
            .toString();

//        FastLogger.logStatic(LogLevel.TRACE, json);

        this.websocket.send(
            json
        );
    }

    private class ConnectionWrapper extends RealtimeConnection {

        @Override
        protected void ping() {
            sendMessage("PING", new JsonObject());
        }

        @Override
        public void close() throws IOException {
            websocket.close();
        }

    }

    private class WidgetInstanceProvider extends WidgetInstance {

        public WidgetInstanceProvider() {
            super(WidgetInstanceMode.WIDGET, connectionId, handle.widget);
        }

        /* ---------------- */
        /* Expose these     */
        /* ---------------- */

        @Deprecated
        @Override
        public void onClose() {
            super.onClose();
        }

        /* ---------------- */

        @Override
        protected void emit0(@NonNull String type, @NonNull JsonElement message) throws IOException {}

        @Override
        public @NonNull String getRemoteIpAddress() {
            return websocket.getSession().getRemoteIpAddress();
        }

        @Override
        public void close() throws IOException {
            websocket.close();
        }

        @Override
        public void onKoiEvent(@NonNull KoiEvent event) throws IOException {}

        @Override
        public void onSettingsUpdate() {}

        @Override
        public void onKoiStaticsUpdate(@NonNull JsonObject statics) throws IOException {}

        @Override
        public void onMusicUpdate(@NonNull JsonObject music) throws IOException {}

        @Override
        public void onAppearanceUpdate(@NonNull JsonObject preferences) throws IOException {}

    }

}
