package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.rhs.session.HttpSession;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppSchemeHandler implements Function<HttpSession, HttpResponse> {

    @SneakyThrows
    @Override
    public @Nullable HttpResponse apply(@Nullable HttpSession request) {
        String uri = request.getUri();

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

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", request.getUri(), uri, mimeType);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mimeType);
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s", request.getUri(), uri);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND, "")
                .setMimeType("application/octet-stream");
        }
    }

}
