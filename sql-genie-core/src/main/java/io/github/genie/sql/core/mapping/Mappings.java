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
    public static class FieldMappingImpl implements FieldMapping {

        protected Class<?> javaType;
        protected Mapping parent;
        protected String fieldName;
        protected Method getter;
        protected Method setter;
        protected Field field;

        @Override
        public Mapping parent() {
            return parent;
        }

        @Override
        public Class<?> javaType() {
            return javaType;
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
    public static class TableMappingImpl implements TableMapping {

        private Class<?> javaType;
        private Mapping parent;
        private FieldMapping id;
        private String tableName;
        private Map<String, FieldMappingImpl> fields;

        @Override
        public FieldMapping id() {
            return id;
        }

        @Override
        public Mapping parent() {
            return parent;
        }

        @Override
        public Class<?> javaType() {
            return javaType;
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
