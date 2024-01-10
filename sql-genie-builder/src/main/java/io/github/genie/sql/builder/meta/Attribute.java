package io.github.genie.sql.builder.meta;

import io.github.genie.sql.builder.TypeCastUtil;
import io.github.genie.sql.builder.exception.BeanReflectiveException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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
        Type owner = this;
        ArrayDeque<Attribute> attributes = new ArrayDeque<>(2);
        while (owner.hasOwner()) {
            owner = owner.owner();
            if (owner instanceof Attribute) {
                attributes.addFirst((Attribute) owner);
            }
        }
        //noinspection Java9CollectionFactory
        return Collections.unmodifiableList(new ArrayList<>(attributes));
    }

}
