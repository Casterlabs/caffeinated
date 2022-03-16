package co.casterlabs.caffeinated.app.plugins;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonClass(exposeAll = true)
public class PluginContext {
    private String id = UUID.randomUUID().toString();
    private List<String> pluginIds;
    private @Setter @Nullable File file;
    private boolean hasSucceeded;
    private @Setter ContextType pluginType = ContextType.PLUGIN;

    public PluginContext(List<String> pluginIds, boolean hasSucceeded) {
        this.pluginIds = Collections.unmodifiableList(pluginIds);
        this.hasSucceeded = hasSucceeded;
    }

    @JsonSerializationMethod("file")
    private JsonElement $serialize_file() {
        if (this.file == null) {
            return JsonNull.INSTANCE;
        } else {
            return new JsonString(this.file.getName());
        }
    }

    public boolean wasCleanFailure() {
        // If it hasSucceeded then we always return true.
        // Otherwise we check to see if the pluginIds list is empty.
        return !this.hasSucceeded || this.pluginIds.isEmpty();
    }

    public static enum ContextType {
        PLUGIN,
        INTERNAL,
        STORE_ASSET
    }

}
