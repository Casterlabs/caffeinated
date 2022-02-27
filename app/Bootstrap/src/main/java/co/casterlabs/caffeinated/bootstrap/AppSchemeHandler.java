package co.casterlabs.caffeinated.bootstrap;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.kaimen.util.functional.ConsumingProducer;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.MimeTypes;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppSchemeHandler implements ConsumingProducer<HttpSession, co.casterlabs.rakurai.io.http.HttpResponse> {

    @Override
    public @Nullable HttpResponse produce(@Nullable HttpSession request) throws InterruptedException {
        String uri = request.getUri();

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
