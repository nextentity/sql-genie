package io.github.genie.sql.api.tuple;

public interface Tuple8<A, B, C, D, E, F, G, H> extends Tuple7<A, B, C, D, E, F, G> {
    default H get7() {
        return get(7);
    }
}
