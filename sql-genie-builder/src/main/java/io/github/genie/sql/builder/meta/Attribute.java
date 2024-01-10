package io.github.genie.sql.builder.meta;

import io.github.genie.sql.builder.TypeCastUtil;
import io.github.genie.sql.builder.exception.BeanReflectiveException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Attribute extends Type {

    String name();

    Method getter();

    Method setter();

    Field field();

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

    default List<? extends Attribute> referencedAttribute() {
        //noinspection SimplifyStreamApiCallChains
        return Stream.iterate(owner(), Type::hasOwner, Type::owner)
                .filter(it -> it instanceof Attribute)
                .map(TypeCastUtil::<Attribute>unsafeCast)
                .collect(Collectors.toUnmodifiableList());
    }

}
