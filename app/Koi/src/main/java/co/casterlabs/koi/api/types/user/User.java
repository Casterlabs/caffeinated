package co.casterlabs.koi.api.types.user;

import java.lang.reflect.Method;
import java.util.List;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Getter
@ToString
@NoArgsConstructor
@JsonClass(exposeAll = true)
public class User extends SimpleProfile {
    private List<UserRoles> roles;

    private List<String> badges;

    private String color;

    private String username;

    private String displayname;

    private Pronouns pronouns = Pronouns.UNKNOWN;

    private String bio;

    private String link;

    @JsonField("image_link")
    private String imageLink;

    @JsonField("followers_count")
    private long followersCount = -1;

    @JsonField("subscriber_count")
    private long subCount = -1;

    public User(
        String id, String channelId, UserPlatform platform,
        List<UserRoles> roles, List<String> badges,
        String color, String username, String displayname, String bio, String link, String imageLink,
        int followersCount, int subCount
    ) {
        this.id = id;
        this.channelId = channelId;
        this.platform = platform;
        this.UPID = this.id + ';' + this.platform.name();
        this.roles = roles;
        this.badges = badges;
        this.color = color;
        this.username = username;
        this.displayname = displayname;
        this.bio = bio;
        this.link = link;
        this.imageLink = imageLink;
        this.followersCount = followersCount;
        this.subCount = subCount;
    }

    @JsonValidate
    private void $lookupPronouns() {
        try {
            Class<?> pronounQuery = Class.forName("co.casterlabs.caffeinated.app.PronounsQuery");
            Method get = pronounQuery.getMethod("get", User.class);
            this.pronouns = (Pronouns) get.invoke(null, this);
        } catch (Throwable t) {
            FastLogger.logStatic(LogLevel.DEBUG, "An error occurred whilst fetching pronouns for %s:\n%s", this.UPID, t);
        }
    }

    public static enum UserRoles {
        BROADCASTER,
        SUBSCRIBER,
        FOLLOWER,
        MODERATOR,
        STAFF,
        VIP,
        OG;
    }

}
