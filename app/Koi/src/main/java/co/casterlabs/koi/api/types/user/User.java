package co.casterlabs.koi.api.types.user;

import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class User extends SimpleProfile {
    private List<UserRoles> roles;

    private List<String> badges;

    private String color;

    private String username;

    private String displayname;

    private String bio;

    private String link;

    @JsonField("image_link")
    private String imageLink;

    @JsonField("followers_count")
    private long followersCount = -1;

    @JsonField("subscriber_count")
    private long subCount = -1;

    public static enum UserRoles {
        BROADCASTER,
        SUBSCRIBER,
        FOLLOWER,
        MODERATOR,
        STAFF;
    }

}
