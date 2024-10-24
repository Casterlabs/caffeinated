package co.casterlabs.caffeinated.localserver.handlers;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.bootstrap.FileUtil;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeHeartbeatListener;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeWidgetListener;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.rhs.session.WebsocketListener;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;
import okhttp3.Request;

public class RouteWidgetApi implements HttpProvider, WebsocketProvider, RouteHelper {
    private static final String BASE_URL_REPLACE = "/$caffeinated-sdk-root$";
    private static final String ESCAPED_BASE_URL_REPLACE = "/$\\caffeinated-sdk-root$";

    @HttpEndpoint(uri = "/api/plugin/widget/loader.*")
    public HttpResponse onGetWidgetLoaderRequest(SoraHttpSession session) {
        try {
            String authorization = session.getQueryParameters().get("authorization");
            String pluginId = session.getQueryParameters().get("pluginId");
            String widgetId = session.getQueryParameters().get("widgetId");
            WidgetInstanceMode mode = WidgetInstanceMode.valueOf(
                session
                    .getQueryParameters()
                    .getOrDefault("mode", "WIDGET")
                    .toUpperCase()
            );

            CaffeinatedPlugin plugin = CaffeinatedApp.getInstance().getPluginIntegration().getPlugins().getPluginById(pluginId);
            if (plugin == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            Widget widget = null;
            for (Widget w : plugin.getWidgets()) {
                if (w.getId().equals(widgetId)) {
                    widget = w;
                }
            }
            if (widget == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
            }

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.TEMPORARY_REDIRECT)
                .putHeader("Location", String.format("/api/plugin/%s/%s/html%s%s", pluginId, authorization, widget.getWidgetBasePath(mode), session.getQueryString()))
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Cross-Origin-Resource-Policy", "cross-origin");
        } catch (Exception e) {
            e.printStackTrace();
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @HttpEndpoint(uri = "/api/plugin/:pluginId/:authorization/html.*")
    public HttpResponse onGetWidgetHtmlRequest(SoraHttpSession session) {
        try {
            String pluginId = session.getUriParameters().get("pluginId");

            String[] urlParts = session.getUri().split("/html", 2);
            String trueBaseUrl = urlParts[0].concat("/html");
            String resource = urlParts[1];

            if (resource.length() == 0) {
                resource = "/";
            }

            CaffeinatedPlugin plugin = CaffeinatedApp.getInstance().getPluginIntegration().getPlugins().getPluginById(pluginId);
            if (plugin == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            Pair<String, String> response;
            if (CaffeinatedPlugin.isDevEnvironment() &&
                ("co.casterlabs.uidocks".equals(pluginId) || "co.casterlabs.defaultwidgets".equals(pluginId))) {
                // Avoid CORS issues.
                String url;
                if ("co.casterlabs.uidocks".equals(pluginId)) {
                    url = "http://localhost:3000/$caffeinated-sdk-root$" + resource;
                } else {
                    url = "http://localhost:3002/$caffeinated-sdk-root$" + resource;
                }

                Pair<byte[], String> result = WebUtil.sendHttpRequestBytesWithMime(
                    new Request.Builder()
                        .url(url)
                );

                response = new Pair<>(new String(result.a(), StandardCharsets.UTF_8), result.b());
            } else {
                response = plugin.getResource(resource);
            }

            if (response == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
            }

            String content = response.a()
                .replace(BASE_URL_REPLACE, trueBaseUrl)
                .replace(ESCAPED_BASE_URL_REPLACE, BASE_URL_REPLACE);

            String mime = response.b();
            if (mime == null) {
                mime = "application/octet-stream";
            }

            if (session.getQueryParameters().containsKey("authorization")) {
                // Inject the environment.
                int htmlStartIndex = content.indexOf("<html");
                int htmlEndIndex = content.substring(htmlStartIndex).indexOf('>') + htmlStartIndex + 1;

                String tagsToInject = String.format("<script type=module>\n%s\n</script>", FileUtil.loadResource("/widget-environment.js"));
                if (CaffeinatedPlugin.isDevEnvironment()) {
                    // https://github.com/liriliri/chii
                    tagsToInject += "<script src=\"//chii.liriliri.io/playground/target.js\"></script>";
                }

                content = content.substring(0, htmlEndIndex) +
                    tagsToInject +
                    content.substring(htmlEndIndex);
            }

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mime)
                .putHeader("Access-Control-Allow-Origin", "*")
                .putHeader("Cross-Origin-Resource-Policy", "cross-origin");
        } catch (Exception e) {
            e.printStackTrace();
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @WebsocketEndpoint(uri = "/api/plugin/:pluginId/widget/:widgetId/realtime/heartbeat")
    public WebsocketListener onWidgetRealtimeConnectionHeartBeat(SoraWebsocketSession session) {
        try {
            if (!authorize(session)) {
                return newWebsocketErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }

            String pluginId = session.getUriParameters().get("pluginId");
            String widgetId = session.getUriParameters().get("widgetId");

            CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPluginIntegration().getPlugins().getPluginById(pluginId);

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
                return new RealtimeHeartbeatListener(widget, UUID.randomUUID().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return newWebsocketErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
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

            CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPluginIntegration().getPlugins().getPluginById(pluginId);
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

}
