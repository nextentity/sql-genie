package io.github.genie.sql.core.mapping;


import io.github.genie.sql.core.Util;
import io.github.genie.sql.core.exception.BeanReflectiveException;
import io.github.genie.sql.core.mapping.Mappings.AssociationMappingImpl;
import io.github.genie.sql.core.mapping.Mappings.ColumnMappingImpl;
import io.github.genie.sql.core.mapping.Mappings.FieldMappingImpl;
import io.github.genie.sql.core.mapping.Mappings.ProjectionFieldImpl;
import io.github.genie.sql.core.mapping.Mappings.ProjectionImpl;
import io.github.genie.sql.core.mapping.Mappings.TableMappingImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractTableMappingFactory implements MappingFactory {

    private final Map<Class<?>, TableMapping> tableMappings = new ConcurrentHashMap<>();
    private final Map<List<Class<?>>, Projection> projections = new ConcurrentHashMap<>();

    @Override
    public TableMapping getMapping(Class<?> entityType) {
        return tableMappings.computeIfAbsent(entityType, this::createMapping);
    }

    @Override
    public Projection getProjection(Class<?> baseType, Class<?> projectionType) {
        List<Class<?>> key = new ArrayList<>(2);
        key.add(baseType);
        key.add(projectionType);
        return projections.computeIfAbsent(key, k -> createProjection(baseType, projectionType));
    }

    @NotNull
    protected Projection createProjection(Class<?> baseType, Class<?> projectionType) {
        TableMapping mapping = getMapping(baseType);
        ArrayList<ProjectionField> list = new ArrayList<>();
        if (projectionType.isInterface()) {
            Method[] methods = projectionType.getMethods();
            for (Method method : methods) {
                if (method.getParameterCount() == 0) {
                    String name = Util.getPropertyName(method.getName());
                    FieldMapping baseField = mapping.getFieldMapping(name);
                    if (baseField != null && baseField.getter().getReturnType() == method.getReturnType()) {
                        FieldMappingImpl fieldMapping = new FieldMappingImpl();
                        fieldMapping.setGetter(method);
                        fieldMapping.setFieldName(name);
                        fieldMapping.setJavaType(method.getReturnType());
                        list.add(new ProjectionFieldImpl(baseField, fieldMapping));
                    }
                }
            }
        } else {
            List<FieldMapping> fields = getAllFields(projectionType);
            for (FieldMapping field : fields) {
                FieldMapping baseField = mapping.getFieldMapping(field.fieldName());
                if (field.javaType() == baseField.javaType()) {
                    list.add(new ProjectionFieldImpl(baseField, field));
                }
            }
        }
        list.trimToSize();
        return new ProjectionImpl(list);
    }

    protected abstract String getTableName(Class<?> javaType);

    protected abstract boolean isMarkedId(FieldMapping field);

    protected abstract String getReferencedColumnName(FieldMapping field);

    protected abstract String getJoinColumnName(FieldMapping field);

    protected abstract boolean isVersionField(FieldMapping field);

    protected abstract boolean isTransient(FieldMapping field);

    protected abstract boolean isBasicField(FieldMapping field);

    protected abstract String getColumnName(FieldMapping field);

    protected TableMappingImpl createMapping(Class<?> entityType) {
        TableMappingImpl tableMapping = new TableMappingImpl();
        tableMapping.setJavaType(entityType);
        Map<String, FieldMappingImpl> map = new HashMap<>();
        tableMapping.setFields(map);
        tableMapping.setTableName(getTableName(entityType));
        List<FieldMapping> allFields = getAllFields(entityType);
        boolean hasVersion = false;
        for (FieldMapping field : allFields) {
            if (map.containsKey(field.fieldName())) {
                throw new IllegalStateException("Duplicate key");
            }

            FieldMappingImpl mapping;
            if (isBasicField(field)) {
                boolean versionColumn = false;
                if (isVersionField(field)) {
                    if (hasVersion) {
                        log.warn("duplicate attributes: " + field.fieldName() + ", ignored");
                    } else {
                        versionColumn = hasVersion = true;
                    }
                }
                mapping = new ColumnMappingImpl(field, getColumnName(field), versionColumn);
                if (versionColumn) {
                    tableMapping.setVersion(mapping);
                }

            } else {
                AssociationMappingImpl m = new AssociationMappingImpl(field);
                m.setJoinColumnName(getJoinColumnName(field));
                m.setReferencedColumnName(getReferencedColumnName(field));
                m.setReferenced(() -> {
                    TableMappingImpl res = createMapping(field.javaType());
                    for (Map.Entry<String, FieldMappingImpl> e : res.getFields().entrySet()) {
                        e.getValue().setOwner(m);
                    }
                    return res;
                });
                mapping = m;
            }

            boolean isMarkedId = isMarkedId(mapping);
            if (isMarkedId || tableMapping.getId() == null && "id".equals(field.fieldName())) {
                tableMapping.setId(mapping);
            }

            map.put(mapping.fieldName(), mapping);
            mapping.setOwner(tableMapping);
        }
        return tableMapping;
    }

    protected List<FieldMapping> getAllFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        Map<Field, FieldMapping> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String propertyName = descriptor.getName();
                Field field = ReflectUtil.getDeclaredField(type, propertyName);
                if (field == null) {
                    continue;
                }
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                FieldMapping value = newFieldMapping(field, getter, setter);
                if (isTransient(value)) {
                    continue;
                }
                map.put(field, value);
            }
        } catch (Exception e) {
            throw new BeanReflectiveException(e);
        }
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            map.computeIfAbsent(field, k -> newFieldMapping(field, null, null));
        }
        return new ArrayList<>(map.values());
    }

    protected FieldMapping newFieldMapping(Field field, Method getter, Method setter) {
        FieldMappingImpl value = new FieldMappingImpl();
        String name = field != null ? field.getName() : Util.getPropertyName(getter.getName());
        value.setFieldName(name);
        value.setGetter(getter);
        value.setSetter(setter);
        value.setField(field);
        value.setJavaType(getter != null ? getter.getReturnType() : field.getType());
        return value;
    }

    protected <T extends Annotation> T getAnnotation(FieldMapping field, Class<T> annotationClass) {
        T column = field.field().getAnnotation(annotationClass);
        if (column == null) {
            Method getter = field.getter();
            if (getter != null) {
                column = getter.getAnnotation(annotationClass);
            }
        }
        return column;
    }

}
