package co.casterlabs.koi.api.types.user;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class SimpleProfile {
    private String UPID;
    private String id;
    private @JsonField("channel_id") String channelId;
    private UserPlatform platform;

    public int tryGetIdAsInt() {
        return Integer.parseInt(this.id);
    }

    public int tryGetChannelIdAsInt() {
        return Integer.parseInt(this.channelId);
    }

    @Override
    public SimpleProfile clone() {
        return new SimpleProfile(this.UPID, this.id, this.channelId, this.platform);
    }

}
