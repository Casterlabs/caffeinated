package co.casterlabs.caffeinated.app.chatbot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonClass(exposeAll = true)
public class ChatbotPreferences {
    private List<String> timers = new ArrayList<>();
    private Set<Command> commands = new HashSet<>();
    private Set<Shout> shouts = new HashSet<>();

    private int timerIntervalSeconds = 300;

    private KoiChatterType chatter = KoiChatterType.SYSTEM;

    @Data
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Command {
        private @Nullable UserPlatform platform; // NULL = ANY
        private String trigger;
        private String response;
        private CommandType type;

        public static enum CommandType {
            COMMAND,
            CONTAINS,
            // SCRIPT
        }
    }

    @Data
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Shout {
        private @Nullable UserPlatform platform; // NULL = ANY
        private KoiEventType eventType;
        private String text;
    }

}
