package co.casterlabs.caffeinated.localserver.handlers;

import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeWidgetListener;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
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

    @HttpEndpoint(uri = "/api/plugin/:pluginId/widget/:widgetId/webappurl")
    public HttpResponse onGetWidgetWebAppRequest(SoraHttpSession session) {
        try {
            if (authorize(session)) {
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
                    return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
                }

                for (Widget widget : owningPlugin.getWidgets()) {
                    if (widget.getId().equals(widgetId)) {
                        // http://password.127-0-0-1.sslip.io ->
                        // http://mode.plugin.widget.password.127-0-0-1.sslip.io
                        String url = String.format(
                            "http://%s.%s.%s.%s/cl-cgi/widget.html",
                            mode.name().toLowerCase(),
                            pluginId.replace(".", "-"),
                            widgetId,
                            CaffeinatedApp.getInstance().getInternalAppUrl().substring("http://".length()) // http://password.127-0-0-1.sslip.io -> password.127-0-0-1.sslip.io
                        );

                        return this.addCors(
                            session,
                            newResponse(StandardHttpStatus.OK, JsonObject.singleton("url", url))
                        );
                    }
                }

                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
            } else {
                return newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @WebsocketEndpoint(uri = "/api/plugin/:pluginId/widget/:widgetId/realtime")
    public WebsocketListener onWidgetRealtimeConnection(SoraWebsocketSession session) {
        try {
            if (authorize(session)) {
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
            } else {
                return newWebsocketErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newWebsocketErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @HttpEndpoint(uri = "/api/plugin/:pluginId/widgets")
    public HttpResponse onGetWidgetsRequest(SoraHttpSession session) {
        try {
            if (authorize(session)) {
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
            } else {
                return newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

}
