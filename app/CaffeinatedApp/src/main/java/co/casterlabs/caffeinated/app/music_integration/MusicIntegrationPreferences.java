package co.casterlabs.caffeinated.app.music_integration;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class MusicIntegrationPreferences {
    private JsonObject settings = new JsonObject();

}
