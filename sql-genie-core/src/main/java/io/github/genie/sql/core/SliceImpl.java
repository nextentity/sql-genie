package io.github.genie.sql.core;

import java.util.List;

public class SliceImpl<T> implements Slice<T> {

    List<T> data;

    long total;

    Slice.Sliceable sliceable;


    public SliceImpl(List<T> data, long total, Slice.Sliceable sliceable) {
        this.data = data;
        this.total = total;
        this.sliceable = sliceable;
    }


    @Override
    public List<T> data() {
        return data;
    }

    @Override
    public long total() {
        return total;
    }

    @Override
    public Sliceable sliceable() {
        return sliceable;
    }

    record SliceableImpl(int offset, int size) implements Sliceable {
    }

}
