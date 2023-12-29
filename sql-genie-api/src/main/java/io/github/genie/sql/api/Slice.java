package io.github.genie.sql.api;

import java.util.List;

public interface Slice<T> extends Sliceable {

    List<T> data();

    long total();

}