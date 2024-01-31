package io.github.genie.sql.builder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class ArrayIterator<T> implements Iterator<T> {
    int index = 0;
    private final T[] data;

    public ArrayIterator(T[] data) {
        this.data = Objects.requireNonNull(data);
    }

    public boolean hasNext() {
        return index < data.length;
    }

    public T next() {
        if (index >= data.length) {
            throw new NoSuchElementException();
        } else {
            return data[this.index++];
        }
    }
}
