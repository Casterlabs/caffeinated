package co.casterlabs.caffeinated.localserver;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rhs.protocol.HttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.rhs.session.HttpSession;
import co.casterlabs.rhs.session.Websocket;
import co.casterlabs.rhs.session.WebsocketListener;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public interface RouteHelper {

    /* -------------------- */
    /* Util                 */
    /* -------------------- */

    default boolean authorize(SoraHttpSession session) {
        String auth = session.getQueryParameters().getOrDefault(
            "authorization",
            session.getUriParameters().get("authorization")
        );

        if (auth == null) {
            return false;
        }

        String conductorKey = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorKey();

        return auth.equals(conductorKey);
    }

    default boolean authorize(SoraWebsocketSession session) {
        String auth = session.getQueryParameters().getOrDefault(
            "authorization",
            session.getUriParameters().get("authorization")
        );

        if (auth == null) {
            return false;
        }

        String conductorKey = CaffeinatedApp.getInstance().getAppPreferences().get().getConductorKey();

        return auth.equals(conductorKey);
    }

    default HttpResponse newResponse(HttpStatus status, JsonElement jsonElement) {
        JsonObject body = new JsonObject();

        body.put("errors", new JsonArray());
        body.put("data", jsonElement);

        HttpResponse response = HttpResponse.newFixedLengthResponse(status, body.toString());

        response.setMimeType("application/json");

        return response;
    }

    default HttpResponse newErrorResponse(HttpStatus status, RequestError error) {
        HttpResponse response = HttpResponse.newFixedLengthResponse(status, "{\"data\":null,\"errors\":[\"" + error + "\"]}");

        response.setMimeType("application/json");

        return response;
    }

    default HttpResponse addCors(HttpSession session, HttpResponse response) {
        String originHeader = session.getHeader("Origin");

        if (originHeader != null) {
            String[] split = originHeader.split("://");
            String protocol = split[0];
            String referer = split[1].split("/")[0]; // Strip protocol and uri

            response.putHeader("Access-Control-Allow-Origin", protocol + "://" + referer);
            response.putHeader("Access-Control-Allow-Methods", LocalServer.ALLOWED_METHODS);
            response.putHeader("Access-Control-Allow-Headers", "Authorization, *");
            FastLogger.logStatic(LogLevel.DEBUG, "Set CORS headers for %s", referer);
        }

        return response;
    }

    default WebsocketListener newWebsocketErrorResponse(HttpStatus status, RequestError error) {
        JsonObject errorPayload = this.makeWebsocketErrorPayload(status, error, true);

        return new WebsocketListener() {

            @Override
            public void onOpen(Websocket websocket) {
                AsyncTask.create(() -> {
                    try {
                        websocket.send(errorPayload.toString());
                        Thread.sleep(1000);
                    } catch (IOException | InterruptedException ignored) {} finally {
                        safeClose(websocket);
                    }
                });
            }

        };
    }

    default JsonObject makeWebsocketErrorPayload(HttpStatus status, RequestError error, boolean isFatal) {
        return new JsonObject()
            .putNull("data")
            .put("errors", JsonArray.of(error.name()))
            .put("type", "ERROR")
            .put(
                "status",
                new JsonObject()
                    .put("code", status.getStatusCode())
                    .put("description", status.getDescription())
                    .put("isFatal", isFatal)
            );
    }

    default void safeClose(@Nullable Websocket websocket) {
        if (websocket != null) {
            try {
                websocket.close();
            } catch (IOException e) {
                FastLogger.logStatic(LogLevel.SEVERE, "An error occurred whilst closing a connection:");
                FastLogger.logException(e);
            }
        }
    }

}
