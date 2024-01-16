package io.github.genie.sql.builder.meta;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.builder.Expressions;
import io.github.genie.sql.builder.exception.BeanReflectiveException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface Attribute extends Type {

    String name();

    Method getter();

    Method setter();

    Field field();

    default Object get(Object entity) {
        try {
            Method getter = getter();
            if (getter != null && ReflectUtil.isAccessible(getter, entity)) {
                return getter.invoke(entity);
            } else {
                return ReflectUtil.getFieldValue(field(), entity);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanReflectiveException(e);
        }
    }

    default void set(Object entity, Object value) {
        try {
            Method setter = setter();
            if (setter != null && ReflectUtil.isAccessible(setter, entity)) {
                setter.invoke(entity, value);
            } else {
                ReflectUtil.setFieldValue(field(), entity, value);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanReflectiveException(e);
        }
    }

    default List<? extends Attribute> referencedAttributes() {
        Type cur = this;
        ArrayDeque<Attribute> attributes = new ArrayDeque<>(2);
        do {
            if (cur instanceof Attribute) {
                attributes.addFirst((Attribute) cur);
            }
            cur = cur.owner();
        } while (cur.hasOwner());
        //noinspection Java9CollectionFactory
        return Collections.unmodifiableList(new ArrayList<>(attributes));
    }

    default Column column() {
        List<? extends Attribute> attributes = referencedAttributes();
        List<String> paths = attributes.stream()
                .map(Attribute::name)
                .collect(Collectors.toList());
        return Expressions.column(paths);
    }

}
