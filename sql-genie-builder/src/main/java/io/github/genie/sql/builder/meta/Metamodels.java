package io.github.genie.sql.builder.meta;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Metamodels {

    public interface AbstractType extends Type {
        void setOwner(Type owner);
    }

    @Data
    public static class AttributeImpl implements Attribute, AbstractType {

        protected Class<?> javaType;
        protected Type owner;
        protected String fieldName;
        protected Method getter;
        protected Method setter;
        protected Field field;

        @Override
        public Type owner() {
            return owner;
        }

        @Override
        public Class<?> javaType() {
            return javaType;
        }

        @Override
        public String name() {
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
    public static class EntityTypeImpl implements EntityType, AbstractType {

        private Class<?> javaType;
        private Type owner;
        private Attribute id;
        private Attribute version;
        private String tableName;
        private Map<String, Attribute> fields;

        @Override
        public Attribute id() {
            return id;
        }

        @Override
        public Type owner() {
            return owner;
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
        public Attribute getAttribute(String fieldName) {
            return fields.get(fieldName);
        }

        @Override
        public Collection<? extends Attribute> fields() {
            return fields.values();
        }

        @Override
        public Attribute version() {
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
    public static class BasicAttributeImpl implements BasicAttribute, AbstractType {
        @Delegate
        private Attribute attribute;
        private String columnName;
        private boolean versionColumn;

        public BasicAttributeImpl(Attribute attribute, String columnName, boolean versionColumn) {
            this.attribute = attribute;
            this.columnName = columnName;
            this.versionColumn = versionColumn;
        }

        @Override
        public String columnName() {
            return columnName;
        }

        @Override
        public boolean isVersion() {
            return versionColumn;
        }

        @Override
        public void setOwner(Type owner) {
            ((AttributeImpl) attribute).setOwner(owner);
        }
    }

    @Data
    public static class AnyToOneAttributeImpl implements AnyToOneAttribute, AbstractType {
        @Delegate
        private Attribute attribute;
        private String joinColumnName;
        private String referencedColumnName;
        private Supplier<EntityType> referenced;

        public AnyToOneAttributeImpl(Attribute attribute) {
            this.attribute = attribute;
        }

        public AnyToOneAttributeImpl() {
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
        public EntityType referenced() {
            return referenced.get();
        }

        @Override
        public void setOwner(Type owner) {
            ((AttributeImpl) attribute).setOwner(owner);
        }
    }


    @Data
    @Accessors(fluent = true)
    static class ProjectionAttributeImpl implements ProjectionAttribute {
        private final Attribute field;
        private final Attribute baseField;

        public ProjectionAttributeImpl(Attribute baseField, Attribute field) {
            this.baseField = baseField;
            this.field = field;
        }
    }

    @Data
    @Accessors(fluent = true)
    static class ProjectionImpl implements Projection {
        private final List<ProjectionAttribute> attributes;

        public ProjectionImpl(List<ProjectionAttribute> attributes) {
            this.attributes = attributes;
        }
    }
}
