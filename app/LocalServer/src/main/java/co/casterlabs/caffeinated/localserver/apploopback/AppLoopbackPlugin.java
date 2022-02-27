package co.casterlabs.caffeinated.localserver.apploopback;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.MimeTypes;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.SoraPlugin;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

// https://bitbucket.org/chromiumembedded/java-cef/issues/365/custom-scheme-onloaderror-not-called
@AllArgsConstructor
public class AppLoopbackPlugin extends SoraPlugin implements HttpProvider {

    @Override
    public void onInit(Sora sora) {
        sora.addHttpProvider(this, this);
    }

    @Override
    public void onClose() {}

    @SneakyThrows
    @HttpEndpoint(uri = "/.*")
    public HttpResponse onRequest(SoraHttpSession session) {
        // This will match all uris, if the host is a loopback address then we serve the
        // app contents, otherwise we return null and sora will try another HttpProvider
        // (LocalServer function remains unimpeaded)

        if (session.getHost().startsWith("app-loopback.widgets.casterlabs.co:")) {
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
                // Load the resource
                byte[] content = IOUtil.readInputStreamBytes(
                    AppLoopbackPlugin.class.getClassLoader().getResourceAsStream("app" + uri)
                );
                String mimeType = "application/octet-stream";

                String[] split = uri.split("\\.");
                if (split.length > 1) {
                    mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
                }

                FastLogger.logStatic(LogLevel.TRACE, "200: Remapped %s -> app%s (%s)", session.getUri(), uri, mimeType);

                return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, content)
                    .setMimeType(mimeType);
            } catch (IOException | NullPointerException e) {
                FastLogger.logStatic(LogLevel.SEVERE, "404: Could not remap %s -> app%s", session.getUri(), uri);
            }
        }

        return null;
    }

    @Override
    public @Nullable String getVersion() {
        return CaffeinatedApp.getInstance().getBuildInfo().getVersionString();
    }

    @Override
    public @Nullable String getAuthor() {
        return "Casterlabs";
    }

    @Override
    public @NonNull String getName() {
        return "AppLoopback";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.caffeinated.apploopback";
    }

}
