package co.casterlabs.caffeinated.util.collections;

import java.util.HashMap;
import java.util.Objects;

public class ValueIdentityHashMap<K, V> extends HashMap<K, V> {
    private static final long serialVersionUID = -2296870748986693758L;

    @Override
    public int hashCode() {
        return Objects.hash(this.values().toArray());
    }

}
