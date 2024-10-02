package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.caffeinated.pluginsdk.Locale;
import co.casterlabs.caffeinated.util.MiscUtil;
import co.casterlabs.commons.platform.OSDistribution;
import co.casterlabs.commons.platform.Platform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class UIPreferences {
    private static final @JsonExclude String CURRENT_SKITTLE = MiscUtil.random("green_skittle", "orange_skittle", "purple_skittle", "red_skittle", "yellow_skittle");

    private String emojiProvider = Platform.osDistribution == OSDistribution.MACOS ? "system" : "twemoji"; // Use system emojis by default on macOS, Twemoji elsewhere.
    private String icon = "casterlabs";
    private String theme = "co.casterlabs.dark"; // Unused.
    private String language = Locale.EN_US.getCode();
    private boolean closeToTray = true;
    private ChatViewerPreferences chatViewerPreferences = new ChatViewerPreferences();
    private ActivityViewerPreferences activityViewerPreferences = new ActivityViewerPreferences();
    private boolean enableStupidlyUnsafeSettings = false;
    private boolean enableAlternateThemes = false;
    private double zoom = 1.0;
    private String uiFont = "";
    private boolean sidebarClosed = false;

    private DashboardConfig mainDashboard = new DashboardConfig();
    private DashboardConfig dockDashboard = new DashboardConfig();

//    private boolean mikeysMode = false; // R.I.P. Mikey's Mode, you will be missed. https://twitter.com/Casterlabs/status/1508475284944736268

    public String getIcon() {
        if ("skittles".equals(this.icon)) {
            return CURRENT_SKITTLE; // Randomize the Skittle shown in the taskbar.
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
        private boolean showBadgesOnLeft = false;
        private boolean showViewers = false;
        private boolean playDingOnMessage = false;
        private boolean readMessagesAloud = false;
        private String ttsVoice = "Brian";
        private boolean showPlatform = false;
        private boolean showActivities = false;
        private String colorBy = "THEME";
        private double ttsOrDingVolume = 1;
        private double textSize = 1;
        private boolean showPronouns = false;
        private boolean showZebraStripes = false;
        private JsonObject inputBoxPreferences = new JsonObject();

    }

    @Data
    @JsonClass(exposeAll = true)
    public static class ActivityViewerPreferences {
        private boolean showTimestamps = true;
        private boolean showProfilePictures = false;
        private boolean showPlatform = false;
        private String colorBy = "THEME";
        private double textSize = 1;
        private boolean showPronouns = false;
        private boolean showZebraStripes = false;

    }

}
