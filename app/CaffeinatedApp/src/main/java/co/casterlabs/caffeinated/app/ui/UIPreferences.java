package co.casterlabs.caffeinated.app.ui;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class UIPreferences {
    private String icon = "casterlabs";
    private String theme = "co.casterlabs.dark";
    private boolean closeToTray = true;
    private ChatViewerPreferences chatViewerPreferences = new ChatViewerPreferences();

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
