package co.casterlabs.caffeinated.app.util;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModifiableArray<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Function<Integer, T[]> arrayProvider;

    private volatile T[] items;

    public ModifiableArray(Function<Integer, T[]> arrayProvider) {
        this.arrayProvider = arrayProvider;
        this.items = arrayProvider.apply(0);
    }

    public void add(T itemToAdd) {
        this.lock.lock();
        try {
            T[] newArr = this.arrayProvider.apply(this.items.length + 1);
            System.arraycopy(this.items, 0, newArr, 0, this.items.length);
            newArr[this.items.length] = itemToAdd;

            this.items = newArr;
        } finally {
            this.lock.unlock();
        }
    }

    public void remove(T itemToRemove) {
        this.lock.lock();
        try {
            int occurrences = 0;
            for (T i : this.items) {
                if (i == itemToRemove) {
                    occurrences++;
                }
            }
            if (occurrences == 0) return;

            T[] newArr = this.arrayProvider.apply(this.items.length - occurrences);
            int newArrIdx = 0;
            for (T i : this.items) {
                if (i != itemToRemove) {
                    newArr[newArrIdx] = i;
                    newArrIdx++;
                }
            }

            this.items = newArr;
        } finally {
            this.lock.unlock();
        }
    }

    public T[] get() {
        return this.items;
    }

    public int length() {
        return this.items.length;
    }

    public void forEach(Consumer<T> consumer) {
        T[] items = this.items;
        for (T i : items) {
            try {
                consumer.accept(i);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

}
