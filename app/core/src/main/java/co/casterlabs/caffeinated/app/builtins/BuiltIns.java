package co.casterlabs.caffeinated.app.builtins;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.commons.io.streams.StreamUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class BuiltIns {

    public static List<CaffeinatedPlugin> init() {
        return Arrays.asList(
            new _UIDocksPlugin(),
            new _KofiServicePlugin()
        );
    }

    static @Nullable Pair<String, String> resolveUIFile(String resource) {
        if (resource.isEmpty()) {
            resource = "/index.html";
        } else {
            // Append `index.html` to the end when required.
            if (!resource.contains(".")) {
                if (resource.endsWith("/")) {
                    resource += "index.html";
                } else {
                    resource += ".html";
                }
            }
        }

        String mimeType = "application/octet-stream";

        String[] split = resource.split("\\.");
        if (split.length > 1) {
            mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
        }

        resource = "co/casterlabs/caffeinated/app/ui/html" + resource; // Load from the app's actual resources.
        FastLogger.logStatic(LogLevel.DEBUG, "Loading resource: %s", resource);

        try (InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream(resource)) {
            return new Pair<>(
                StreamUtil.toString(in, StandardCharsets.UTF_8),
                mimeType
            );
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.DEBUG, "An error occurred whilst loading resource %s:\n%s", resource, e);
            return new Pair<>("", "text/plain");
        }
    }

}
