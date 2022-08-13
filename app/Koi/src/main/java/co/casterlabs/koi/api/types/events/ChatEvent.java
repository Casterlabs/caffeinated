package co.casterlabs.koi.api.types.events;

import java.util.List;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @Deprecated use {@link RichMessageEvent}
 */
@Deprecated
@Getter
@ToString
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
