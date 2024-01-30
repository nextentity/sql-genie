package io.github.genie.sql.builder;

import io.github.genie.sql.api.Path;
import lombok.Getter;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class PathReference {

    private static final Pattern RETURN_TYPE_PATTERN = Pattern.compile("\\(.*\\)L(.*);");
    private static final Pattern PARAMETER_TYPE_PATTERN = Pattern.compile("\\((.*)\\).*");

    private static final Map<Path<?, ?>, PathReference> map = new ConcurrentHashMap<>();

    private final String propertyName;
    private final Class<?> returnType;
    private final Class<?> entityType;
    private final SerializedLambda serializedLambda;

    private PathReference(SerializedLambda serializedLambda) {
        this.serializedLambda = serializedLambda;
        this.propertyName = getPropertyName(serializedLambda.getImplMethodName());
        this.returnType = getReturnType(serializedLambda);
        this.entityType = getEntityType(serializedLambda);
    }

    public static <T, R> PathReference of(Path<T, R> path) {
        return map.computeIfAbsent(path, PathReference::createPathReference);
    }

    private static PathReference createPathReference(Path<?, ?> path) {
        try {
            Class<? extends Serializable> clazz = path.getClass();
            Method method = clazz.getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(path);
            int implMethodKind = serializedLambda.getImplMethodKind();
            if (implMethodKind != MethodHandleInfo.REF_invokeVirtual
                    && implMethodKind != MethodHandleInfo.REF_invokeInterface) {
                throw new IllegalStateException("implMethodKind error: required "
                        + MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_invokeVirtual)
                        + " or " + MethodHandleInfo.referenceKindToString(MethodHandleInfo.REF_invokeInterface)
                        + " but is " + MethodHandleInfo.referenceKindToString(implMethodKind));
            }
            return new PathReference(serializedLambda);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getPropertyName(String methodName) {
        Objects.requireNonNull(methodName, "methodName");
        StringBuilder builder;
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

    private static Class<?> getReturnType(SerializedLambda serializedLambda) {
        String expr = serializedLambda.getInstantiatedMethodType();
        Matcher matcher = RETURN_TYPE_PATTERN.matcher(expr);
        if (!matcher.find() || matcher.groupCount() != 1) {
            throw new IllegalStateException("Failed to get Lambda information");
        }
        String className = matcher.group(1).replace("/", ".");
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }


    private static Class<?> getEntityType(SerializedLambda serializedLambda) {
        String expr = serializedLambda.getInstantiatedMethodType();
        Matcher matcher = PARAMETER_TYPE_PATTERN.matcher(expr);
        if (!matcher.find() || matcher.groupCount() != 1) {
            throw new IllegalStateException("Failed to get Lambda information");
        }
        expr = matcher.group(1);
        return Arrays.stream(expr.split(";"))
                .filter(s -> !s.isBlank())
                .map(s -> s.replace("L", "").replace("/", "."))
                .map(s -> {
                    try {
                        return Class.forName(s);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .findFirst()
                .orElseThrow();
    }

}
