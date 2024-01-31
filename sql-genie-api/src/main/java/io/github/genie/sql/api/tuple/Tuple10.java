package io.github.genie.sql.api.tuple;

public interface Tuple10<A, B, C, D, E, F, G, H, I, J> extends Tuple9<A, B, C, D, E, F, G, H, I> {
    default J get9() {
        return get(9);
    }
}
