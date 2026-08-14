package co.casterlabs.caffeinated.localserver;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rhs.HttpStatus;
import co.casterlabs.rhs.protocol.http.HttpResponse;
import co.casterlabs.rhs.protocol.http.HttpSession;
import co.casterlabs.rhs.protocol.websocket.Websocket;
import co.casterlabs.rhs.protocol.websocket.WebsocketSession;

public class RouteHelper {

    /* -------------------- */
    /* Util                 */
    /* -------------------- */

    public static boolean authorize(HttpSession session) {
        String auth = session.uri().query.getSingleOrDefault(
            "authorization",
            session.uri().query.getSingle("authorization")
        );

        if (auth == null) {
            return false;
        }

        String conductorKey = AppConfig.appPreferences.get().conductorKey;

        return auth.equals(conductorKey);
    }

    public static boolean authorize(WebsocketSession session) {
        String auth = session.uri().query.getSingleOrDefault(
            "authorization",
            session.uri().query.getSingle("authorization")
        );

        if (auth == null) {
            return false;
        }

        String conductorKey = AppConfig.appPreferences.get().conductorKey;

        return auth.equals(conductorKey);
    }

    public static HttpResponse newResponse(HttpStatus status, JsonElement jsonElement) {
        JsonObject body = new JsonObject();

        body.put("errors", new JsonArray());
        body.put("data", jsonElement);

        return HttpResponse.newFixedLengthResponse(status, body.toString())
            .mime("application/json");
    }

    public static HttpResponse newErrorResponse(HttpStatus status, RequestError error) {
        return HttpResponse.newFixedLengthResponse(status, "{\"data\":null,\"errors\":[\"" + error + "\"]}")
            .mime("application/json");
    }

    public static HttpResponse addCors(HttpResponse response) {
        return response
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, OPTIONS")
            .header("Access-Control-Allow-Headers", "Authorization, *")
            .header("Access-Control-Allow-Private-Network", "true");
    }

    public static void safeClose(@Nullable Websocket websocket) {
        if (websocket != null) {
            websocket.close();
        }
    }

}
