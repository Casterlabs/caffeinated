package co.casterlabs.caffeinated.pluginsdk;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@NonNull
@JsonClass(exposeAll = true)
public class CasterlabsAccount {
    // @formatter:off
    private @Accessors(fluent= true) boolean  hasCasterlabsPlus;
    private String   accountId;
    private String   email;
    private String   name;
    private boolean  emailVerified;
    private boolean  isBanned;
    private long     creationTimestamp;
    // @formatter:on

}
