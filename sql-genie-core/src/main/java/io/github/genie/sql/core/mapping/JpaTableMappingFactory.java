package io.github.genie.sql.core.mapping;


import io.github.genie.sql.core.mapping.Mappings.AssociationMappingImpl;
import io.github.genie.sql.core.mapping.Mappings.ColumnMappingImpl;
import io.github.genie.sql.core.mapping.Mappings.FieldMappingImpl;
import io.github.genie.sql.core.mapping.Mappings.TableMappingImpl;
import jakarta.persistence.*;
import lombok.experimental.Delegate;
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
import java.util.function.Supplier;

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
        return null;
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
        List<FieldMappingImpl> ColumnMappings = new ArrayList<>();
        Field[] fields = javaType.getDeclaredFields();
        Map<Field, Method> readerMap = new HashMap<>();
        Map<Field, Method> writeMap = new HashMap<>();
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
                ColumnMappingImpl m = new ColumnMappingImpl();
                Column column = getAnnotation(field, getter, Column.class);
                m.setColumnName(getColumnName(field, column));
                fieldMapping = m;
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
            ColumnMappings.add(fieldMapping);
        }
        return ColumnMappings;
    }

    private static final List<Class<? extends Annotation>> JOIN_ANNOTATIONS = List.of(ManyToOne.class, OneToMany.class, ManyToMany.class, OneToOne.class);

    static class LazyTableMapping implements TableMapping {

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
