package io.github.genie.sql.api.tuple;

public interface Tuple6<A, B, C, D, E, F> extends Tuple5<A, B, C, D, E> {
    default F get5() {
        return get(5);
    }
}
