package io.github.genie.sql.core.mapping;

import lombok.SneakyThrows;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {
    static Field getDeclaredField(Class<?> clazz, String name) {
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

}
