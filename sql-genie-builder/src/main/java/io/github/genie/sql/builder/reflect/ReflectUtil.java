package io.github.genie.sql.builder.reflect;

import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.ObjectType;
import io.github.genie.sql.builder.meta.Type;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReflectUtil {

    static final Map<Collection<? extends Attribute>, ObjectConstructor> CONSTRUCTORS = new ConcurrentHashMap<>();

    @Nullable
    public static Field getDeclaredField(@NotNull Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getDeclaredField(superclass, name);
            }
        }
        return null;
    }

    @SneakyThrows
    public static <T> void copyTargetNullFields(T src, T target, Class<T> type) {
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            Method reader = descriptor.getReadMethod();
            Method writer = descriptor.getWriteMethod();
            if (reader != null && writer != null) {
                Object tv = reader.invoke(target);
                if (tv != null) {
                    continue;
                }
                Object sv = reader.invoke(src);
                if (sv != null) {
                    writer.invoke(target, sv);
                }
            }
        }
    }

    public static InstanceConstructor getRowInstanceConstructor(Collection<? extends Attribute> attributes, Class<?> resultType) {
        ObjectConstructor schema = CONSTRUCTORS.computeIfAbsent(attributes, ReflectUtil::doGetConstructor);
        if (schema.type.javaType() != resultType) {
            throw new IllegalArgumentException();
        }
        return schema;
    }

    private static ObjectConstructor doGetConstructor(Collection<? extends Attribute> attributes) {
        Map<Type, Property> map = new HashMap<>();
        ObjectConstructor result = null;
        for (Attribute attribute : attributes) {
            Type cur = attribute;
            while (true) {
                Property property = map.computeIfAbsent(cur, ReflectUtil::newProperty);
                cur = Attribute.getDeclaringType(cur);
                if (cur == null) {
                    if (result == null) {
                        result = (ObjectConstructor) property;
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
            PropertyImpl property = (PropertyImpl) map.get(attribute);
            property.setIndex(i++);
        }
        Map<Type, List<Entry<Type, Property>>> attrs = map.entrySet().stream()
                .filter(it -> Attribute.getDeclaringType(it.getKey()) != null)
                .collect(Collectors.groupingBy(e -> Attribute.getDeclaringType(e.getKey())));
        for (Entry<Type, List<Entry<Type, Property>>> entry : attrs.entrySet()) {
            Property property = map.get(entry.getKey());
            List<Entry<Type, Property>> v = entry.getValue();
            if (v != null && !v.isEmpty()) {
                ((ObjectConstructor) property).setProperties(v.stream()
                        .map(Entry::getValue)
                        .toArray(Property[]::new));
            }
        }
        result.root = true;
        return result;
    }

    private static Property newProperty(Type type) {
        if (type instanceof ObjectType) {
            Class<?> javaType = type.javaType();
            if (javaType.isInterface()) {
                return new InterfaceConstructor(type);
            } else {
                return new BeanConstructor(type);
            }
        } else {
            return new PropertyImpl((Attribute) type);
        }
    }

    @NotNull
    public static Object newInstance(Class<?> resultType) {
        try {
            return resultType.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }

    public static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        final float version = Float.parseFloat(System.getProperty("java.class.version"));
        if (version <= 52) {
            final Constructor<Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);

            final Class<?> clazz = method.getDeclaringClass();
            MethodHandles.Lookup lookup = constructor.newInstance(clazz);
            return lookup
                    .in(clazz)
                    .unreflectSpecial(method, clazz)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        } else {
            return MethodHandles.lookup()
                    .findSpecial(
                            method.getDeclaringClass(),
                            method.getName(),
                            MethodType.methodType(method.getReturnType(), new Class[0]),
                            method.getDeclaringClass()
                    )
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }
    }

    public static Object getFieldValue(Field field, Object instance) throws IllegalAccessException {
        checkAccessible(field, instance);
        return field.get(instance);
    }

    public static void setFieldValue(Field field, Object instance, Object value) throws IllegalAccessException {
        checkAccessible(field, instance);
        field.set(instance, value);
    }

    private static void checkAccessible(Field field, Object instance) {
        if (!isAccessible(field, instance)) {
            field.setAccessible(true);
        }
    }

    public static boolean isAccessible(AccessibleObject accessibleObject, Object instance) {
        return accessibleObject.isAccessible();
    }

    @NotNull
    public static Object newProxyInstance(Property[] fields, @NotNull Class<?> resultType, Map<Method, Object> map) {
        ClassLoader classLoader = resultType.getClassLoader();
        Class<?>[] interfaces = {resultType};
        return Proxy.newProxyInstance(classLoader, interfaces, new InstanceInvocationHandler(fields, resultType, map));
    }

}
