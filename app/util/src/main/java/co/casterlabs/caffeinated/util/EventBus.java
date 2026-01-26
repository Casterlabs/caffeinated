package co.casterlabs.caffeinated.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class EventBus<T, E> {
    private final Map<T, Map<UUID, Consumer<? extends E>>> listeners = new HashMap<>();

    public Subscription subscribe(@NonNull T type, Consumer<? extends E> listener) {
        UUID id = UUID.randomUUID();
        synchronized (this.listeners) {
            this.listeners.computeIfAbsent(type, k -> new HashMap<>()).put(id, listener);
        }
        return new Subscription(type, id);
    }

    public void post(@NonNull T type, @NonNull E event) {
        synchronized (this.listeners) {
            Map<UUID, Consumer<? extends E>> typeListeners = this.listeners.get(type);
            if (typeListeners != null) {
                for (Consumer<? extends E> listener : typeListeners.values()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Consumer<E> consumer = (Consumer<E>) listener;
                        consumer.accept(event);
                    } catch (Exception ex) {
                        FastLogger.logStatic(LogLevel.SEVERE, "Exception occurred while posting event to listener:\n%s", ex);
                    }
                }
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class Subscription {
        private final T type;
        private final UUID id;

        public void revoke() {
            synchronized (EventBus.this.listeners) {
                EventBus.this.listeners.get(this.type).remove(this.id);
            }
        }

    }

}
