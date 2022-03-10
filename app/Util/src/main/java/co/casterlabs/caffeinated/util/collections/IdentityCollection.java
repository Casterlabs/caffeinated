package co.casterlabs.caffeinated.util.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IdentityCollection<T> implements Collection<T> {
    private Collection<T> target;

    @Override
    public int hashCode() {
        return Objects.hash(this.target.toArray());
    }

    // Forward.

    @Override
    public int size() {
        return this.target.size();
    }

    @Override
    public boolean isEmpty() {
        return this.target.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.target.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return this.target.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.target.toArray();
    }

    @Override
    public <V> V[] toArray(V[] a) {
        return this.target.toArray(a);
    }

    @Override
    public boolean add(T e) {
        return this.target.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.target.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.target.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.target.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.target.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.target.retainAll(c);
    }

    @Override
    public void clear() {
        this.target.clear();
    }

}
