package io.github.genie.sql.core.executor;

import lombok.Lombok;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class JdbcUtil {

    private static final Map<Class<?>, Object> SINGLE_ENUM_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResultSetGetter<?>> GETTER_MAPS = new HashMap<>();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = getPrimitiveMap();

    @NotNull
    private static Map<Class<?>, Class<?>> getPrimitiveMap() {
        HashMap<Class<?>, Class<?>> map = new HashMap<>();
        Class<?>[]
                types = {Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE,
                Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE},
                types2 = {Boolean.class, Character.class, Byte.class, Short.class, Integer.class,
                        Long.class, Float.class, Double.class, Void.class};

        for (int i = 0; i < types.length; i++) {
            map.put(types[i], types2[i]);
        }
        return map;
    }

    static {

        put(Byte.class, ResultSet::getByte);
        put(byte.class, ResultSet::getByte);
        put(Short.class, ResultSet::getShort);
        put(short.class, ResultSet::getShort);
        put(Integer.class, ResultSet::getInt);
        put(int.class, ResultSet::getInt);
        put(Float.class, ResultSet::getFloat);
        put(float.class, ResultSet::getFloat);
        put(Long.class, ResultSet::getLong);
        put(long.class, ResultSet::getLong);
        put(Double.class, ResultSet::getDouble);
        put(double.class, ResultSet::getDouble);
        put(Boolean.class, ResultSet::getBoolean);
        put(boolean.class, ResultSet::getBoolean);
        put(BigDecimal.class, ResultSet::getBigDecimal);
        put(Date.class, ResultSet::getTimestamp);
        put(String.class, ResultSet::getString);
        put(Time.class, ResultSet::getTime);

        put(java.sql.Date.class, ResultSet::getDate);
        put(Blob.class, ResultSet::getBlob);
        put(Clob.class, ResultSet::getClob);
        put(java.sql.Array.class, ResultSet::getArray);
        put(java.io.InputStream.class, ResultSet::getBinaryStream);
        put(byte[].class, ResultSet::getBytes);
        put(Timestamp.class, ResultSet::getTimestamp);

    }

    public static Class<?> getWrapedClass(Class<?> javaType) {
        return PRIMITIVE_MAP.getOrDefault(javaType, javaType);
    }


    public static <X> X getValue(ResultSet resultSet, int column, Class<X> targetType) throws SQLException {
        Object result;
        if (resultSet.getObject(column) == null) {
            return null;
        }
        ResultSetGetter<?> getter = GETTER_MAPS.get(targetType);
        if (getter == null) {
            if (Enum.class.isAssignableFrom(targetType)) {
                result = getEnum(targetType, resultSet.getInt(column));
            } else {
                result = resultSet.getObject(column);
            }
        } else {
            result = getter.getValue(resultSet, column);
        }
        // noinspection unchecked
        return (X) result;
    }


    public static void setParam(PreparedStatement pst, List<Object> args) throws SQLException {
        int i = 0;
        for (Object arg : args) {
            if (arg instanceof Enum) {
                arg = ((Enum<?>) arg).ordinal();
            }
            pst.setObject(++i, arg);
        }
    }

    public static void setParam(PreparedStatement pst, Object[] args) throws SQLException {
        int i = 0;
        for (Object arg : args) {
            if (arg instanceof Enum) {
                arg = ((Enum<?>) arg).ordinal();
            }
            pst.setObject(++i, arg);
        }
    }

    public static void setParamBatch(PreparedStatement pst, List<Object[]> argsList) throws SQLException {
        for (Object[] args : argsList) {
            setParam(pst, args);
            pst.addBatch();
        }
    }

    private static Object getEnum(Class<?> cls, int index) {
        Object array = SINGLE_ENUM_MAP.computeIfAbsent(cls, k -> {
            try {
                Method method = cls.getMethod("values");
                return method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw Lombok.sneakyThrow(e);
            }
        });
        return Array.get(array, index);
    }

    private static <T> void put(Class<T> type, ResultSetGetter<T> getter) {
        GETTER_MAPS.put(type, getter);
    }

    @FunctionalInterface
    interface ResultSetGetter<T> {
        T getValue(ResultSet resultSet, int index) throws SQLException;
    }
}


