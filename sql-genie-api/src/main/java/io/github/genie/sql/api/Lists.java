package io.github.genie.sql.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Lists {

    @SafeVarargs
    static <E> List<E> of(E... es) {
        return Arrays.asList(es);
    }

    static <E> List<E> of(E e) {
        return Collections.singletonList(e);
    }

    static <E> List<E> of() {
        return Collections.emptyList();
    }

}
