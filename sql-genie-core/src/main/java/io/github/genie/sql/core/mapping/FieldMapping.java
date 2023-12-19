package io.github.genie.sql.core.mapping;

import io.github.genie.sql.core.exception.BeanReflectiveException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface FieldMapping extends Mapping {

    Mapping parent();

    String fieldName();

    Method getter();

    Method setter();

    Field field();

    Class<?> javaType();

    default Object invokeGetter(Object entity) {
        try {
            return getter().invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanReflectiveException(e);
        }
    }

    default void invokeSetter(Object entity, Object value) {
        try {
            setter().invoke(entity, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanReflectiveException(e);
        }
    }

}
