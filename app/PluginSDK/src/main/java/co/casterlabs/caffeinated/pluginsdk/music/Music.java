package co.casterlabs.caffeinated.pluginsdk.music;

import java.util.Map;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;

public abstract class Music {

    public abstract Map<String, MusicProvider> getProviders();

    public abstract MusicProvider getActivePlayback();

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public JsonObject toJson() {
        JsonElement _activePlayback;

        if (this.getActivePlayback() == null) {
            _activePlayback = JsonNull.INSTANCE;
        } else {
            _activePlayback = this.getActivePlayback().toJson();
        }

        return new JsonObject()
            .put("activePlayback", _activePlayback)
            .put("providers", Rson.DEFAULT.toJson(this.getProviders()));
    }

}
