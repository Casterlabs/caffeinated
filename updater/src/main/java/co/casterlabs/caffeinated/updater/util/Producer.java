package co.casterlabs.caffeinated.updater.util;

import org.jetbrains.annotations.Nullable;

public interface Producer<T> {

    public @Nullable T produce() throws Exception;

    public static <T> Producer<T> of(@Nullable T value) {
        return () -> {
            return value;
        };
    }

}
