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
        String uri = request.uri().toString();

        if (uri.startsWith("/$caffeinated-sdk-root$")) {
            uri = uri.substring("/$caffeinated-sdk-root$".length());
        }

        if (uri.isEmpty()) {
            uri = "/index.html";
        } else {
            // Append `index.html` to the end when required.
            if (!uri.contains(".")) {
                if (uri.endsWith("/")) {
                    uri += "index.html";
                } else {
                    uri += ".html";
                }
            }
        }

        try {
            byte[] content = FileUtil.loadResourceBytes("co/casterlabs/caffeinated/app/ui/html" + uri);
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.uri(), uri, mimeType);

            return SaucerSchemeResponse.success(SaucerStash.of(content), mimeType);
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s", request.uri(), uri);

            return SaucerSchemeResponse.error(SaucerRequestError.NOT_FOUND);
        }
    }

}
