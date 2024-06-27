package co.casterlabs.caffeinated.app.scripting;

import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngine;
import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngines;
import lombok.NonNull;

public class ScriptingEnginesImpl implements ScriptingEngines {
    private JavascriptEngineImpl javascript = new JavascriptEngineImpl();

    @Override
    public ScriptingEngine get(@NonNull String language) {
        switch (language.toLowerCase()) {
            case "javascript":
                return this.javascript;

            default:
                throw new IllegalArgumentException("Unsupported engine: " + language);
        }
    }

}
