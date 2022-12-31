package co.casterlabs.caffeinated.app.ui;

import java.util.Calendar;

import co.casterlabs.caffeinated.util.MiscUtil;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class UIPreferences {
    private static final String CURRENT_SKITTLE = MiscUtil.random("green_skittle", "orange_skittle", "purple_skittle", "red_skittle", "yellow_skittle");
    private static final boolean IS_PRIDE_MONTH = Calendar.getInstance().get(Calendar.MONTH) == Calendar.JUNE;

    private String emojiProvider = Platform.osDistribution == OSDistribution.MACOS ? "system" : "twemoji"; // Use system emojis by default on macOS, Twemoji elsewhere.
    private String icon = "casterlabs";
    private String theme = "co.casterlabs.dark"; // Unused.
    private String language = "en-US";
    private boolean closeToTray = true;
    private ChatViewerPreferences chatViewerPreferences = new ChatViewerPreferences();
    private boolean enableStupidlyUnsafeSettings = false;
    private boolean enableAlternateThemes = false;

    private DashboardConfig mainDashboard = new DashboardConfig();
    private DashboardConfig dockDashboard = new DashboardConfig();

    private boolean mikeysMode = false; // https://twitter.com/Casterlabs/status/1508475284944736268

    public String getIcon() {
        if (IS_PRIDE_MONTH) {
            return "pride";
        } else if ("skittles".equals(this.icon)) {
            return CURRENT_SKITTLE; // Randomize the skittle shown in the taskbar.
                                    // https://twitter.com/Skittles/status/1510008458489249796
        } else {
            return this.icon;
        }
    }

    @Data
    @JsonClass(exposeAll = true)
    public static class ChatViewerPreferences {
        private boolean showChatTimestamps = true;
        private boolean showProfilePictures = false;
        private boolean showBadges = false;
        private boolean showViewers = false;
        private boolean userBadgesOnLeft = false;

        // Still used by the popout dock.
        private @Deprecated boolean showModActions = true;
        private @Deprecated boolean showViewersList = true;

    }

}
