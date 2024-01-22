package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.meta.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;

public class RecordConstructor extends ObjectConstructor {
    public Class<?>[] parameterTypes;

    public RecordConstructor(Type type) {
        super(type);
    }

    public void setProperties(Property[] properties) {
        Map<String, Property> map = new HashMap<>();
        for (Property property : properties) {
            map.put(property.attribute().name(), property);
        }
        Class<?> resultType = type.javaType();
        RecordComponent[] components = resultType.getRecordComponents();
        parameterTypes = new Class[components.length];
        Property[] argProperties = new Property[components.length];
        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            argProperties[i] = map.get(component.getName());
            parameterTypes[i] = component.getType();
        }
        this.properties = argProperties;
    }

    @Override
    public Object newInstance(Object[] extractor) {
        try {
            Class<?> resultType = type.javaType();
            RecordComponent[] components = resultType.getRecordComponents();
            Object[] args = new Object[components.length];
            boolean hasNonnullProperty = false;
            for (int i = 0; i < properties.length; i++) {
                Object extract = properties[i].newInstance(extractor);
                hasNonnullProperty = hasNonnullProperty || extract != null;
                args[i] = extract;
            }
            if (!root && !hasNonnullProperty) {
                return null;
            }
            Constructor<?> constructor = resultType.getDeclaredConstructor(parameterTypes);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }
}
