package co.casterlabs.caffeinated.localserver.websocket;

import java.io.IOException;

import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget.WidgetHandle;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.util.Pair;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketCloseCode;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class RealtimeWidgetListener implements WebsocketListener, RouteHelper {
    private WidgetHandle handle;
    private WidgetInstanceMode mode;
    private String connectionId;

    private WidgetInstanceProvider wInstance;
    private RealtimeConnection connInstance;
    private Websocket websocket;

    @SneakyThrows
    public RealtimeWidgetListener(Widget widget, WidgetInstanceMode mode, String connectionId) {
        this.handle = ReflectionLib.getValue(widget, "$handle");
        this.mode = mode;
        this.connectionId = connectionId;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onOpen(Websocket websocket) {
        this.websocket = websocket;

        this.connInstance = new ConnectionWrapper();
        this.wInstance = new WidgetInstanceProvider();

        Pair<RealtimeConnection, WidgetInstance> connPair = new Pair<>(this.connInstance, this.wInstance);

        websocket.setAttachment(connPair);
        this.handle.widgetInstances.add(this.wInstance);
        this.handle.widget.onNewInstance(this.wInstance);

        this.sendMessage(
            "KOI_STATICS",
            Caffeinated.getInstance().getKoi().toJson()
        );

        this.sendMessage(
            "MUSIC",
            Caffeinated.getInstance().getMusic().toJson()
        );

        this.sendMessage(
            "INIT",
            new JsonObject()
                .put("connectionId", this.connectionId)
                .put("widget", Rson.DEFAULT.toJson(this.handle))
                .put("koi", Caffeinated.getInstance().getKoi().toJson())
        );
    }

    @Override
    public void onText(Websocket websocket, String raw) {
        try {
            JsonObject message = Rson.DEFAULT.fromJson(raw, JsonObject.class);

            String type = message.getString("type").toUpperCase();

            switch (type) {

                case "READY": {
                    this.handle.widget.onNewInstance(this.wInstance);
                    return;
                }

                case "KOI": {
                    // TODO proxy koi calls.
                    return;
                }

                case "EMISSION": {
                    JsonObject data = message.getObject("data");
                    String emissionType = data.getString("type");
                    JsonElement emissionPayload = data.get("data");

                    this.wInstance.broadcast(emissionType, emissionPayload);
                    return;
                }

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
            websocket.close(WebsocketCloseCode.NORMAL);
        }

    }

    private class WidgetInstanceProvider extends WidgetInstance {

        public WidgetInstanceProvider() {
            super(mode, connectionId);
        }

        /* ---------------- */
        /* Expose these     */
        /* ---------------- */

        @Deprecated
        @Override
        public void broadcast(@NonNull String type, @NonNull JsonElement message) {
            super.broadcast(type, message);
        }

        @Deprecated
        @Override
        public void onClose() {
            super.onClose();
        }

        /* ---------------- */

        @Override
        public void emit(@NonNull String type, @NonNull JsonElement message) throws IOException {
            sendMessage(
                "EMISSION",
                new JsonObject()
                    .put("type", type)
                    .put("data", message)
            );
        }

        @Override
        public @NonNull String getRemoteIpAddress() {
            return websocket.getSession().getRemoteIpAddress();
        }

        @Override
        public void close() throws IOException {
            websocket.close(WebsocketCloseCode.NORMAL);
        }

        @Override
        public void onKoiEvent(@NonNull KoiEvent event) throws IOException {
            sendMessage("KOI", Rson.DEFAULT.toJson(event).getAsObject());
        }

        @Override
        public void onSettingsUpdate() {
            sendMessage(
                "UPDATE",
                new JsonObject()
                    .put("widget", Rson.DEFAULT.toJson(handle))
            );
        }

        @Override
        public void onKoiStaticsUpdate(@NonNull JsonObject statics) throws IOException {
            sendMessage(
                "KOI_STATICS",
                statics
            );
        }

        @Override
        public void onMusicUpdate(@NonNull JsonObject music) throws IOException {
            sendMessage(
                "MUSIC",
                music
            );
        }

    }

}
