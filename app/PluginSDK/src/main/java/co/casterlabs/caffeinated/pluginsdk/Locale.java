package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public enum Locale {
    // @formatter:off
    EN_US    ("en-US",  WritingDirection.LEFT_TO_RIGHT, "🇺🇸", "English (United States)"),
    ES_ES    ("es-ES",  WritingDirection.LEFT_TO_RIGHT, "🇪🇸", "Español"),
    ES_419   ("es-419", WritingDirection.LEFT_TO_RIGHT, "🌎", "Español Latino"),
    FR_FR    ("fr-FR",  WritingDirection.LEFT_TO_RIGHT, "🇫🇷", "Français"),
    DA_DK    ("da-DK",  WritingDirection.LEFT_TO_RIGHT, "🇩🇰", "Dansk"),
    ID_ID    ("id-ID",  WritingDirection.LEFT_TO_RIGHT, "🇮🇩", "Bahasa Indonesia"),
    RU_RU    ("ru-RU",  WritingDirection.LEFT_TO_RIGHT, "🇷🇺", "Русский"),

    // @formatter:on
    ;

    public final String code;
    public final WritingDirection direction;
    public final String emoji;
    public final String name;

    public static enum WritingDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put("code", this.code)
            .put("direction", this.direction.name())
            .put("emoji", this.emoji)
            .put("name", this.name);
    }

}
