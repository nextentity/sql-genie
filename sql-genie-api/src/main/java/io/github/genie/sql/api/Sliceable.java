package io.github.genie.sql.api;


import java.util.List;

public interface Sliceable<T, U> {

    int offset();

    int limit();

    U collect(List<T> list, int total);

}
