package co.casterlabs.caffeinated.window.jcef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.window.AppWindow;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.NonNull;

/**
 * @apiNote This class is not thread-safe.
 */
final class _JSMessages {
    private Map<String, Consumer<JsonElement>> listeners = new HashMap<>();

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    synchronized void handle(@Nullable JsonElement data) {
        this.listeners.values().forEach((listener) -> {
            try {
                listener.accept(data);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /* ------------------------------------ */
    /* ------------------------------------ */
    /* ------------------------------------ */

    /**
     * Deregisters a listener that you previously registered.
     * 
     * @return this instance, for chaining.
     */
    public synchronized _JSMessages off(@NonNull String registrationId) {
        this.listeners.remove(registrationId);
        return this;
    }

    /**
     * Sends a message to the JavaScript environment.
     * 
     * @param   data the data to send
     * 
     * @return       this instance, for chaining.
     * 
     * @apiNote      {@link JsonNull#INSTANCE} in Java is `null` in JS. Any non-JSON
     *               type will be automatically marshalled to JSON for you. Rson
     *               will be used to serialize the object, and you will need to
     *               add @JsonClass or @JsonExpose to your code for this to work.
     * 
     */
    public _JSMessages emit(@NonNull Object data) {
        AppWindow.INSTANCE.executeJavaScript(
            String.format(
                "window.saucer.messages.__internal(%s);",
                Rson.DEFAULT.toJson(data).toString()
            )
        );
        return this;
    }

    /**
     * Allows you to listen for all incoming messages.
     * 
     * @return  an registrationId, which you can use when calling
     *          {@link #off(String)}
     * 
     * @apiNote `null` in JS is {@link JsonNull#INSTANCE} in Java.
     */
    public synchronized String onMessage(@NonNull Consumer<@Nullable JsonElement> callback) {
        String registrationId = UUID.randomUUID().toString();
        this.listeners.put(registrationId, callback);
        return registrationId;
    }

    /**
     * Allows you to listen for all incoming messages.
     * 
     * @return an registrationId, which you can use when calling
     *         {@link #off(String)}
     */
    public String onMessage(@NonNull Runnable callback) {
        return this.onMessage((ignored) -> callback.run());
    }

    /**
     * Allows you to listen for all incoming messages with automatic JSON
     * deserialization
     * 
     * @return an registrationId, which you can use when calling
     *         {@link #off(String)}
     */
    public <T> String onMessage(@NonNull Consumer<@Nullable T> callback, @NonNull TypeToken<T> type) {
        return this.onMessage((data) -> {
            try {
                T typed = Rson.DEFAULT.fromJson(data, type);
                callback.accept(typed);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Allows you to listen for all incoming messages with automatic JSON
     * deserialization.
     * 
     * @apiNote Rson will be used to deserialize the object, and you will need to
     *          add @JsonClass or @JsonExpose to your code for this to work.
     * 
     * @return  an registrationId, which you can use when calling
     *          {@link #off(String)}
     */
    public <T> String onMessage(@NonNull Consumer<@Nullable T> callback, @NonNull Class<T> type) {
        return this.onMessage(callback, TypeToken.of(type));
    }

}
