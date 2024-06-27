package co.casterlabs.caffeinated.pluginsdk.scripting;

import lombok.NonNull;

public interface ScriptingEngines {

    public ScriptingEngine get(@NonNull String language);

}
