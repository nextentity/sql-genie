package io.github.genie.sql.core.mapping;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Mappings {

    @Data
    public static class MappingImpl implements Mapping {

        private Mapping owner;
        private Class<?> javaType;

        @Override
        public Class<?> javaType() {
            return javaType;
        }

        @Override
        public Mapping owner() {
            return owner;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FieldMappingImpl extends MappingImpl implements FieldMapping {

        private String fieldName;
        private Method getter;
        private Method setter;
        private Field field;

        public FieldMappingImpl() {
        }

        public FieldMappingImpl(FieldMapping field) {
            this.fieldName = field.fieldName();
            this.getter = field.getter();
            this.setter = field.setter();
            this.field = field.field();
            setJavaType(field.javaType());
        }

        @Override
        public String fieldName() {
            return fieldName;
        }

        @Override
        public Method getter() {
            return getter;
        }

        @Override
        public Method setter() {
            return setter;
        }

        @Override
        public Field field() {
            return field;
        }

        @Override
        public String toString() {
            return getFieldName();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TableMappingImpl extends MappingImpl implements TableMapping {

        private FieldMapping id;
        private FieldMapping version;
        private String tableName;
        private Map<String, FieldMappingImpl> fields;

        @Override
        public FieldMapping id() {
            return id;
        }

        @Override
        public String tableName() {
            return tableName;
        }

        @Override
        public FieldMapping getFieldMapping(String fieldName) {
            return fields.get(fieldName);
        }

        @Override
        public Collection<? extends FieldMapping> fields() {
            return fields.values();
        }

        @Override
        public FieldMapping version() {
            return version;
        }

        @Override
        public String toString() {
            return "TableMapping{" +
                   ", tableName='" + tableName + '\'' +
                   ", javaType=" + getJavaType().getName() +
                   '}';
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ColumnMappingImpl extends FieldMappingImpl implements ColumnMapping {
        private String columnName;
        private boolean versionColumn;

        public ColumnMappingImpl(FieldMapping field, String columnName, boolean versionColumn) {
            super(field);
            this.columnName = columnName;
            this.versionColumn = versionColumn;
        }

        @Override
        public String columnName() {
            return columnName;
        }

        @Override
        public boolean versionColumn() {
            return versionColumn;
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class AssociationMappingImpl extends FieldMappingImpl implements AssociationMapping {

        private String joinColumnName;
        private String referencedColumnName;
        private Supplier<TableMapping> referenced;

        public AssociationMappingImpl(FieldMapping field) {
            super(field);
        }

        @Override
        public String joinColumnName() {
            return joinColumnName;
        }

        @Override
        public String referencedColumnName() {
            return referencedColumnName;
        }

        @Override
        public TableMapping referenced() {
            return referenced.get();
        }

    }

    record ProjectionFieldImpl(FieldMapping baseField, FieldMapping field) implements ProjectionField {

    }

    record ProjectionImpl(List<ProjectionField> fields) implements Projection {

    }
}
