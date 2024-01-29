package io.github.genie.sql.builder;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Util {

    private static final Map<Object, String> PROPERTY_NAME_CACHE = new ConcurrentHashMap<>();

    public static String getPropertyName(Serializable getterReference) {
        return PROPERTY_NAME_CACHE.computeIfAbsent(getterReference,
                k -> getPropertyName(getReferenceMethodName(getterReference)));
    }

    public static String getPropertyName(String methodName) {
        StringBuilder builder;
        Objects.requireNonNull(methodName, "methodName");
        if (methodName.length() > 3 && methodName.startsWith("get")) {
            builder = new StringBuilder(methodName.substring(3));
        } else if (methodName.length() > 2 && methodName.startsWith("is")) {
            builder = new StringBuilder(methodName.substring(2));
        } else {
            return methodName;
        }
        if (builder.length() == 1) {
            return builder.toString().toLowerCase();
        }
        if (Character.isUpperCase(builder.charAt(1))) {
            return builder.toString();
        }
        builder.setCharAt(0, Character.toLowerCase(builder.charAt(0)));
        return builder.toString();
    }

    public static String getReferenceMethodName(Serializable getterReference) {
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

    static <T> List<T> concat(Collection<? extends T> collection, Collection<? extends T> value) {
        return Stream.concat(collection.stream(), value.stream())
                .collect(Collectors.toList());
    }

    private Util() {
    }
}
