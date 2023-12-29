package io.github.genie.sql.core.mapping;


import io.github.genie.sql.core.Util;
import io.github.genie.sql.core.mapping.Mappings.*;
import jakarta.persistence.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class JpaTableMappingFactory implements MappingFactory {

    public static final String FIX = "`";

    public JpaTableMappingFactory() {
    }

    @Override
    public TableMapping getMapping(Class<?> type) {
        return createMapping(type);
    }

    @Override
    public Projection getProjection(Class<?> baseType, Class<?> projectionType) {
        ArrayList<ProjectionField> list;
        if (projectionType.isInterface()) {
            list = getInterfaceProjectionFields(baseType, projectionType);
        } else if (projectionType.isRecord()) {
            list = getRecordProjectionFields(baseType, projectionType);
        } else {
            list = getBeanProjectionFields(baseType, projectionType);
        }
        list.trimToSize();
        return new ProjectionImpl(list);
    }

    private ArrayList<ProjectionField> getBeanProjectionFields(Class<?> baseType, Class<?> projectionType) {
        TableMapping mapping = getMapping(baseType);
        ArrayList<ProjectionField> list = new ArrayList<>();
        List<FieldMappingImpl> fields = getColumnMappings(projectionType);
        for (FieldMappingImpl field : fields) {
            FieldMapping baseField = mapping.getFieldMapping(field.getFieldName());
            if (field.javaType() == baseField.javaType()) {
                list.add(new ProjectionFieldImpl(baseField, field));
            }
        }
        return list;
    }

    private ArrayList<ProjectionField> getRecordProjectionFields(Class<?> baseType, Class<?> projectionType) {
        TableMapping mapping = getMapping(baseType);
        ArrayList<ProjectionField> list = new ArrayList<>();
        RecordComponent[] components = projectionType.getRecordComponents();
        for (RecordComponent method : components) {
            String name = method.getName();
            FieldMapping baseField = mapping.getFieldMapping(name);
            if (baseField != null && baseField.getter().getReturnType() == method.getType()) {
                FieldMappingImpl fieldMapping = new FieldMappingImpl();
                fieldMapping.setFieldName(name);
                fieldMapping.setJavaType(method.getType());
                list.add(new ProjectionFieldImpl(baseField, fieldMapping));
            }
        }
        return list;
    }

    private ArrayList<ProjectionField> getInterfaceProjectionFields(Class<?> baseType, Class<?> projectionType) {
        TableMapping mapping = getMapping(baseType);
        ArrayList<ProjectionField> list = new ArrayList<>();
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
        return list;
    }


    @NotNull
    private TableMappingImpl createMapping(Class<?> type) {
        TableMappingImpl tableMapping = new TableMappingImpl();
        tableMapping.setJavaType(type);
        List<FieldMappingImpl> fields = getColumnMappings(type);
        Map<String, FieldMappingImpl> map = new HashMap<>();
        tableMapping.setFields(map);
        for (FieldMappingImpl field : fields) {
            if (map.put(field.fieldName(), field) != null) {
                throw new IllegalStateException("Duplicate key");
            }

            Id id = getAnnotation(field.field(), field.getter(), Id.class);
            if (id != null || tableMapping.getId() == null && "id".equals(field.fieldName())) {
                tableMapping.setId(field);
            }

        }
        for (FieldMapping field : tableMapping.fields()) {
            if (field instanceof AssociationMappingImpl associationMapping) {
                String columnName = associationMapping.joinColumnName();
                FieldMapping mapping = tableMapping.getFieldMapping(columnName);
                if (mapping instanceof ColumnMapping columnMapping) {
                    associationMapping.setJoinColumnName(columnMapping.columnName());
                }
            }
        }
        tableMapping.setTableName(getTableName(type));
        for (FieldMappingImpl value : tableMapping.getFields().values()) {
            if (value.parent() == null) {
                value.setParent(tableMapping);
            }
        }
        return tableMapping;
    }

    private String getTableName(Class<?> javaType) {
        Table table = javaType.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        Entity entity = javaType.getAnnotation(Entity.class);
        if (entity != null && !entity.name().isEmpty()) {
            return entity.name();
        }
        String tableName = javaType.getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        if (tableName.startsWith(FIX) && tableName.endsWith(FIX)) {
            tableName = tableName.substring(1, tableName.length() - 1);
        }
        return tableName;
    }

    private List<FieldMappingImpl> getColumnMappings(Class<?> javaType) {
        List<FieldMappingImpl> mappings = new ArrayList<>();
        Field[] fields = javaType.getDeclaredFields();
        Map<Field, Method> readerMap = new HashMap<>();
        Map<Field, Method> writeMap = new HashMap<>();
        boolean hasVersion = false;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(javaType);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String propertyName = descriptor.getName();
                Field field = ReflectUtil.getDeclaredField(javaType, propertyName);
                if (field == null) {
                    continue;
                }
                readerMap.put(field, descriptor.getReadMethod());
                writeMap.put(field, descriptor.getWriteMethod());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Field field : fields) {
            if (!readerMap.containsKey(field)) {
                readerMap.put(field, null);
            }
            if (!writeMap.containsKey(field)) {
                writeMap.put(field, null);
            }
        }

        for (Field field : writeMap.keySet()) {

            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }

            if (field.getAnnotation(Transient.class) != null) {
                continue;
            }

            Method getter = readerMap.get(field);
            if (getter == null) {
                continue;
            }
            if (getter.getAnnotation(Transient.class) != null) {
                continue;
            }

            FieldMappingImpl fieldMapping;

            if (isBasicField(field, getter)) {
                Column column = getAnnotation(field, getter, Column.class);
                Version version = getAnnotation(field, getter, Version.class);
                boolean versionColumn = false;
                if (version != null) {
                    Class<?> type = field.getType();
                    if (type == long.class || type == Long.class || type == Integer.class || type == int.class) {
                        if (hasVersion) {
                            log.warn("duplicate attributes: " + field.getName() + ", ignored");
                        } else {
                            versionColumn = hasVersion = true;
                        }
                    } else {
                        log.warn("not support version type: " + type);
                    }
                }
                fieldMapping = new ColumnMappingImpl(getColumnName(field, column), versionColumn);
            } else {
                AssociationMappingImpl m = new AssociationMappingImpl();
                JoinColumn joinColumn = getAnnotation(field, getter, JoinColumn.class);
                if (joinColumn != null) {
                    m.setJoinColumnName(joinColumn.name());
                    m.setReferencedColumnName(joinColumn.referencedColumnName());
                    m.setReferenced(new LazyTableMapping(() -> {
                        TableMappingImpl mapping = createMapping(field.getType());
                        // mapping.setParent(m.parent());
                        for (Map.Entry<String, FieldMappingImpl> e : mapping.getFields().entrySet()) {
                            e.getValue().setParent(m);
                        }
                        return mapping;
                    }));
                }
                fieldMapping = m;
            }
            fieldMapping.setFieldName(field.getName());
            fieldMapping.setGetter(getter);
            fieldMapping.setSetter(writeMap.get(field));
            fieldMapping.setJavaType(field.getType());
            fieldMapping.setField(field);
            mappings.add(fieldMapping);
        }
        return mappings;
    }

    private static final List<Class<? extends Annotation>> JOIN_ANNOTATIONS =
            List.of(ManyToOne.class, OneToMany.class, ManyToMany.class, OneToOne.class);

    private static class LazyTableMapping implements TableMapping {

        volatile Object data;

        public LazyTableMapping(Supplier<TableMapping> data) {
            this.data = data;
        }

        @Delegate
        TableMapping tableMapping() {
            if (data instanceof Supplier<?>) {
                synchronized (this) {
                    if (data instanceof Supplier<?> supplier) {
                        data = supplier.get();
                    }
                }
            }
            return (TableMapping) data;
        }

        @Override
        public String toString() {
            if (data instanceof TableMappingImpl) {
                return data.toString();
            } else {
                return "Not loaded";
            }
        }
    }

    private boolean isBasicField(Field field, Method getter) {
        for (Class<? extends Annotation> annotationClass : JOIN_ANNOTATIONS) {
            if (getAnnotation(field, getter, annotationClass) != null) {
                return false;
            }
        }
        return true;
    }


    private static String getColumnName(Field field, Column column) {
        String columnName;
        if (column != null && !column.name().isEmpty()) {
            columnName = column.name();
        } else {
            columnName = field.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }
        if (columnName.startsWith(FIX) && columnName.endsWith(FIX)) {
            columnName = columnName.substring(1, columnName.length() - 1);
        }
        return columnName;
    }

    private <T extends Annotation> T getAnnotation(Field field, Method getter, Class<T> annotationClass) {
        T column = field.getAnnotation(annotationClass);
        if (column == null) {
            column = getter.getAnnotation(annotationClass);
        }
        return column;
    }

}
