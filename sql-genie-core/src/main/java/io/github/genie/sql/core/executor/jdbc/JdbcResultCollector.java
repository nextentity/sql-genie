package io.github.genie.sql.core.executor.jdbc;

import io.github.genie.sql.core.SelectClause;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.mapping.FieldMapping;
import io.github.genie.sql.core.mapping.Mapping;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static io.github.genie.sql.core.SelectClause.MultiColumn;
import static io.github.genie.sql.core.SelectClause.SingleColumn;
import static java.lang.invoke.MethodHandles.lookup;

public class JdbcResultCollector implements JdbcQueryExecutor.ResultCollector {

    @Override
    public <R> R collect(@NotNull ResultSet resultSet,
                         @NotNull SelectClause selectClause,
                         @NotNull Class<?> fromType,
                         @NotNull List<? extends FieldMapping> fields)
            throws SQLException {
        int columnsCount = resultSet.getMetaData().getColumnCount();
        int column = 0;
        if (selectClause instanceof MultiColumn multiColumn) {
            if (multiColumn.columns().size() != columnsCount) {
                throw new IllegalStateException();
            }
            Object[] row = new Object[columnsCount];
            while (column < columnsCount) {
                row[column++] = resultSet.getObject(column);
            }
            return cast(row);
        } else if (selectClause instanceof SingleColumn singleColumn) {
            if (1 != columnsCount) {
                throw new IllegalStateException();
            }
            Object r = JdbcUtil.getValue(resultSet, 1, singleColumn.resultType());
            return cast(r);
        } else {
            if (fields.size() != columnsCount) {
                throw new IllegalStateException();
            }
            try {
                Class<?> resultType = selectClause.resultType();
                if (resultType.isInterface()) {
                    return getInterfaceResult(resultSet, fields, resultType);
                } else if (resultType.isRecord()) {
                    return getRecordResult(resultSet, fields, resultType);
                } else {
                    return getBeanResult(resultSet, fields, resultType);
                }
            } catch (ReflectiveOperationException e) {
                throw new BeanReflectiveException(e);
            }
        }
    }

    @NotNull
    private <R> R getRecordResult(@NotNull ResultSet resultSet,
                                  @NotNull List<? extends FieldMapping> fields,
                                  Class<?> resultType)
            throws SQLException, ReflectiveOperationException {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = JdbcUtil.getValue(resultSet, ++i, attribute.javaType());
            map.put(attribute.fieldName(), value);
        }
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
        return cast(row);
    }

    private <R> R getInterfaceResult(ResultSet resultSet, List<? extends FieldMapping> fields, Class<?> resultType) throws SQLException {
        ClassLoader classLoader = resultType.getClassLoader();
        Class<?>[] interfaces = {resultType, ProjectionProxyInstance.class};
        Map<Method, Object> map = new HashMap<>();
        int i = 0;
        for (FieldMapping attribute : fields) {
            Object value = JdbcUtil.getValue(resultSet, ++i, attribute.javaType());
            map.put(attribute.getter(), value);
        }
        Object result = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            if (map.containsKey(method)) {
                return map.get(method);
            }
            if (ProjectionProxyInstance.TO_STRING_METHOD.equals(method)) {
                Map<String, Object> stringMap = new HashMap<>();
                for (FieldMapping attribute : fields) {
                    stringMap.put(attribute.fieldName(), map.get(attribute.getter()));
                }
                return resultType.getSimpleName() + stringMap;
            }

            if (ProjectionProxyInstance.MAP_METHOD.equals(method)) {
                return map;
            }

            if (ProjectionProxyInstance.CLASS_METHOD.equals(method)) {
                return resultType;
            }

            if (ProjectionProxyInstance.EQUALS_METHOD.equals(method)) {
                if (proxy == args[0]) {
                    return true;
                }
                if (args[0] instanceof ProjectionProxyInstance instance) {
                    if (instance.__projectionClassOfProjectionProxyInstance__() == resultType) {
                        return map.equals(instance.__dataMapOfProjectionProxyInstance__());
                    }
                }
                return false;
            }
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(map, args);
            }
            if (method.isDefault()) {
                return invokeDefaultMethod(proxy, method, args);
            }
            throw new AbstractMethodError(method.toString());
        });
        return cast(result);
    }

    @NotNull
    private <R> R getBeanResult(@NotNull ResultSet resultSet,
                                @NotNull List<? extends FieldMapping> fields,
                                Class<?> resultType)
            throws ReflectiveOperationException, SQLException {
        int column = 0;
        Object row = resultType.getConstructor().newInstance();
        for (FieldMapping projection : fields) {
            int deep = 0;
            Mapping cur = projection;
            while (cur != null) {
                deep++;
                cur = cur.parent();
            }
            FieldMapping[] mappings = new FieldMapping[deep - 1];
            cur = projection;
            for (int i = mappings.length - 1; i >= 0; i--) {
                mappings[i] = (FieldMapping) cur;
                cur = cur.parent();
            }
            Class<?> fieldType = projection.javaType();
            Object value = JdbcUtil.getValue(resultSet, ++column, fieldType);
            if (value == null && mappings.length > 1) {
                continue;
            }
            Object obj = row;
            for (int i = 0; i < mappings.length - 1; i++) {
                FieldMapping mapping = mappings[i];
                Object tmp = mapping.invokeGetter(obj);
                if (tmp == null) {
                    tmp = mapping.javaType().getConstructor().newInstance();
                    mapping.invokeSetter(obj, tmp);
                }
                obj = tmp;
            }
            projection.invokeSetter(obj, value);
        }
        return cast(row);
    }

    private <R> R cast(Object result) {
        // noinspection unchecked
        return (R) result;
    }

    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
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
            return lookup()
                    .findSpecial(
                            method.getDeclaringClass(),
                            method.getName(),
                            MethodType.methodType(method.getReturnType(), new Class[0]),
                            method.getDeclaringClass()
                    ).bindTo(proxy)
                    .invokeWithArguments(args);
        }
    }

    private interface ProjectionProxyInstance {

        Method TO_STRING_METHOD = getMethod(() -> Object.class.getMethod("toString"));
        Method EQUALS_METHOD = getMethod(() -> Object.class.getMethod("equals", Object.class));
        Method MAP_METHOD = getMethod(() ->
                ProjectionProxyInstance.class.getMethod("__dataMapOfProjectionProxyInstance__"));
        Method CLASS_METHOD = getMethod(() ->
                ProjectionProxyInstance.class.getMethod("__projectionClassOfProjectionProxyInstance__"));

        @SneakyThrows
        static Method getMethod(Callable<Method> method) {
            return method.call();
        }

        Map<Method, Object> __dataMapOfProjectionProxyInstance__();

        Class<?> __projectionClassOfProjectionProxyInstance__();

    }
}
