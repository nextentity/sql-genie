package io.github.genie.sql.core;


import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class Util {

    static <T> List<T> concat(Collection<T> collection, T value) {
        return Stream.concat(collection.stream(), Stream.of(value)).toList();
    }


    static <T> T cast(Object o) {
        return UnsafeTypeCast.cast(o);
    }
}
