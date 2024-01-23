package io.github.genie.sql.builder.meta;

public interface Type {

    Type owner();

    Class<?> javaType();

    String name();

    default boolean hasOwner() {
        return owner() != null;
    }

}
