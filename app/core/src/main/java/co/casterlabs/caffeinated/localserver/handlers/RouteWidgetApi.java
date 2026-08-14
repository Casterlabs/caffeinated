package co.casterlabs.caffeinated.localserver.handlers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import co.casterlabs.caffeinated.app.plugins.CaffeinatedPluginsImpl;
import co.casterlabs.caffeinated.app.util.Resources;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeHeartbeatListener;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeWidgetListener;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginResource;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rhs.HttpMethod;
import co.casterlabs.rhs.HttpStatus.StandardHttpStatus;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointData;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointProvider;
import co.casterlabs.rhs.protocol.api.endpoints.HttpEndpoint;
import co.casterlabs.rhs.protocol.api.endpoints.WebsocketEndpoint;
import co.casterlabs.rhs.protocol.http.HttpResponse;
import co.casterlabs.rhs.protocol.http.HttpSession;
import co.casterlabs.rhs.protocol.websocket.WebsocketResponse;
import co.casterlabs.rhs.protocol.websocket.WebsocketSession;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class RouteWidgetApi implements EndpointProvider {
    private static final String BASE_URL_REPLACE = "/$caffeinated-sdk-root$";
    private static final String ESCAPED_BASE_URL_REPLACE = "/$\\caffeinated-sdk-root$";

    @HttpEndpoint(path = "/api/plugin/widget/loader.*", allowedMethods = {
            HttpMethod.GET
    })
    public HttpResponse onGetWidgetLoaderRequest(HttpSession session, EndpointData<Void> data) {
        try {
            String authorization = session.uri().query.getSingle("authorization");
            String pluginId = session.uri().query.getSingle("pluginId");
            String widgetId = session.uri().query.getSingle("widgetId");
            WidgetInstanceMode mode = WidgetInstanceMode.valueOf(
                session
                    .uri().query
                        .getSingleOrDefault("mode", "WIDGET")
                        .toUpperCase()
            );

            CaffeinatedPlugin plugin = CaffeinatedPluginsImpl.INSTANCE.getPluginById(pluginId);
            if (plugin == null) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            Widget widget = null;
            for (Widget w : plugin.getWidgets()) {
                if (w.getId().equals(widgetId)) {
                    widget = w;
                }
            }
            if (widget == null) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
            }

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.TEMPORARY_REDIRECT)
                .header("Location", String.format("/api/plugin/%s/%s/html%s?%s", pluginId, authorization, widget.getWidgetBasePath(mode), session.uri().query.raw))
                .header("Access-Control-Allow-Origin", "*")
                .header("Cross-Origin-Resource-Policy", "cross-origin");
        } catch (Exception e) {
            e.printStackTrace();
            return RouteHelper.newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @HttpEndpoint(path = "/api/plugin/:pluginId/:authorization/html.*", allowedMethods = {
            HttpMethod.GET
    })
    public HttpResponse onGetWidgetHtmlRequest(HttpSession session, EndpointData<Void> data) {
        try {
            String pluginId = data.uriParameters().get("pluginId");

            String[] urlParts = session.uri().path.split("/html", 2);
            String trueBaseUrl = urlParts[0].concat("/html");
            String resource = urlParts[1];

            if (resource.length() == 0) {
                resource = "/";
            }

            CaffeinatedPlugin plugin = CaffeinatedPluginsImpl.INSTANCE.getPluginById(pluginId);
            if (plugin == null) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            PluginResource response;

            boolean isInternalPlugin = "co.casterlabs.uidocks".equals(pluginId) || pluginId.startsWith("co.casterlabs.thirdparty");
            boolean isDefaultWidgetPlugin = "co.casterlabs.defaultwidgets".equals(pluginId);

            if (CaffeinatedPlugin.isDevEnvironment() && (isInternalPlugin || isDefaultWidgetPlugin)) {
                // Avoid CORS issues.
                String url;
                if (isInternalPlugin) {
                    url = "http://localhost:3000/$caffeinated-sdk-root$" + resource;
                } else {
                    url = "http://localhost:3002/$caffeinated-sdk-root$" + resource;
                }

                Pair<byte[], String> result = WebUtil.sendHttpRequestBytesWithMime(
                    new Request.Builder()
                        .url(url)
                );

                response = PluginResource.of(result.a(), result.b());
            } else {
                response = plugin.resolveResource(resource);
            }

            if (response == null) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
            }

            byte[] responseData = response.data;
            String responseMime = response.mimeType;

            if (responseMime.startsWith("text/") || responseMime.startsWith("application/javascript") || responseMime.startsWith("application/json")) {
                String textContent = new String(response.data, StandardCharsets.UTF_8);

                textContent = textContent
                    .replace(BASE_URL_REPLACE, trueBaseUrl)
                    .replace(ESCAPED_BASE_URL_REPLACE, BASE_URL_REPLACE);

                if (session.uri().query.containsKey("authorization")) {
                    // Inject the environment.
                    int htmlStartIndex = textContent.toLowerCase().indexOf("<html");
                    if (htmlStartIndex != -1) {
                        int htmlEndIndex = textContent.substring(htmlStartIndex).indexOf('>') + htmlStartIndex + 1;

                        String tagsToInject = String.format("<script>\n%s\n</script>", Resources.string("widget-environment.js"));

                        textContent = textContent.substring(0, htmlEndIndex) +
                            tagsToInject +
                            textContent.substring(htmlEndIndex);
                    }
                }

                responseData = textContent.getBytes(StandardCharsets.UTF_8);
            }

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, responseData)
                .mime(responseMime)
                .header("Access-Control-Allow-Origin", "*")
                .header("Cross-Origin-Resource-Policy", "cross-origin");
        } catch (Exception e) {
            e.printStackTrace();
            return RouteHelper.newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @WebsocketEndpoint(path = "/api/plugin/:pluginId/widget/:widgetId/realtime/heartbeat")
    public WebsocketResponse onWidgetRealtimeConnectionHeartBeat(WebsocketSession session, EndpointData<Void> data) {
        try {
            if (!RouteHelper.authorize(session)) {
                return WebsocketResponse.reject(StandardHttpStatus.UNAUTHORIZED);
            }

            String pluginId = data.uriParameters().get("pluginId");
            String widgetId = data.uriParameters().get("widgetId");

            CaffeinatedPlugin owningPlugin = CaffeinatedPluginsImpl.INSTANCE.getPluginById(pluginId);

            if (owningPlugin == null) {
                return WebsocketResponse.reject(StandardHttpStatus.NOT_FOUND);
            }

            Widget widget = null;

            for (Widget w : owningPlugin.getWidgets()) {
                if (w.getId().equals(widgetId)) {
                    widget = w;
                }
            }

            if (widget == null) {
                return WebsocketResponse.reject(StandardHttpStatus.NOT_FOUND);
            } else {
                // Connect.
                return WebsocketResponse.accept(
                    new RealtimeHeartbeatListener(widget, UUID.randomUUID().toString()),
                    session.firstProtocol()
                );
            }
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Failed to handle websocket connection.", e);
            return WebsocketResponse.reject(StandardHttpStatus.INTERNAL_ERROR);
        }
    }

    @WebsocketEndpoint(path = "/api/plugin/:pluginId/widget/:widgetId/realtime")
    public WebsocketResponse onWidgetRealtimeConnection(WebsocketSession session, EndpointData<Void> data) {
        try {
            if (!RouteHelper.authorize(session)) {
                return WebsocketResponse.reject(StandardHttpStatus.UNAUTHORIZED);
            }

            String pluginId = data.uriParameters().get("pluginId");
            String widgetId = data.uriParameters().get("widgetId");
            WidgetInstanceMode mode = WidgetInstanceMode.valueOf(
                session
                    .uri().query
                        .getSingleOrDefault("mode", "WIDGET")
                        .toUpperCase()
            );

            CaffeinatedPlugin owningPlugin = CaffeinatedPluginsImpl.INSTANCE.getPluginById(pluginId);
            if (owningPlugin == null) {
                return WebsocketResponse.reject(StandardHttpStatus.NOT_FOUND);
            }

            Widget widget = null;
            for (Widget w : owningPlugin.getWidgets()) {
                if (w.getId().equals(widgetId)) {
                    widget = w;
                }
            }

            if (widget == null) {
                return WebsocketResponse.reject(StandardHttpStatus.NOT_FOUND);
            } else {
                // Connect.
                return WebsocketResponse.accept(
                    new RealtimeWidgetListener(widget, mode, UUID.randomUUID().toString()),
                    session.firstProtocol()
                );
            }
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Failed to handle websocket connection.", e);
            return WebsocketResponse.reject(StandardHttpStatus.INTERNAL_ERROR);
        }
    }

}
