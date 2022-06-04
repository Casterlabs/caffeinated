package co.casterlabs.caffeinated.pluginsdk.ui;

import java.io.IOException;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.kaimen.util.functional.ConsumingProducer;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.MimeTypes;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

/**
 * This handler will automatically serve resources out of a `basePath` from your
 * plugin's class loader.
 */
public class CaffeinatedWebAppHandler implements ConsumingProducer<HttpSession, HttpResponse> {
    private final CaffeinatedPlugin plugin;
    private final String basePath;
    private final FastLogger logger;

    public CaffeinatedWebAppHandler(@NonNull CaffeinatedPlugin plugin, @NonNull String basePath) {
        this.plugin = plugin;
        this.basePath = basePath;
        this.logger = new FastLogger(String.format("[WebAppHandler for %s: %s]", this.plugin.getName(), basePath));
    }

    @Override
    public @NonNull HttpResponse produce(@NonNull HttpSession request) throws InterruptedException {
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
            byte[] content = IOUtil.readInputStreamBytes(this.plugin.getClassLoader().getResourceAsStream(this.basePath + uri));
            String mimeType = "application/octet-stream";

            String[] split = uri.split("\\.");
            if (split.length > 1) {
                mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
            }

            this.logger.debug("200 %s -> %s%s (%s)", request.getUri(), this.basePath, uri, mimeType);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                .setMimeType(mimeType);
        } catch (IOException e) {
            this.logger.severe("404 %s -> %s%s", this.basePath, request.getUri(), uri);

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND, "")
                .setMimeType("application/octet-stream");
        }
    }

}
