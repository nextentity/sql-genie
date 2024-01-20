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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("PatternVariableCanBeUsed")
public class ProjectionUtil {

    static Map<Collection<? extends Attribute>, Map<Type, List<Type>>> schemas = new ConcurrentHashMap<>();

    public static <R> R newProjectionResult(@NotNull BiFunction<Integer, Class<?>, ?> extractor,
                                            Collection<? extends Attribute> attributes,
                                            Class<?> resultType) {
        Map<Type, Map<Type, Object>> schemaData = new HashMap<>();
        Map<Type, List<Type>> schema = getSchema(attributes);
        int i = 0;
        for (Attribute attribute : attributes) {
            Object value = extractor.apply(i++, attribute.javaType());
            Map<Type, Object> m = schemaData.computeIfAbsent(attribute.owner(), __ -> new HashMap<>());
            m.put(attribute, value);
        }
        Object result = null;
        for (Entry<Type, List<Type>> entry : schema.entrySet()) {
            Type instanceType = entry.getKey();
            List<Type> attrs = entry.getValue();
            Map<Type, Object> data = schemaData.get(instanceType);
            if (instanceType.hasOwner()) {
                if (instanceType instanceof Attribute) {
                    Type k = instanceType.owner();
                    Map<Type, Object> ownerData = schemaData.computeIfAbsent(k, __ -> new HashMap<>());
                    Object o;
                    o = newInstance(instanceType, attrs, data);
                    ownerData.put(instanceType, o);
                } else {
                    log.debug("ignored type: " + instanceType);
                }
            } else if (result == null) {
                if (instanceType.javaType() != resultType) {
                    throw new IllegalStateException();
                }
                result = newInstance(instanceType, attrs, data);
            } else {
                throw new IllegalStateException();
            }
        }
        return TypeCastUtil.unsafeCast(result);
    }

    @Nullable
    private static Object newInstance(Type key, List<Type> value, Map<Type, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        if (key.javaType().isInterface()) {
            return newInterfaceInstance(value, key.javaType(), data);
        } else if (key.javaType().isRecord()) {
            return newRecordInstance(key.javaType(), data);
        } else {
            return newBeanInstance(value, key.javaType(), data);
        }
    }

    @NotNull
    private static Map<Type, List<Type>> getSchema(Collection<? extends Attribute> attributes) {
        return schemas.computeIfAbsent(attributes, ProjectionUtil::doGetSchema);
    }

    private static Map<Type, List<Type>> doGetSchema(Collection<? extends Attribute> attributes) {
        Map<Type, List<Type>> schema = new TreeMap<>(Comparator.comparingInt(Type::layer).reversed());
        for (Attribute attribute : attributes) {
            setAttributes(attribute, schema);
        }
        return schema;
    }

    private static void setAttributes(Type type, Map<Type, List<Type>> schema) {
        Type owner = type.owner();
        if (owner != null) {
            List<Type> types = schema.get(owner);
            if (types == null) {
                types = new ArrayList<>();
                schema.put(owner, types);
                setAttributes(owner, schema);
            }
            schema.computeIfAbsent(owner, k -> new ArrayList<>()).add(type);
        }
    }

    private static Object newBeanInstance(List<Type> attributes, Class<?> type, Map<Type, Object> data) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            Object o = constructor.newInstance();
            for (Type v : attributes) {
                if (v instanceof Attribute) {
                    Attribute attribute = (Attribute) v;
                    Object attrVal = data.get(attribute);
                    if (attrVal != null) {
                        attribute.set(o, attrVal);
                    }
                }
            }
            return o;
        } catch (
                ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }

    private static Object newRecordInstance(Class<?> resultType, Map<Type, Object> data) {
        Map<String, Object> d = data.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Entry::getValue));
        try {
            RecordComponent[] components = resultType.getRecordComponents();
            Object[] args = new Object[components.length];
            Class<?>[] parameterTypes = new Class[components.length];
            for (int i = 0; i < components.length; i++) {
                RecordComponent component = components[i];
                args[i] = d.get(component.getName());
                parameterTypes[i] = component.getType();
            }
            Constructor<?> constructor = resultType.getDeclaredConstructor(parameterTypes);
            Object row = constructor.newInstance(args);
            return TypeCastUtil.unsafeCast(row);
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }

    @NotNull
    public static Object newInterfaceInstance(Collection<? extends Type> attributes,
                                              @NotNull Class<?> resultType,
                                              Map<Type, Object> map) {
        ClassLoader classLoader = resultType.getClassLoader();
        Class<?>[] interfaces = {resultType};
        Map<Method, Object> m = new HashMap<>();
        for (Entry<Type, Object> entry : map.entrySet()) {
            if (entry.getKey() instanceof Attribute attribute && attribute.getter() != null) {
                m.put(attribute.getter(), entry.getValue());
            }
        }
        return Proxy.newProxyInstance(classLoader, interfaces, new Handler(attributes, resultType, m));
    }

    @Data
    @Accessors(fluent = true)
    private static class Handler implements InvocationHandler {
        private static final Method EQUALS = getEqualsMethod();
        private final Collection<? extends Type> attributes;
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
            for (Type attribute : attributes) {
                if (attribute instanceof Attribute) {
                    stringMap.put(attribute.name(), data.get(((Attribute) attribute).getter()));
                }
            }
            return resultType.getSimpleName() + stringMap;
        }
    }

}
