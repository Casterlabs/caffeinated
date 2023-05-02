package co.casterlabs.caffeinated.app.thirdparty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.io.IOUtil;
import lombok.NonNull;

public class StreamlabsServicePlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        this.getPlugins().registerWidget(this, StreamlabsUI.DETAILS, StreamlabsUI.class);
    }

    @Override
    public void onClose() {}

    @Override
    public @NonNull String getName() {
        return "Streamlabs Integration";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.thirdparty.streamlabs";
    }

    public static class StreamlabsUI extends Widget {
        public static final WidgetDetails DETAILS = new WidgetDetails()
            .withNamespace("co.casterlabs.thirdparty.streamlabs.settings")
            .withType(WidgetType.SETTINGS_APPLET)
            .withFriendlyName("Streamlabs Integration");

        @Override
        public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
            return "/thirdparty/streamlabs/settings";
        }

    }

    @Override
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        // Append `index.html` to the end when required.
        if (!resource.contains(".")) {
            if (resource.endsWith("/")) {
                resource += "index.html";
            } else {
                resource += ".html";
            }
        }

        String mimeType = "application/octet-stream";

        String[] split = resource.split("\\.");
        if (split.length > 1) {
            mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
        }

        resource = "app" + resource; // Load from the app's actual resources.
        this.getLogger().debug("Loading resource: %s", resource);

        try (InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream(resource)) {
            return new Pair<>(
                IOUtil.readInputStreamString(in, StandardCharsets.UTF_8),
                mimeType
            );
        } catch (Exception e) {
            this.getLogger().debug("An error occurred whilst loading resource %s:\n%s", resource, e);
            return new Pair<>("", "text/plain");
        }
    }

}
