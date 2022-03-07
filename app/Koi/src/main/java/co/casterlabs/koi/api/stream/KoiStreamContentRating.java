package co.casterlabs.koi.api.stream;

import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KoiStreamContentRating {
    FAMILY_FRIENDLY("Family Friendly"),
    PG("PG"),
    MATURE("Mature");

    private String lang;

    public static JsonObject toJson() {
        JsonObject json = new JsonObject();

        for (KoiStreamContentRating rating : values()) {
            json.put(rating.name(), rating.lang);
        }

        return json;
    }

}
