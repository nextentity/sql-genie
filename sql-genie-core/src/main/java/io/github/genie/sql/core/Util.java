package io.github.genie.sql.core;


import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

final class Util {

    private static final Map<Object, String> PROPERTY_NAME_CACHE = new ConcurrentHashMap<>();

    public static String getPropertyName(Serializable getterReference) {
        return PROPERTY_NAME_CACHE.computeIfAbsent(getterReference,
                k -> getterNameToPropertyName(getMethodReferenceName(getterReference)));
    }

    public static String getterNameToPropertyName(String getterName) {
        StringBuilder builder = null;
        if (getterName != null) {
            if (getterName.length() > 3 && getterName.startsWith("get")) {
                builder = new StringBuilder(getterName.substring(3));
            } else if (getterName.length() > 2 && getterName.startsWith("is")) {
                builder = new StringBuilder(getterName.substring(2));
            }
        }
        Objects.requireNonNull(builder, "the function is not getters");
        if (builder.length() == 1) {
            return builder.toString().toLowerCase();
        }
        if (Character.isUpperCase(builder.charAt(1))) {
            return builder.toString();
        }
        builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));
        return builder.toString();
    }

    public static String getMethodReferenceName(Serializable getterReference) {
        try {
            Class<? extends Serializable> clazz = getterReference.getClass();
            Method method = clazz.getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(getterReference);
            return serializedLambda.getImplMethodName();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static <T> List<T> concat(Collection<T> collection, T value) {
        return Stream.concat(collection.stream(), Stream.of(value)).toList();
    }

    static <T> List<T> concat(Collection<? extends T> collection, Collection<? extends T> value) {
        return Stream.concat(collection.stream(), value.stream()).toList();
    }

    private Util() {
    }
}
