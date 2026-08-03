package co.casterlabs.caffeinated.window.saucer;

import app.saucer.webview.scheme.SaucerSchemeHandler;
import app.saucer.webview.scheme.SaucerSchemeRequest;
import app.saucer.webview.scheme.SaucerSchemeResponse;
import co.casterlabs.caffeinated.app.util.Resources;
import co.casterlabs.caffeinated.util.MimeTypes;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

class _SchemeHandler implements SaucerSchemeHandler {
    public static final _SchemeHandler INSTANCE = new _SchemeHandler();

    @SneakyThrows
    @Override
    public SaucerSchemeResponse handle(SaucerSchemeRequest request) throws Throwable {
        String path = request.url().path()
            .replace('\\', '/')
            .replace("%5c", "/")
            .replace("%5C", "/");

        if (path.startsWith("/$caffeinated-sdk-root$")) {
            path = path.substring("/$caffeinated-sdk-root$".length());
        }

        if (path.isEmpty()) {
            path = "/index.html";
        } else {
            // Append `index.html` to the end when required.
            if (!path.contains(".")) {
                if (path.endsWith("/")) {
                    path += "index.html";
                } else {
                    path += ".html";
                }
            }
        }

        try {
            byte[] content = Resources.bytes("co/casterlabs/caffeinated/app/ui/html" + path);
            String mimeType = "application/octet-stream";

            String[] split = path.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.url(), path, mimeType);

            return SaucerSchemeResponse.create(content, mimeType)
                .status(200);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s\n%s", request.url(), path, e);
            return SaucerSchemeResponse.create("Not found".getBytes(), "text/plain")
                .status(404);
        }
    }

}
