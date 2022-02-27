package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class RoomstateEvent extends KoiEvent {

    @Deprecated
    public RoomstateEvent() {}

    @Deprecated
    public RoomstateEvent(@NonNull User user) {
        this.streamer = user;
        this.abilities = new EventAbilities();
    }

    @JsonField("is_emote_only")
    private boolean isEmoteOnly = false;

    @JsonField("is_subs_only")
    private boolean isSubsOnly = false;

    @JsonField("is_r9k")
    private boolean isR9KMode = false;

    @JsonField("is_followers_only")
    private boolean isFollowersOnly = false;

    @JsonField("is_slowmode")
    private boolean isSlowMode = false;

    @Override
    public KoiEventType getType() {
        return KoiEventType.ROOMSTATE;
    }

}
