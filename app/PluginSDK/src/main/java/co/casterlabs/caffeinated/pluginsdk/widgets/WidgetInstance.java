package co.casterlabs.caffeinated.pluginsdk.widgets;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonNull;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public abstract class WidgetInstance implements Closeable {
    private @Getter WidgetInstanceMode instanceMode;
    private @Getter String connectionId;
    private @Getter Widget widget;
    private FastLogger logger;

    private MultiValuedMap<String, Consumer<JsonElement>> eventHandlers = new HashSetValuedHashMap<>();
    private Set<Runnable> onCloseHandlers = new HashSet<>();

    protected WidgetInstance(WidgetInstanceMode instanceMode, String connectionId, Widget widget) {
        this.instanceMode = instanceMode;
        this.connectionId = connectionId;
        this.widget = widget;
        this.logger = new FastLogger("WidgetInstance, id=" + connectionId + ", widget=" + widget.getNamespace());
    }

    /* ------------ */
    /* Used by the IMPL */
    /* ------------ */

    protected void broadcast(@NonNull String type, @NonNull JsonElement message) {
        if (type.startsWith("__internal:")) {
            String internalQueryType = type.substring("__internal:".length());

            switch (internalQueryType) {
                case "resource_poll": {
                    String resourceId = message.getAsString();

                    String content = null;
                    String mime = null;

                    try {
                        Pair<String, String> result = this.widget.getPlugin().getResource(resourceId);
                        content = result.a();
                        mime = result.b();
                    } catch (Throwable t) {
                        this.logger.severe("An error occured whilst getting a resource:\n%s", t);
                    }

                    try {
                        this.emit0(
                            "__internal:resource_poll:" + resourceId,
                            new JsonObject().put("content", content)
                                .put("mime", mime)
                        );
                    } catch (IOException ignored) {}

                    break;
                }

                default:
                    break;
            }
        } else {
            this.eventHandlers
                .get(type)
                .forEach((c) -> c.accept(message));
        }
    }

    protected void onClose() {
        this.onCloseHandlers
            .forEach((r) -> r.run());
    }

    /* ------------ */
    /* Events       */
    /* ------------ */

    public void on(@NonNull String type, @NonNull Consumer<JsonElement> handler) {
        this.eventHandlers.put(type, handler);
    }

    public void on(@NonNull String type, @NonNull Runnable handler) {
        this.eventHandlers.put(type, (ignored) -> handler.run());
    }

    public void onClose(@NonNull Runnable handler) {
        this.onCloseHandlers.add(handler);
    }

    public final void emit(@NonNull String type, @NonNull JsonElement message) throws IOException {
        assert !type.startsWith("__internal:") : "__internal is a reserved prefix.";

        this.emit0(type, message);
    }

    protected abstract void emit0(@NonNull String type, @NonNull JsonElement message) throws IOException;

    public abstract @NonNull String getRemoteIpAddress();

    // Convenience method for #emit(String, JsonElement)
    public void emit(@NonNull String type, @Nullable String message) throws IOException {
        this.emit(type, Rson.DEFAULT.toJson(message));
    }

    // Convenience method for #emit(String, JsonElement)
    public void emit(@NonNull String type) throws IOException {
        this.emit(type, JsonNull.INSTANCE);
    }

    /* ------------ */
    /* Misc         */
    /* ------------ */

    public abstract void onSettingsUpdate();

    public abstract void onKoiEvent(@NonNull KoiEvent event) throws IOException;

    public abstract void onKoiStaticsUpdate(@NonNull JsonObject json) throws IOException;

    public abstract void onMusicUpdate(@NonNull JsonObject music) throws IOException;

    public abstract void onAppearanceUpdate(@NonNull JsonObject preferences) throws IOException;

}
