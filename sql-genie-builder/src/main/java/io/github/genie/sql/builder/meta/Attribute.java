package io.github.genie.sql.builder.meta;

import io.github.genie.sql.builder.exception.BeanReflectiveException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Attribute extends Type {

    String name();

    Method getter();

    Method setter();

    Field field();

    Class<?> javaType();

    default Object get(Object entity) {
        try {
            Method getter = getter();
            if (getter != null) {
                return getter.invoke(entity);
            } else {
                return field().get(entity);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanReflectiveException(e);
        }
    }

    default void set(Object entity, Object value) {
        try {
            Method setter = setter();
            if (setter != null) {
                setter.invoke(entity, value);
            } else {
                field().set(entity, value);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanReflectiveException(e);
        }
    }

}
