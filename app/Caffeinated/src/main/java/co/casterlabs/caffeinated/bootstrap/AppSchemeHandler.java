package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.saucer.scheme.SaucerSchemeHandler;
import co.casterlabs.saucer.scheme.SaucerSchemeRequest;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse;
import co.casterlabs.saucer.scheme.SaucerSchemeResponse.SaucerRequestError;
import co.casterlabs.saucer.utils.SaucerStash;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppSchemeHandler implements SaucerSchemeHandler {

    @SneakyThrows
    @Override
    public SaucerSchemeResponse handle(SaucerSchemeRequest request) throws Throwable {
        String path = request.uri().getPath();

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
            byte[] content = FileUtil.loadResourceBytes("co/casterlabs/caffeinated/app/ui/html" + path);
            String mimeType = "application/octet-stream";

            String[] split = path.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.uri(), path, mimeType);

            return SaucerSchemeResponse.success(SaucerStash.of(content), mimeType);
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s", request.uri(), path);

            return SaucerSchemeResponse.error(SaucerRequestError.NOT_FOUND);
        }
    }

}
