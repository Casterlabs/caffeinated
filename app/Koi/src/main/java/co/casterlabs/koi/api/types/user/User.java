package co.casterlabs.koi.api.types.user;

import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Deprecated
    public User(
        String UPID, String id, String channelId, UserPlatform platform,
        List<UserRoles> roles, List<String> badges, String color, String username,
        String displayname, String bio, String link, String imageLink, int followersCount, int subCount
    ) {
        this(roles, badges, color, username, displayname, bio, link, imageLink, followersCount, subCount);
        this.UPID = UPID;
        this.id = id;
        this.channelId = channelId;
        this.platform = platform;
    }

    public static enum UserRoles {
        BROADCASTER,
        SUBSCRIBER,
        FOLLOWER,
        MODERATOR,
        STAFF,
        VIP;
    }

}
