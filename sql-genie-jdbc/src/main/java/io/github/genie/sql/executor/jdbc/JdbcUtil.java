package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.builder.TypeCastUtil;
import lombok.Lombok;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class JdbcUtil {

    private static final Map<Class<?>, Object> SINGLE_ENUM_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResultSetGetter<?>> GETTER_MAPS = new HashMap<>();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = getPrimitiveMap();

    @NotNull
    private static Map<Class<?>, Class<?>> getPrimitiveMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        Class<?>[]
                primitiveTypes = {Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE,
                Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE},
                wrapperTypes = {Boolean.class, Character.class, Byte.class, Short.class, Integer.class,
                        Long.class, Float.class, Double.class, Void.class};

        for (int i = 0; i < primitiveTypes.length; i++) {
            map.put(primitiveTypes[i], wrapperTypes[i]);
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
        ResultSetGetter<Character> getChar = (resultSet, index) -> {
            String string = resultSet.getString(index);
            if (string == null || string.length() != 1) {
                throw new IllegalStateException(string + " is not a character");
            }
            return string.charAt(0);
        };
        put(char.class, getChar);
        put(Character.class, getChar);
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
        put(Instant.class, (resultSet, columnIndex) -> resultSet.getTimestamp(columnIndex).toInstant());

    }

    public static Class<?> getWrapedClass(Class<?> javaType) {
        return PRIMITIVE_MAP.getOrDefault(javaType, javaType);
    }

    public static <X> X getValue(ResultSet resultSet, int column, Class<X> targetType) throws SQLException {
        Object result = resultSet.getObject(column);
        if (result == null) {
            return null;
        }
        if (!targetType.isInstance(result)) {
            ResultSetGetter<?> getter = GETTER_MAPS.get(targetType);
            if (getter == null) {
                if (Enum.class.isAssignableFrom(targetType)) {
                    result = getEnum(targetType, resultSet.getInt(column));
                }
            } else {
                result = getter.getValue(resultSet, column);
            }
        }
        return TypeCastUtil.unsafeCast(result);
    }

    public static void setParam(PreparedStatement pst, List<?> args) throws SQLException {
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

