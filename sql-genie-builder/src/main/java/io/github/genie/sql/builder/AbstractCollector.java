package io.github.genie.sql.builder;

import io.github.genie.sql.api.Slice;
import io.github.genie.sql.api.Sliceable;
import io.github.genie.sql.builder.QueryStructures.SliceImpl;
import io.github.genie.sql.builder.QueryStructures.SliceableImpl;
import io.github.genie.sql.api.Query.Collector;

import java.util.List;

public interface AbstractCollector<T> extends Collector<T> {

    @Override
    default Slice<T> slice(int offset, int limit) {
        return slice(new SliceableImpl(offset, limit));
    }

    @Override
    default Slice<T> slice(Sliceable sliceable) {
        int count = count();
        if (count <= sliceable.offset()) {
            return new SliceImpl<>(List.of(), count, sliceable);
        } else {
            List<T> list = getList(sliceable.offset(), sliceable.limit());
            return new SliceImpl<>(list, count, sliceable);
        }
    }

}
