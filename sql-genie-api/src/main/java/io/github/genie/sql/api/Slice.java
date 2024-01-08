package io.github.genie.sql.api;

import java.util.List;

public interface Slice<T> {

    List<T> data();

    long total();

    int offset();

    int limit();

}