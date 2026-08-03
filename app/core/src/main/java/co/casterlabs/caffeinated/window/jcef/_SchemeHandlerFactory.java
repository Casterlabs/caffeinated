package co.casterlabs.caffeinated.window.jcef;

import java.net.URI;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefCallback;
import org.cef.callback.CefResourceReadCallback;
import org.cef.callback.CefResourceSkipCallback;
import org.cef.callback.CefSchemeHandlerFactory;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.BoolRef;
import org.cef.misc.IntRef;
import org.cef.misc.LongRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import co.casterlabs.caffeinated.app.util.Resources;
import co.casterlabs.caffeinated.util.MimeTypes;
import lombok.AllArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

class _SchemeHandlerFactory implements CefSchemeHandlerFactory {

    @Override
    public CefResourceHandler create(CefBrowser browser, CefFrame frame, String schemeName, CefRequest request) {
        return new Handler();
    }

    private static ResponseContent get(String path) {
        path = path
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

            FastLogger.logStatic(LogLevel.DEBUG, "200 %s -> app%s (%s)", path, path, mimeType);

            return new ResponseContent(200, content, mimeType);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "404 %s -> app%s\n%s", path, path, e);
            return new ResponseContent(404, "Not found".getBytes(), "text/plain");
        }
    }

    @AllArgsConstructor
    private static class ResponseContent {
        public final int statusCode;
        public final byte[] content;
        public final String mimeType;
    }

    static class Handler implements CefResourceHandler {
        private ResponseContent data;
        private int offset;

        @Override
        public boolean processRequest(CefRequest request, CefCallback callback) {
            String path = URI.create(request.getURL()).getPath();

            if (path == null || path.isEmpty()) {
                path = "/";
            }

            this.data = get(path);
            this.offset = 0;

            callback.Continue();
            return true;
        }

        @Override
        public boolean open(CefRequest request, BoolRef handleRequest, CefCallback callback) {
            handleRequest.set(true);
            return processRequest(request, callback);
        }

        @Override
        public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
            response.setStatus(this.data.statusCode);

            if (this.data.mimeType != null) {
                response.setMimeType(this.data.mimeType);
            }

            responseLength.set(this.data.content.length);
        }

        @Override
        public boolean readResponse(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefCallback callback) {
            int remaining = this.data.content.length - this.offset;

            if (remaining <= 0) {
                bytesRead.set(0);
                return false;
            }

            int count = Math.min(bytesToRead, remaining);

            System.arraycopy(this.data.content, this.offset, dataOut, 0, count);

            this.offset += count;
            bytesRead.set(count);

            return true;
        }

        @Override
        public boolean read(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefResourceReadCallback callback) {
            return readResponse(dataOut, bytesToRead, bytesRead, null);
        }

        @Override
        public boolean skip(long bytesToSkip, LongRef bytesSkipped, CefResourceSkipCallback callback) {
            int remaining = this.data.content.length - this.offset;
            int skipped = (int) Math.min(bytesToSkip, remaining);

            this.offset += skipped;
            bytesSkipped.set(skipped);

            return skipped > 0;
        }

        @Override
        public void cancel() {}

    }

}
