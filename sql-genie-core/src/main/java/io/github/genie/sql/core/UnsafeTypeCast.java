package io.github.genie.sql.core;

public class UnsafeTypeCast {
    public static <T> T cast(Object o) {
        // noinspection unchecked
        return (T) o;
    }
}
