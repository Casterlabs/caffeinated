package co.casterlabs.koi.api.types.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum UserPlatform {
    CAFFEINE("Caffeine"),
    TWITCH("Twitch"),
    TROVO("Trovo"),
    YOUTUBE("YouTube"),
    DLIVE("DLive"),
    THETA("Theta"),
    KICK("Kick"),
    TIKTOK("TikTok"),

    // Coming up.
    YOUNOW("YouNow"),

    // Graveyard. R.I.P.
    GLIMESH("Glimesh"),
    BRIME("Brime"),

    // Other.
    CASTERLABS_SYSTEM("System"),
    CUSTOM_INTEGRATION("Custom Integration 🔧"), // For custom events for non-streaming platforms.
    ;

    private @Getter String str;

    @Override
    public String toString() {
        return this.str;
    }

}
