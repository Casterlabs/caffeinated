package co.casterlabs.caffeinated.pluginsdk.koi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.RoomstateEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public abstract class Koi {

    public abstract List<KoiEvent> getEventHistory();

    public abstract Map<UserPlatform, List<User>> getViewers();

    public abstract Map<UserPlatform, UserUpdateEvent> getUserStates();

    public abstract Map<UserPlatform, StreamStatusEvent> getStreamStates();

    public abstract Map<UserPlatform, RoomstateEvent> getRoomStates();

    public abstract Map<UserPlatform, List<KoiIntegrationFeatures>> getFeatures();

    public abstract void sendChat(@NonNull UserPlatform platform, @NonNull String message, @NonNull KoiChatterType chatter);

    public abstract void upvoteChat(@NonNull UserPlatform platform, @NonNull String messageId);

    public abstract void deleteChat(@NonNull UserPlatform platform, @NonNull String messageId);

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    public JsonObject toJson() {
        return new JsonObject()
            .put("history", Rson.DEFAULT.toJson(this.getEventHistory()))
            .put("viewers", Rson.DEFAULT.toJson(this.getViewers()))
            .put("userStates", Rson.DEFAULT.toJson(this.getUserStates()))
            .put("streamStates", Rson.DEFAULT.toJson(this.getStreamStates()))
            .put("roomStates", Rson.DEFAULT.toJson(this.getRoomStates()));
    }

    public List<UserPlatform> getSignedInPlatforms() {
        return new LinkedList<>(this.getUserStates().keySet());
    }

    public boolean isMultiUserMode() {
        return this.getUserStates().size() > 1;
    }

    public boolean isSignedOut() {
        return this.getUserStates().size() == 0;
    }

    public int getMaxMessageLength(@NonNull UserPlatform platform) {
        switch (platform) {
            case CAFFEINE:
                return 80;

            case TWITCH:
                return 500;

            case TROVO:
                return 300;

            case GLIMESH:
                return 255;

            case BRIME:
                return 300;

            default:
                return 100; // ?
        }
    }

    /**
     * It's important to note that this is really only useful outside of multi-user
     * mode.
     * 
     * @throws IndexOutOfBoundsException if no user is signed in.
     */
    public UserPlatform getFirstSignedInPlatform() {
        return this.getSignedInPlatforms().get(0);
    }

}
