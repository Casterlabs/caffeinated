package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.kaimen.util.functional.ConsumingProducer;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.MimeTypes;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppSchemeHandler implements ConsumingProducer<HttpSession, HttpResponse>, RouteHelper {

    @Override
    public HttpResponse produce(HttpSession session) throws InterruptedException {
        // Either:
        // http://mode.plugin.widget.password.127-0-0-1.sslip.io:1234
        // or:
        // http://password.127-0-0-1.sslip.io:1234
        String[] host = session.getHost().split("\\.");

        if (host.length == 7) {
            return this.onWidgetWebAppRequest(host, session);
        } else {
            return this.onAppContentRequest(session);
        }
    }

    private HttpResponse onAppContentRequest(HttpSession session) {
        String uri = session.getUri();

        // Append `index.html` to the end when required.
        if (!uri.contains(".")) {
            if (uri.endsWith("/")) {
                uri += "index.html";
            } else {
                uri += "/index.html";
            }
        }

        try {
            byte[] content = FileUtil.loadResourceBytes("app" + uri);
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", session.getUri(), uri, mimeType);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mimeType);
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s", session.getUri(), uri);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND, "")
                .setMimeType("application/octet-stream");
        }
    }

    private HttpResponse onWidgetWebAppRequest(String[] host, HttpSession session) {
        try {
            WidgetInstanceMode mode = WidgetInstanceMode.valueOf(host[0].toUpperCase());
            String pluginId = host[1].replace("-", ".");
            String widgetId = host[2];

            CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPlugins().getPlugins().getPluginById(pluginId);

            if (owningPlugin == null) {
                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            for (Widget widget : owningPlugin.getWidgets()) {
                if (widget.getId().equals(widgetId)) {
                    if (session.getUri().equals("/blank.html")) {
                        return this.addCors(
                            session,
                            HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK)
                                .setMimeType("text.html")
                        );
                    } else if (session.getUri().startsWith("/cl-cgi/")) {
                        return onCGIRequest(session);
                    } else {
                        @SuppressWarnings("deprecation")
                        HttpResponse response = widget.handle(mode, session);

                        return this.addCors(session, response);
                    }
                }
            }

            return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    private static @NonNull HttpResponse onCGIRequest(@NonNull HttpSession session) throws InterruptedException {
        String uri = session.getUri();

        // Append `index.html` to the end when required.
        if (!uri.contains(".")) {
            if (uri.endsWith("/")) {
                uri += "index.html";
            } else {
                uri += "/index.html";
            }
        }

        try {
            byte[] content = FileUtil.loadResourceBytes(uri);
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> %s (%s)", session.getUri(), uri, mimeType);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mimeType);
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> %s", session.getUri(), uri);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND, "")
                .setMimeType("application/octet-stream");
        }
    }

}
