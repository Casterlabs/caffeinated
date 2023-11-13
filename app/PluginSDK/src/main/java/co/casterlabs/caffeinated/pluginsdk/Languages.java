package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@JsonClass(exposeAll = true)
public enum Languages {
    // @formatter:off
    EN_US    ("en-US",  WritingDirection.LEFT_TO_RIGHT, "🇺🇸", "English, United States of America"),
    ES       ("es-ES",  WritingDirection.LEFT_TO_RIGHT, "🇪🇸", "Español"),
    ES_491   ("es-419", WritingDirection.LEFT_TO_RIGHT, "🌎", "Español Latino"),
    FR       ("fr-FR",  WritingDirection.LEFT_TO_RIGHT, "🇫🇷", "Français"),
    ID       ("id-ID",  WritingDirection.LEFT_TO_RIGHT, "🇮🇩", "Bahasa Indonesia"),

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

}
