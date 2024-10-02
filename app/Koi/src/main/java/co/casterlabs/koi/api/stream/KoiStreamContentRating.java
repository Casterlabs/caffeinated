package co.casterlabs.koi.api.stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KoiStreamContentRating {
    FAMILY_FRIENDLY("Family Friendly"),
    PG("PG"),
    MATURE("Mature");

    public static final Map<KoiStreamContentRating, String> LANG;

    static {
        Map<KoiStreamContentRating, String> map = new HashMap<>();
        LANG = Collections.unmodifiableMap(map);

        for (KoiStreamContentRating l : values()) {
            map.put(l, l.lang);
        }
    }

    private String lang;

    public static JsonObject toJson() {
        JsonObject json = new JsonObject();

        for (KoiStreamContentRating rating : values()) {
            json.put(rating.name(), rating.lang);
        }

        return json;
    }

}
