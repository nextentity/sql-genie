package io.github.genie.sql.api.tuple;

public interface Tuple9<A, B, C, D, E, F, G, H, I> extends Tuple8<A, B, C, D, E, F, G, H> {
    default I get8() {
        return get(8);
    }
}
