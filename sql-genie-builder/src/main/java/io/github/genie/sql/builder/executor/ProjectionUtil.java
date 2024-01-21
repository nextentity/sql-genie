package io.github.genie.sql.builder.executor;

import io.github.genie.sql.builder.TypeCastUtil;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.ReflectUtil;
import io.github.genie.sql.builder.meta.Type;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
public class ProjectionUtil {

    private static final Map<Collection<? extends Attribute>, Schema> SCHEMAS = new ConcurrentHashMap<>();

    public static <R> R newInstance(@NotNull BiFunction<Integer, Class<?>, ?> extractor,
                                    Collection<? extends Attribute> attributes,
                                    Class<?> resultType) {
        Schema schema = getSchema(attributes);
        if (schema.type.javaType() != resultType) {
            throw new IllegalArgumentException();
        }
        return TypeCastUtil.unsafeCast(schema.extract(true, extractor));
    }

    public static Schema getSchema(Collection<? extends Attribute> attributes) {
        return SCHEMAS.computeIfAbsent(attributes, ProjectionUtil::doGetSchema);
    }

    private static Schema doGetSchema(Collection<? extends Attribute> attributes) {
        Map<Type, Property> map = new HashMap<>();
        Schema result = null;
        for (Attribute attribute : attributes) {
            Type cur = attribute;
            while (true) {
                Property property = map.computeIfAbsent(cur, ProjectionUtil::newProperty);
                cur = cur.owner();
                if (cur == null) {
                    if (result == null) {
                        result = (Schema) property;
                    } else if (result != property) {
                        throw new IllegalArgumentException();
                    }
                    break;
                }
            }
        }
        if (result == null) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        for (Attribute attribute : attributes) {
            BaseProperty property = (BaseProperty) map.get(attribute);
            property.index = i++;
        }
        Map<Type, List<Entry<Type, Property>>> attrs = map.entrySet().stream()
                .filter(it -> it.getKey().owner() != null)
                .collect(Collectors.groupingBy(e -> e.getKey().owner()));
        for (Entry<Type, List<Entry<Type, Property>>> entry : attrs.entrySet()) {
            Property property = map.get(entry.getKey());
            List<Entry<Type, Property>> v = entry.getValue();
            if (v != null && !v.isEmpty()) {
                ((Schema) property).setProperties(v.stream()
                        .map(Entry::getValue)
                        .toArray(Property[]::new));
            }
        }
        return result;
    }

    private static Property newProperty(Type type) {
        if (type instanceof io.github.genie.sql.builder.meta.Schema) {
            Class<?> javaType = type.javaType();
            if (javaType.isInterface()) {
                return new InterfaceSchema(type);
            } else if (javaType.isRecord()) {
                return new RecordSchema(type);
            } else {
                return new BeanSchema(type);
            }
        } else {
            return new BaseProperty((Attribute) type);
        }
    }


    public abstract static class Schema implements Property {
        protected Property[] properties;
        protected final Type type;

        public Schema(Type type) {
            this.type = type;
        }

        @Override
        public Attribute attribute() {
            return (Attribute) type;
        }

        public void setProperties(Property[] properties) {
            this.properties = properties;
        }
    }

    public static class InterfaceSchema extends Schema {
        public InterfaceSchema(Type type) {
            super(type);
        }

        @Override
        public Object extract(boolean root, BiFunction<Integer, Class<?>, ?> extractor) {
            Map<Method, Object> map = new HashMap<>();
            boolean hasNonnullProperty = false;
            for (Property property : properties) {
                Object extract = property.extract(false, extractor);
                hasNonnullProperty = hasNonnullProperty || extract != null;
                map.put(property.attribute().getter(), extract);
            }
            if (root || hasNonnullProperty) {
                return newProxyInstance(properties, type.javaType(), map);
            } else {
                return null;
            }
        }
    }

    public static class RecordSchema extends Schema {
        public Class<?>[] parameterTypes;

        public RecordSchema(Type type) {
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
        public Object extract(boolean root, BiFunction<Integer, Class<?>, ?> extractor) {

            try {
                Class<?> resultType = type.javaType();
                RecordComponent[] components = resultType.getRecordComponents();
                Object[] args = new Object[components.length];
                boolean hasNonnullProperty = false;
                for (int i = 0; i < properties.length; i++) {
                    Object extract = properties[i].extract(false, extractor);
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

    public static class BeanSchema extends Schema {
        public BeanSchema(Type type) {
            super(type);
        }

        @Override
        public Object extract(boolean root, BiFunction<Integer, Class<?>, ?> extractor) {
            Object result = null;
            for (Property property : properties) {
                Object value = property.extract(false, extractor);
                if (value != null) {
                    if (result == null) {
                        result = newInstance(type.javaType());
                    }
                    property.attribute().set(result, value);
                }
            }
            if (root && result == null) {
                result = newInstance(type.javaType());
            }
            return result;
        }
    }

    public interface Property {
        Attribute attribute();

        Object extract(boolean root, BiFunction<Integer, Class<?>, ?> extractor);
    }


    public static class BaseProperty implements Property {
        private int index;
        private final Attribute attribute;

        public BaseProperty(Attribute attribute) {
            this.attribute = attribute;
        }


        @Override
        public Attribute attribute() {
            return attribute;
        }

        @Override
        public Object extract(boolean root, BiFunction<Integer, Class<?>, ?> extractor) {
            return extractor.apply(index, attribute.javaType());
        }
    }

    @NotNull
    private static Object newInstance(Class<?> resultType) {
        try {
            return resultType.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }

    @NotNull
    public static Object newProxyInstance(Property[] fields, @NotNull Class<?> resultType, Map<Method, Object> map) {
        ClassLoader classLoader = resultType.getClassLoader();
        Class<?>[] interfaces = {resultType};
        return Proxy.newProxyInstance(classLoader, interfaces, new Handler(fields, resultType, map));
    }

    @Data
    @Accessors(fluent = true)
    private static class Handler implements InvocationHandler {
        private static final Method EQUALS = getEqualsMethod();
        private final Property[] fields;
        private final Class<?> resultType;
        private final Map<Method, Object> data;

        @SneakyThrows
        @NotNull
        private static Method getEqualsMethod() {
            return Object.class.getMethod("equals", Object.class);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (data.containsKey(method)) {
                return data.get(method);
            }
            if (EQUALS.equals(method)) {
                return equals(proxy, args[0]);
            }
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if (method.isDefault()) {
                return ReflectUtil.invokeDefaultMethod(proxy, method, args);
            }
            throw new AbstractMethodError(method.toString());
        }

        @NotNull
        private Object equals(Object proxy, Object other) {
            if (proxy == other) {
                return true;
            }
            if (other == null || !Proxy.isProxyClass(other.getClass())) {
                return false;
            }
            InvocationHandler handler = Proxy.getInvocationHandler(other);
            return equals(handler);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Handler handler = (Handler) o;
            return resultType.equals(handler.resultType) && data.equals(handler.data);
        }

        @Override
        public int hashCode() {
            int result = data.hashCode();
            result = 31 * result + resultType.hashCode();
            return result;
        }

        @Override
        public String toString() {
            Map<String, Object> stringMap = new HashMap<>();
            for (Property property : fields) {
                Attribute attribute = property.attribute();
                stringMap.put(attribute.name(), data.get(attribute.getter()));
            }
            return resultType.getSimpleName() + stringMap;
        }
    }


}
