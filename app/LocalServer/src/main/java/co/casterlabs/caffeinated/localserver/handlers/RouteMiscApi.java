package co.casterlabs.caffeinated.localserver.handlers;

import java.util.UUID;

import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeListener;
import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.session.WebsocketListener;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;

public class RouteMiscApi implements WebsocketProvider, RouteHelper {

    @WebsocketEndpoint(uri = "/api/realtime")
    public WebsocketListener onWidgetRealtimeConnection(SoraWebsocketSession session) {
        try {
            if (!authorize(session)) {
                return newWebsocketErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }

            return new RealtimeListener(UUID.randomUUID().toString());
        } catch (Exception e) {
            e.printStackTrace();
            return newWebsocketErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

}
