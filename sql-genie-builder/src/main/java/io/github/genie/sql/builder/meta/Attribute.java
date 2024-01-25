package io.github.genie.sql.builder.meta;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.builder.Expressions;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.reflect.ReflectUtil;

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

    Type declaringType();

    static Type getDeclaringType(Type type) {
        if (type instanceof Attribute) {
            return ((Attribute) type).declaringType();
        }
        return null;
    }

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
        while (true) {
            if (cur instanceof Attribute) {
                //noinspection PatternVariableCanBeUsed
                Attribute attribute = (Attribute) cur;
                attributes.addFirst(attribute);
                cur = attribute.declaringType();
            } else {
                break;
            }
        }
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
