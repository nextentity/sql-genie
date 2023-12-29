package io.github.genie.sql.core;

import io.github.genie.sql.core.Models.SliceableImpl;

public interface Sliceable {

    int offset();

    int limit();

    static Sliceable of(int offset, int limit) {
        return new SliceableImpl(offset, limit);
    }


}
