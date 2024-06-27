package co.casterlabs.caffeinated.app.scripting;

import java.util.Map;

import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngine;
import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngines;
import lombok.NonNull;

public class ScriptingEnginesImpl implements ScriptingEngines {
    private static final Map<String, ScriptingEngine> ENGINES = Map.of(
        "javascript", new JavascriptEngineImpl()
    );

    @Override
    public ScriptingEngine get(@NonNull String language) {
        ScriptingEngine engine = ENGINES.get(language.toLowerCase());
        if (engine == null) {
            throw new IllegalArgumentException("Unsupported engine: " + language);
        }

        return engine;
    }

}
