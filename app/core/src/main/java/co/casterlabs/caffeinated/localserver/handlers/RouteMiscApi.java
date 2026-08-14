package co.casterlabs.caffeinated.localserver.handlers;

import java.util.UUID;

import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeListener;
import co.casterlabs.rhs.HttpStatus.StandardHttpStatus;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointData;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointProvider;
import co.casterlabs.rhs.protocol.api.endpoints.WebsocketEndpoint;
import co.casterlabs.rhs.protocol.websocket.WebsocketResponse;
import co.casterlabs.rhs.protocol.websocket.WebsocketSession;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class RouteMiscApi implements EndpointProvider {

    @WebsocketEndpoint(path = "/api/realtime")
    public WebsocketResponse onWidgetRealtimeConnection(WebsocketSession session, EndpointData<Void> data) {
        try {
            if (!RouteHelper.authorize(session)) {
                return WebsocketResponse.reject(StandardHttpStatus.UNAUTHORIZED);
            }

            return WebsocketResponse.accept(
                new RealtimeListener(UUID.randomUUID().toString()),
                session.firstProtocol()
            );
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Failed to handle websocket connection.", e);
            return WebsocketResponse.reject(StandardHttpStatus.INTERNAL_ERROR);
        }
    }

}
