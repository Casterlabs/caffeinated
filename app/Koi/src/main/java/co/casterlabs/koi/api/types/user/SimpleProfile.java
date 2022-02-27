package co.casterlabs.koi.api.types.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SimpleProfile {
    private String id;
    private String channelId;
    private UserPlatform platform;

    @Override
    public String toString() {
        return String.format("%s/%s;%s", this.id, this.channelId, this.platform);
    }

    public int tryGetIdAsInt() {
        return Integer.parseInt(this.id);
    }

    public int tryGetChannelIdAsInt() {
        return Integer.parseInt(this.channelId);
    }

}
