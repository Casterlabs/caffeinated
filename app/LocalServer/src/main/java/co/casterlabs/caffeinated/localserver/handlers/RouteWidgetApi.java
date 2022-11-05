package co.casterlabs.caffeinated.localserver.handlers;

import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeWidgetListener;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;

public class RouteWidgetApi implements HttpProvider, WebsocketProvider, RouteHelper {
    private static final String BASE_URL_REPLACE = "/$caffeinated-sdk-root$";

    @HttpEndpoint(uri = "/api/plugin/:pluginId/widget/:widgetId/:mode/:authorization/html.*")
    public HttpResponse onGetWidgetHtmlRequest(SoraHttpSession session) {
        try {
            String pluginId = session.getUriParameters().get("pluginId");
            String widgetId = session.getUriParameters().get("widgetId");
            WidgetInstanceMode mode = WidgetInstanceMode.valueOf(session.getUriParameters().get("mode").toUpperCase());

            String[] urlParts = session.getUri().split("/html", 2);
            String trueBaseUrl = urlParts[0].concat("/html");
            String resource = urlParts[1];

            CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPlugins().getPlugins().getPluginById(pluginId);

            if (owningPlugin == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            Widget widget = null;
            // Search for the widget.
            for (Widget w : owningPlugin.getWidgets()) {
                if (w.getId().equals(widgetId)) {
                    widget = w;
                    break;
                }
            }

            if (widget == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
            }

            Pair<String, String> response = widget.getWidgetResource(mode, resource);

            if (response == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
            } else {
                String content = response.a()
                    .replace(BASE_URL_REPLACE, trueBaseUrl);

                String mime = response.b();
                if (mime == null) {
                    mime = "application/octet-stream";
                }

                return this.addCors(
                    session,
                    HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                        .setMimeType(mime)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @WebsocketEndpoint(uri = "/api/plugin/:pluginId/widget/:widgetId/realtime")
    public WebsocketListener onWidgetRealtimeConnection(SoraWebsocketSession session) {
        try {
            if (!authorize(session)) {
                return newWebsocketErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }

            String pluginId = session.getUriParameters().get("pluginId");
            String widgetId = session.getUriParameters().get("widgetId");
            WidgetInstanceMode mode = WidgetInstanceMode.valueOf(
                session
                    .getQueryParameters()
                    .getOrDefault("mode", "WIDGET")
                    .toUpperCase()
            );

            CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPlugins().getPlugins().getPluginById(pluginId);

            if (owningPlugin == null) {
                return newWebsocketErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            Widget widget = null;

            for (Widget w : owningPlugin.getWidgets()) {
                if (w.getId().equals(widgetId)) {
                    widget = w;
                }
            }

            if (widget == null) {
                return newWebsocketErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
            } else {
                // Connect.
                return new RealtimeWidgetListener(widget, mode, UUID.randomUUID().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newWebsocketErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @HttpEndpoint(uri = "/api/plugin/:pluginId/widgets")
    public HttpResponse onGetWidgetsRequest(SoraHttpSession session) {
        try {
            if (!authorize(session)) {
                return newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }

            String pluginId = session.getUriParameters().get("pluginId");

            CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPlugins().getPlugins().getPluginById(pluginId);

            if (owningPlugin == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            JsonArray widgetsJson = new JsonArray();

            for (Widget widget : owningPlugin.getWidgets()) {
                JsonObject widgetJson = Rson.DEFAULT.toJson(widget).getAsObject();

                widgetJson.remove("settings");
                widgetJson.remove("settingsLayout");

                widgetsJson.add(widgetJson);
            }

            return newResponse(StandardHttpStatus.OK, new JsonObject().put("widgets", widgetsJson));
        } catch (Exception e) {
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

}
