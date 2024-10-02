package co.casterlabs.koi.api.types.events;

import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class FollowEvent extends KoiEvent {
    private User follower;

    @Override
    public KoiEventType getType() {
        return KoiEventType.FOLLOW;
    }

}
