package co.casterlabs.koi.api.types.events;

import java.util.LinkedList;
import java.util.List;

import co.casterlabs.koi.api.types.events.rich.Attachment;
import co.casterlabs.koi.api.types.events.rich.ChatFragment;
import co.casterlabs.koi.api.types.events.rich.ChatFragment.FragmentType;
import co.casterlabs.koi.api.types.events.rich.Donation;
import co.casterlabs.koi.api.types.events.rich.EmojiFragment;
import co.casterlabs.koi.api.types.events.rich.EmoteFragment;
import co.casterlabs.koi.api.types.events.rich.LinkFragment;
import co.casterlabs.koi.api.types.events.rich.MentionFragment;
import co.casterlabs.koi.api.types.events.rich.TextFragment;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.rakurai.json.validation.JsonValidationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@NonNull
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = false)
public class RichMessageEvent extends MessageMeta {
    private User sender;

    private @JsonExclude List<ChatFragment> fragments = new LinkedList<>();
    private List<Donation> donations = new LinkedList<>();
    private List<Attachment> attachments = new LinkedList<>();
    private String raw;
    private String html;

    private String id;
    @JsonField("meta_id")
    private String metaId;

    @JsonField("reply_target")
    private String replyTarget = null;

    @JsonDeserializationMethod("fragments")
    private void $deserialize_fragments(JsonElement arr) throws JsonValidationException, JsonParseException {
        for (JsonElement fragment : arr.getAsArray()) {
            JsonObject obj = fragment.getAsObject();
            Class<? extends ChatFragment> fragmentClass = null;

            switch (FragmentType.valueOf(obj.getString("type"))) {
                case EMOJI:
                    fragmentClass = EmojiFragment.class;
                    break;
                case EMOTE:
                    fragmentClass = EmoteFragment.class;
                    break;
                case LINK:
                    fragmentClass = LinkFragment.class;
                    break;
                case MENTION:
                    fragmentClass = MentionFragment.class;
                    break;
                case TEXT:
                    fragmentClass = TextFragment.class;
                    break;
            }

            assert fragmentClass != null : "Unrecognized ChatFragment type: " + obj.getString("type");

            this.fragments.add(Rson.DEFAULT.fromJson(fragment, fragmentClass));
        }
    }

    @JsonSerializationMethod("fragments")
    private JsonElement $serialize_fragments() {
        return Rson.DEFAULT.toJson(this.fragments);
    }

    @Override
    public KoiEventType getType() {
        return KoiEventType.RICH_MESSAGE;
    }

}
