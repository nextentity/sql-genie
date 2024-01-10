package io.github.genie.sql.builder.meta;

public interface Type {

    Type owner();

    Class<?> javaType();

    default boolean hasOwner() {
        return owner() != null;
    }

}
