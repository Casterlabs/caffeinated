package co.casterlabs.caffeinated.app.controldeck;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.controldeck.protocol.deck.ControlDeck;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.Data;

@Data
public class ControlDeckPreferences {
    private Map<String, DeckConfig> decks = new HashMap<>();

    @JsonDeserializationMethod("decks")
    private void $deserialize_decks(JsonElement e) throws JsonValidationException, JsonParseException {
        JsonObject json = e.getAsObject();

        // I need to just move this into Rson already.
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            decks.put(
                entry.getKey(),
                Rson.DEFAULT.fromJson(entry.getValue(), DeckConfig.class)
            );
        }
    }

    @Data
    @JsonClass(exposeAll = true)
    public static class DeckConfig {
        private VolumeItemConfig[] volumeItems;

        public static DeckConfig create(ControlDeck deck) {
            DeckConfig conf = new DeckConfig();

            // Init the volume items.
            conf.volumeItems = new VolumeItemConfig[deck.getVolumeItemsCount()];
            for (int idx = 0; idx < conf.volumeItems.length; idx++) {
                conf.volumeItems[idx] = new VolumeItemConfig();
            }

            return conf;
        }

    }

    @Data
    @JsonClass(exposeAll = true)
    public static class VolumeItemConfig {
        private VolumeItemSource source = VolumeItemSource.NONE;
        private @Nullable String id;

    }

    public static enum VolumeItemSource {
        NONE,
        SPOTIFY,
        OBS,
        SYSTEM_VOLUME_MIXER;
    }

}
