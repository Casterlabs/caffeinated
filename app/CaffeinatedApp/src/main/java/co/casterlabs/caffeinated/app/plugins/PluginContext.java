package co.casterlabs.caffeinated.app.plugins;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PluginContext {
    private List<String> pluginIds;
    private @Setter @Nullable File file;
    private boolean hasSucceeded;

    public PluginContext(List<String> pluginIds, boolean hasSucceeded) {
        this.pluginIds = Collections.unmodifiableList(pluginIds);
        this.hasSucceeded = hasSucceeded;
    }

    public boolean wasCleanFailure() {
        // If it hasSucceeded then we always return true.
        // Otherwise we check to see if the pluginIds list is empty.
        return !this.hasSucceeded || this.pluginIds.isEmpty();
    }

}
