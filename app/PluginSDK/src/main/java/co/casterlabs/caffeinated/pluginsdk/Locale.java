package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public enum Locale {
    // @formatter:off
    EN_US    (WritingDirection.LEFT_TO_RIGHT, "🇺🇸", "English (United States)"),
    ES_ES    (WritingDirection.LEFT_TO_RIGHT, "🇪🇸", "Español"),
    ES_419   (WritingDirection.LEFT_TO_RIGHT, "🌎", "Español Latino"),
    FR_FR    (WritingDirection.LEFT_TO_RIGHT, "🇫🇷", "Français"),
    DA_DK    (WritingDirection.LEFT_TO_RIGHT, "🇩🇰", "Dansk"),
    ID_ID    (WritingDirection.LEFT_TO_RIGHT, "🇮🇩", "Bahasa Indonesia"),
    RU_RU    (WritingDirection.LEFT_TO_RIGHT, "🇷🇺", "Русский"),
    TR_TR    (WritingDirection.LEFT_TO_RIGHT, "🇹🇷", "Türkçe"),

    // @formatter:on
    ;

    public final WritingDirection direction;
    public final String emoji;
    public final String name;

    public String getCode() {
        return this.name();
    }

    public static enum WritingDirection {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put("direction", this.direction.name())
            .put("emoji", this.emoji)
            .put("name", this.name);
    }

}
