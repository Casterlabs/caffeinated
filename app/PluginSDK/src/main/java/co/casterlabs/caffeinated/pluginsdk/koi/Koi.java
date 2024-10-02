package co.casterlabs.caffeinated.pluginsdk.koi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.KoiIntegrationFeatures;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.events.ConnectionStateEvent.ConnectionState;
import co.casterlabs.koi.api.types.events.RoomstateEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.UserUpdateEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public interface Koi {

    public List<KoiEvent> getEventHistory();

    public Map<UserPlatform, List<User>> getViewers();

    public Map<UserPlatform, Integer> getViewerCounts();

    public Map<UserPlatform, UserUpdateEvent> getUserStates();

    public Map<UserPlatform, StreamStatusEvent> getStreamStates();

    public Map<UserPlatform, RoomstateEvent> getRoomStates();

    public Map<UserPlatform, List<KoiIntegrationFeatures>> getFeatures();

    public Map<UserPlatform, Map<String, ConnectionState>> getConnectionStates();

    /**
     * A NULL platform will broadcast to all connected accounts.
     */
    public void sendChat(@Nullable UserPlatform platform, @NonNull String message, @NonNull KoiChatterType chatter, @Nullable String replyTarget, boolean isUserGesture);

    public void upvoteChat(@NonNull UserPlatform platform, @NonNull String messageId);

    public void deleteChat(@NonNull UserPlatform platform, @NonNull String messageId, boolean isUserGesture);

    public void broadcastEvent(@NonNull KoiEvent event);

    /**
     * @deprecated This is used internally.
     */
    @Deprecated
    default JsonObject toJson() {
        int previous = Math.min(35, this.getEventHistory().size());
        List<KoiEvent> history = this.getEventHistory().subList(this.getEventHistory().size() - previous, this.getEventHistory().size());

        return new JsonObject()
            .put("history", Rson.DEFAULT.toJson(history))
            .put("viewers", Rson.DEFAULT.toJson(this.getViewers()))
            .put("viewerCounts", Rson.DEFAULT.toJson(this.getViewerCounts()))
            .put("userStates", Rson.DEFAULT.toJson(this.getUserStates()))
            .put("streamStates", Rson.DEFAULT.toJson(this.getStreamStates()))
            .put("roomStates", Rson.DEFAULT.toJson(this.getRoomStates()))
            .put("features", Rson.DEFAULT.toJson(this.getFeatures()));
    }

    /**
     * @deprecated This is used internally.
     * 
     * @implNote   The difference between this and {@link #toJson()} is that this
     *             version give a fuller event history.
     */
    @Deprecated
    default JsonObject toJsonExtended() {
        int previous = Math.min(200, this.getEventHistory().size());
        List<KoiEvent> history = this.getEventHistory().subList(this.getEventHistory().size() - previous, this.getEventHistory().size());

        return new JsonObject()
            .put("history", Rson.DEFAULT.toJson(history))
            .put("viewers", Rson.DEFAULT.toJson(this.getViewers()))
            .put("viewerCounts", Rson.DEFAULT.toJson(this.getViewerCounts()))
            .put("userStates", Rson.DEFAULT.toJson(this.getUserStates()))
            .put("streamStates", Rson.DEFAULT.toJson(this.getStreamStates()))
            .put("roomStates", Rson.DEFAULT.toJson(this.getRoomStates()))
            .put("features", Rson.DEFAULT.toJson(this.getFeatures()));
    }

    default List<UserPlatform> getSignedInPlatforms() {
        return new LinkedList<>(this.getUserStates().keySet());
    }

    default boolean isMultiUserMode() {
        return this.getUserStates().size() > 1;
    }

    default boolean isSignedOut() {
        return this.getUserStates().size() == 0;
    }

    default int getMaxMessageLength(@NonNull UserPlatform platform) {
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
    default UserPlatform getFirstSignedInPlatform() {
        return this.getSignedInPlatforms().get(0);
    }

}
