package io.github.genie.sql.core;

import io.github.genie.sql.core.Models.SliceImpl;

import java.util.List;

public interface Slice<T> extends Sliceable {

    List<T> data();

    long total();

    static <T> Slice<T> of(List<T> data, long total, Sliceable sliceable) {
        return new SliceImpl<>(data, total, sliceable);
    }

}