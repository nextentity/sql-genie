package io.github.genie.sql.api.tuple;

public interface Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {

    default E get4() {
        return get(4);
    }

}
