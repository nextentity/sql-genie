package io.github.genie.sql.api;

public interface Column extends Expression, Iterable<String> {
    int size();

    String get(int i);

    Column get(String path);

    Column parent();
}
