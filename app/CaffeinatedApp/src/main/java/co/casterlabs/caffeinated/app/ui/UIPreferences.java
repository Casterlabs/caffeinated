package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.caffeinated.util.MiscUtil;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class UIPreferences {
    private static final String CURRENT_SKITTLE = MiscUtil.random("green_skittle", "orange_skittle", "purple_skittle", "red_skittle", "yellow_skittle");

    private String emojiProvider = "system";
    private String icon = "casterlabs";
    private String theme = "co.casterlabs.dark";
    private String language = "en-US";
    private boolean closeToTray = true;
    private ChatViewerPreferences chatViewerPreferences = new ChatViewerPreferences();
    private boolean enableStupidlyUnsafeSettings = false;

    private boolean mikeysMode = false; // https://twitter.com/Casterlabs/status/1508475284944736268

    public String getIcon() {
        if ("skittles".equals(this.icon)) {
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
        private boolean showModActions = true;
        private boolean showProfilePictures = false;
        private boolean showBadges = false;
        private boolean showViewers = false;
        private boolean showViewersList = true;
        private int viewersListX = 205;
        private int viewersListY = 5;

    }

}
