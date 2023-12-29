package io.github.genie.sql.core;

import java.io.Serializable;

@FunctionalInterface
public interface Path<T, R> extends Serializable {

    R apply(T t);

    interface NumberPath<T, R extends Number & Comparable<R>> extends Path<T, R> {
    }

    interface ComparablePath<T, R extends Comparable<R>> extends Path<T, R> {
    }

    interface BooleanPath<T> extends Path<T, Boolean> {
    }

    interface StringPath<T> extends Path<T, String> {
    }

}
