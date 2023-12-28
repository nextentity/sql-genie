package io.github.genie.sql.core.executor;

import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.Mapping;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ProjectionUtil {

    @NotNull
    public static <R> R getBeanResult(@NotNull BiFunction<Integer, Class<?>, ?> resultSet,
                                      @NotNull List<? extends FieldMapping> fields,
                                      Class<?> resultType) {
        Object row = newInstance(resultType);
        int column = 0;
        for (FieldMapping projection : fields) {
            int deep = 0;
            Mapping cur = projection;
            while (cur != null) {
                deep++;
                cur = cur.owner();
            }
            FieldMapping[] mappings = new FieldMapping[deep - 1];
            cur = projection;
            for (int i = mappings.length - 1; i >= 0; i--) {
                mappings[i] = (FieldMapping) cur;
                cur = cur.owner();
            }
            Class<?> fieldType = projection.javaType();
            Object value = resultSet.apply(column++, fieldType);
            if (value == null && mappings.length > 1) {
                continue;
            }
            Object obj = row;
            for (int i = 0; i < mappings.length - 1; i++) {
                FieldMapping mapping = mappings[i];
                Object tmp = mapping.invokeGetter(obj);
                if (tmp == null) {
                    tmp = newInstance(mapping.javaType());
                    mapping.invokeSetter(obj, tmp);
                }
                obj = tmp;
            }
            projection.invokeSetter(obj, value);
        }
        // noinspection unchecked
        return (R) (row);
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
    public static <R> R getRecordResult(@NotNull BiFunction<Integer, Class<?>, ?> resultSet,
                                        @NotNull List<? extends FieldMapping> fields,
                                        Class<?> resultType) {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = resultSet.apply(i++, attribute.javaType());
            map.put(attribute.fieldName(), value);
        }
        try {
            return ProjectionUtil.getRecordResult(resultType, map);
        } catch (ReflectiveOperationException e) {
            throw new BeanReflectiveException(e);
        }
    }

    @NotNull
    public static <R> R getRecordResult(Class<?> resultType, Map<String, Object> map)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        int i;
        RecordComponent[] components = resultType.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] parameterTypes = new Class[components.length];
        for (i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            args[i] = map.get(component.getName());
            parameterTypes[i] = component.getType();
        }
        Constructor<?> constructor = resultType.getDeclaredConstructor(parameterTypes);
        Object row = constructor.newInstance(args);
        // noinspection unchecked
        return (R) row;
    }

    public static <R> R getInterfaceResult(@NotNull BiFunction<Integer, Class<?>, ?> resultSet,
                                           List<? extends FieldMapping> fields,
                                           Class<?> resultType) {
        Map<Method, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = resultSet.apply(i++, attribute.javaType());
            map.put(attribute.getter(), value);
        }

        Object result = ProjectionUtil.newProxyInstance(fields, resultType, map);
        //noinspection unchecked
        return (R) (result);
    }

    @NotNull
    public static Object newProxyInstance(List<? extends FieldMapping> fields, @NotNull Class<?> resultType, Map<Method, Object> map) {
        ClassLoader classLoader = resultType.getClassLoader();
        Class<?>[] interfaces = {resultType};
        return Proxy.newProxyInstance(classLoader, interfaces, new Handler(fields, resultType, map));
    }


    private record Handler(List<? extends FieldMapping> fields,
                           Class<?> resultType,
                           Map<Method, Object> data) implements InvocationHandler {
        private static final Method EQUALS = getEqualsMethod();

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
                return InvocationHandler.invokeDefault(proxy, method, args);
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
            for (FieldMapping attribute : fields) {
                stringMap.put(attribute.fieldName(), data.get(attribute.getter()));
            }
            return resultType.getSimpleName() + stringMap;
        }
    }

}
