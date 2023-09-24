package io.github.genie.sql.core.mapping;

public interface Mapping {

    Mapping parent();

    default FieldMapping getFieldMapping(String fieldName) {
        throw new UnsupportedOperationException();
    }

    Class<?> javaType();

}
