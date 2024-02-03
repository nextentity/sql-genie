package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.meta.Type;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InterfaceConstructor extends ObjectConstructor {
    public InterfaceConstructor(Type type) {
        super(type);
    }

    @Override
    public Object newInstance(Object[] arguments) {
        Map<Method, Object> map = new HashMap<>();
        boolean hasNonnullProperty = false;
        for (Property property : properties) {
            Object extract = property.newInstance(arguments);
            hasNonnullProperty = hasNonnullProperty || extract != null;
            map.put(property.attribute().getter(), extract);
        }
        if (root || hasNonnullProperty) {
            return ReflectUtil.newProxyInstance(properties, type.javaType(), map);
        } else {
            return null;
        }
    }
}
