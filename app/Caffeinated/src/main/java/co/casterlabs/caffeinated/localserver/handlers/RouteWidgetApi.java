package co.casterlabs.caffeinated.localserver.handlers;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeHeartbeatListener;
import co.casterlabs.caffeinated.localserver.websocket.RealtimeWidgetListener;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.io.streams.StreamUtil;
import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.rhs.session.WebsocketListener;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import co.casterlabs.sora.api.websockets.SoraWebsocketSession;
import co.casterlabs.sora.api.websockets.WebsocketProvider;
import co.casterlabs.sora.api.websockets.annotations.WebsocketEndpoint;

public class RouteWidgetApi implements HttpProvider, WebsocketProvider, RouteHelper {
    private static final String BASE_URL_REPLACE = "/$caffeinated-sdk-root$";
    private static final String ESCAPED_BASE_URL_REPLACE = "/$\\caffeinated-sdk-root$";

    @HttpEndpoint(uri = "/api/plugin/widget/loader.*")
    public HttpResponse onGetWidgetLoaderRequest(SoraHttpSession session) {
        try {
            String resource = session.getUri().split("/loader", 2)[1];

            if (CaffeinatedPlugin.isDevEnvironment()) {
                // Avoid CORS issues.
                String url;
                if ("co.casterlabs.uidocks".equals(session.getQueryParameters().get("pluginId"))) {
                    url = "http://localhost:3000/$caffeinated-sdk-root$/loader" + resource + session.getQueryString();
                } else {
                    url = "http://localhost:3002/$caffeinated-sdk-root$/loader" + resource + session.getQueryString();
                }

                return HttpResponse.newFixedLengthResponse(
                    StandardHttpStatus.OK,
                    "<!DOCTYPE html>\r\n"
                        + "<html>\r\n"
                        + "<head>\r\n"
                        + "<meta http-equiv=\"refresh\" content=\"0; url='" + url + "'\" />\r\n"
                        + "</head>\r\n"
                        + "</html>"
                )
                    .setMimeType("text/html")
                    .putHeader("Access-Control-Allow-Origin", "https://widgets.casterlabs.co")
                    .putHeader("Cross-Origin-Resource-Policy", "cross-origin");
            }

            InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream("co/casterlabs/caffeinated/app/ui/html/loader" + resource);

            String content = StreamUtil.toString(in, StandardCharsets.UTF_8);
            String mime = MimeTypes.getMimeForFile(new File(resource));

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mime)
                .putHeader("Access-Control-Allow-Origin", "https://widgets.casterlabs.co")
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

            Pair<String, String> response = plugin.getResource(resource);

            if (response == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
            } else {
                String content = response.a()
                    .replace(BASE_URL_REPLACE, trueBaseUrl)
                    .replace(ESCAPED_BASE_URL_REPLACE, BASE_URL_REPLACE);

                String mime = response.b();
                if (mime == null) {
                    mime = "application/octet-stream";
                }

                return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                    .setMimeType(mime)
//                    .putHeader("Content-Security-Policy", "frame-ancestors 'self' casterlabs.co widgets.casterlabs.co;")
                    .putHeader("Access-Control-Allow-Origin", "https://widgets.casterlabs.co")
                    .putHeader("Cross-Origin-Resource-Policy", "cross-origin");
            }
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
