package io.github.genie.sql.builder;

import io.github.genie.sql.api.Query.Collector;
import io.github.genie.sql.api.Slice;
import io.github.genie.sql.api.Sliceable;
import io.github.genie.sql.builder.QueryStructures.SliceImpl;

import java.util.List;

public interface AbstractCollector<T> extends Collector<T> {

    @Override
    default Slice<T> slice(int offset, int limit) {
        int count = count();
        if (count <= offset) {
            return new SliceImpl<>(List.of(), count, offset, limit);
        } else {
            List<T> list = getList(offset, limit);
            return new SliceImpl<>(list, count, offset, limit);
        }
    }


    @Override
    default <R> R slice(Sliceable<T, R> sliceable) {
        int count = count();
        if (count <= sliceable.offset()) {
            return sliceable.collect(List.of(), count);
        } else {
            List<T> list = getList(sliceable.offset(), sliceable.limit());
            return sliceable.collect(list, count);
        }
    }

}
