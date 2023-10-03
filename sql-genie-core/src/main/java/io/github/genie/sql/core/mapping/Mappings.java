package io.github.genie.sql.core.mapping;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Mappings {

    @Data
    public static class MappingImpl implements Mapping {

        private Mapping parent;
        private Class<?> javaType;

        @Override
        public Class<?> javaType() {
            return javaType;
        }

        @Override
        public Mapping parent() {
            return parent;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FieldMappingImpl extends MappingImpl implements FieldMapping {

        private String fieldName;
        private Method getter;
        private Method setter;
        private Field field;

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
        public String toString() {
            return "TableMapping{" +
                   ", tableName='" + tableName + '\'' +
                   ", javaType=" + getJavaType().getName() +
                   '}';
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ColumnMappingImpl extends FieldMappingImpl implements ColumnMapping {
        private final String columnName;

        @Override
        public String columnName() {
            return columnName;
        }

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AssociationMappingImpl extends FieldMappingImpl implements AssociationMapping {

        private String joinColumnName;
        private String referencedColumnName;
        private TableMapping referenced;

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
            return referenced;
        }

    }

    record ProjectionFieldImpl(FieldMapping baseField, FieldMapping field) implements ProjectionField {

    }

    record ProjectionImpl(List<ProjectionField> fields) implements Projection {

    }
}
