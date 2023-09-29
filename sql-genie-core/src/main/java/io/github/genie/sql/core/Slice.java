package io.github.genie.sql.core;

import java.util.List;

public interface Slice<T> {

    List<T> data();

    long total();

    Sliceable sliceable();

    static Sliceable sliceable(int offset, int limit) {
        return new Models.SliceableImpl(offset, limit);
    }

    interface Sliceable {

        int offset();

        int size();

    }
}