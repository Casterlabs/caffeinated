package co.casterlabs.koi.api.types.events;

import java.util.Collections;
import java.util.List;

import co.casterlabs.koi.api.types.user.SimpleProfile;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Deprecated use {@link RichMessageEvent}
 */
@Deprecated
@Getter
@ToString
@NoArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class ChatEvent extends MessageMeta {
    private List<Mention> mentions;
    private List<String> links;
    private User sender;
    private String message;
    private String id;
    @JsonField("meta_id")
    private String metaId;
//    private Map<String, String> emotes;
//    @JsonField("external_emotes")
//    private Map<String, Map<String, ExternalEmote>> externalEmotes;

    // TODO patch this.
    private JsonObject emotes;
    @JsonField("external_emotes")
    private JsonObject externalEmotes;

    ChatEvent(SimpleProfile streamer, User sender, String id, String message) {
        this.streamer = streamer;
        this.sender = sender;
        this.id = id;
        this.metaId = id;
        this.message = message;
        this.mentions = Collections.emptyList();
        this.links = Collections.emptyList();
        this.emotes = new JsonObject();
        this.externalEmotes = new JsonObject();
    }

    @Override
    public KoiEventType getType() {
        return KoiEventType.CHAT;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @JsonClass(exposeAll = true)
    public static class Mention {
        private String target;
        private User mentioned;

    }

}
