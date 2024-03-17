package io.github.genie.sql.api.tuple;

public interface Tuple2<A, B> extends Tuple {
    default A get0() {
        return get(0);
    }

    default B get1() {
        return get(1);
    }

}
