package io.github.genie.sql.core.mapping;

public interface Mapping {

    Mapping parent();

    Class<?> javaType();

}
