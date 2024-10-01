package co.casterlabs.caffeinated.pluginsdk.scripting;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.koi.api.types.KoiEvent;
import lombok.NonNull;

public interface ScriptingEngine {

    public @Nullable Object execute(@Nullable KoiEvent event, @NonNull String scriptToExecute);

}
