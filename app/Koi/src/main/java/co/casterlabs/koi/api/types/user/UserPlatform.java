package co.casterlabs.koi.api.types.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum UserPlatform {
    TWITCH("Twitch"),
    TROVO("Trovo"),
    YOUTUBE("YouTube"),
    DLIVE("DLive"),
    KICK("Kick"),
    TIKTOK("TikTok"),
    LOCO("Loco"),

    // Coming up.
    X("𝕏"),
    RUMBLE("Rumble"),

    // Graveyard. R.I.P.
    CAFFEINE("Caffeine"),
    GLIMESH("Glimesh"),
    BRIME("Brime"),
    THETA("Theta"),
    YOUNOW("YouNow"),
    LIVESPACE("LiveSpace"),

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
