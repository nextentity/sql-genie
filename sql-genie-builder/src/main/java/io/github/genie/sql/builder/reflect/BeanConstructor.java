package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.meta.Type;

public class BeanConstructor extends ObjectConstructor {
    public BeanConstructor(Type type) {
        super(type);
    }

    @Override
    public Object newInstance(Object[] args) {
        Object result = null;
        for (Property property : properties) {
            Object value = property.newInstance(args);
            if (value != null) {
                if (result == null) {
                    result = ReflectUtil.newInstance(type.javaType());
                }
                property.attribute().set(result, value);
            }
        }
        if (root && result == null) {
            result = ReflectUtil.newInstance(type.javaType());
        }
        return result;
    }
}
