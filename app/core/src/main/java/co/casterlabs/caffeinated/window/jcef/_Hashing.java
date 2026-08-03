package co.casterlabs.caffeinated.window.jcef;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.Mutable;

class _Hashing {

    static int hash(@Nullable Object o) {
        if (o == null) {
            return 0;
        }

        if (o instanceof Mutable<?>) {
            return ((Mutable<?>) o).hashCode();
        }

        try {
            if (o instanceof Map<?, ?>) {
                return hashOfMap((Map<?, ?>) o);
            } else if (o instanceof Collection<?>) {
                return hashOfCollection((Collection<?>) o);
            } else if (o.getClass().isArray()) {
                return hashOfArray((Object[]) o);
            }
        } catch (StackOverflowError e) {
            // There's probably a circular reference in the maps/collections. We'll ignore
            // them and return the normal hashCode (fall through).
        }

        return Objects.hashCode(o);
    }

    private static int hashOfMap(Map<?, ?> m) {
        // We need to hash all of the keys and values, recursively.
        int result = 1;
        for (Map.Entry<?, ?> entry : m.entrySet().toArray(new Map.Entry[0])) {
            result = 31 * result + hash(entry.getKey());
            result = 31 * result + hash(entry.getValue());
        }
        return result;
    }

    private static int hashOfCollection(Collection<?> c) {
        // We need to hash all of the elements, recursively.
        int result = 1;
        for (Object element : c.toArray()) {
            result = 31 * result + hash(element);
        }
        return result;
    }

    private static int hashOfArray(Object[] a) {
        // We need to hash all of the elements, recursively.
        int result = 1;
        for (Object element : a) {
            result = 31 * result + hash(element);
        }
        return result;
    }

}
