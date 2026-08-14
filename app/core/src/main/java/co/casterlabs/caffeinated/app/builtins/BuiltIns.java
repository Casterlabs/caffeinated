package co.casterlabs.caffeinated.app.builtins;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginResource;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.io.streams.StreamUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class BuiltIns {
    private static final PluginResource RESOURCE_NOT_FOUND = PluginResource.of("Not found.".getBytes(), "text/plain");

    public static List<CaffeinatedPlugin> init() {
        return Arrays.asList(
            new _UIDocksPlugin(),
            new _KofiServicePlugin()
        );
    }

    static @Nullable PluginResource resolveUIFile(String resource) {
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
            byte[] data = StreamUtil.toBytes(in);

            return PluginResource.of(
                data,
                mimeType
            );
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.DEBUG, "An error occurred whilst loading resource %s:\n%s", resource, e);
            return RESOURCE_NOT_FOUND;
        }
    }

}
