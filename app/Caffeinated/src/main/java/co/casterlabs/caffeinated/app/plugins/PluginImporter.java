package co.casterlabs.caffeinated.app.plugins;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonObject;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class PluginImporter {
    private static final FastLogger logger = new FastLogger();

    public static List<WidgetSettingsDetails> importOldJson() {
        File oldJson = new File(CaffeinatedApp.APP_DATA_DIR, "preferences/plugins.json");
        if (!oldJson.exists()) return Collections.emptyList();

        try {
            logger.info("Found old plugins.json, importing...");

            JsonObject json = Rson.DEFAULT.fromJson(new String(Files.readAllBytes(oldJson.toPath()), StandardCharsets.UTF_8), JsonObject.class);

            return Rson.DEFAULT.fromJson(json.get("widgetSettings"), new TypeToken<List<WidgetSettingsDetails>>() {
            });
        } catch (Exception e) {
            logger.warn("Could not import old plugins.json:\n%s", e);
            return Collections.emptyList();
        } finally {
            // Keep a backup of the file.
            logger.info("Done!");
            oldJson.renameTo(new File(CaffeinatedApp.APP_DATA_DIR, "preferences/old/plugins.json"));
            oldJson.delete();
        }
    }

}
