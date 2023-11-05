package co.casterlabs.caffeinated.pluginsdk.localization;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Language {
    // @formatter:off
    EN_US    ("en-US",  WritingDirection.LEFT_TO_RIGHT, "🇺🇸", "English, United States of America"),
    ES       ("es",     WritingDirection.LEFT_TO_RIGHT, "🇪🇸", "Español"),
    ES_491   ("es-419", WritingDirection.LEFT_TO_RIGHT, "🌎", "Español Latino"),
    FR       ("fr",     WritingDirection.LEFT_TO_RIGHT, "🇫🇷", "Français"),
    ID       ("id",     WritingDirection.LEFT_TO_RIGHT, "🇮🇩", "Bahasa Indonesia"),

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
