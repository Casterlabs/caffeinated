package co.casterlabs.caffeinated.app.chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonClass(exposeAll = true)
public class ChatbotPreferences {
    private List<String> timers = new ArrayList<>();
    private List<Command> commands = new ArrayList<>();
    private List<Shout> shouts = new ArrayList<>();

    private int timerIntervalSeconds = 300;

    private List<String> chatbots = new ArrayList<>();
    private boolean hideCommandsFromChat = false;
    private boolean hideTimersFromChat = false;

    private KoiChatterType chatter = KoiChatterType.SYSTEM;

    private Map<String, Object> store = new HashMap<>();

    public KoiChatterType getRealChatter() {
        boolean hasCasterlabsPlus = CaffeinatedApp.getInstance().hasCasterlabsPlus();

        if (hasCasterlabsPlus) {
            return this.chatter;
        } else {
            return KoiChatterType.SYSTEM;
        }
    }

    @Data
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Command {
        private @Nullable UserPlatform platform; // NULL = ANY
        private TriggerType triggerType;
        private String trigger; // Not used on ALWAYS.
        private Action responseAction; // Must be EXECUTE on ALWAYS
        private String response;

        @JsonDeserializationMethod("type")
        private void $migrate_trigger(JsonElement e) {
            switch (e.getAsString()) {
                case "SCRIPT":
                    this.triggerType = TriggerType.COMMAND;
                    this.responseAction = Action.EXECUTE;
                    return;
                case "CONTAINS":
                    this.triggerType = TriggerType.CONTAINS;
                    this.responseAction = Action.REPLY_WITH;
                    return;
                default:
                case "COMMAND":
                    this.triggerType = TriggerType.COMMAND;
                    this.responseAction = Action.REPLY_WITH;
                    return;
            }
        }

    }

    @Data
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Shout {
        private @Nullable UserPlatform platform; // NULL = ANY
        private KoiEventType eventType;
        private Action responseAction;
        private String response;

        @JsonDeserializationMethod("text")
        private void $migrate_response(JsonElement e) {
            this.responseAction = Action.REPLY_WITH;
            this.response = e.getAsString();
        }
    }

    public static enum TriggerType {
        COMMAND,
        CONTAINS,
        ALWAYS
    }

    public static enum Action {
        REPLY_WITH,
        EXECUTE
    }
}
