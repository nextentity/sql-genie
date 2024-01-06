package io.github.genie.sql.builder.meta;

import lombok.Data;
import lombok.Getter;
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
    @Accessors(fluent = true)
    public static class AttributeImpl implements Attribute, AbstractType {

        protected Class<?> javaType;
        protected Type owner;
        protected String name;
        protected Method getter;
        protected Method setter;
        protected Field field;

        @Override
        public void setOwner(Type owner) {
            this.owner = owner;
        }

        @Override
        public String toString() {
            return name();
        }

    }

    @Data
    @Accessors(fluent = true)
    public static class EntityTypeImpl implements EntityType, AbstractType {

        private Class<?> javaType;
        private Type owner;
        private Attribute id;
        private Attribute version;
        private String tableName;
        private Map<String, Attribute> attributeMap;

        public Collection<Attribute> attributes() {
            return attributeMap.values();
        }

        @Override
        public void setOwner(Type owner) {
            this.owner = owner;
        }

        @Override
        public Attribute getAttribute(String fieldName) {
            return attributeMap.get(fieldName);
        }

        @Override
        public String toString() {
            return "EntityType{" +
                    ", tableName='" + tableName + '\'' +
                    ", javaType=" + javaType().getName() +
                    '}';
        }
    }

    @Data
    @Accessors(fluent = true)
    public static class BasicAttributeImpl implements BasicAttribute, AbstractType {
        @Delegate
        private Attribute attribute;
        private String columnName;
        private boolean hasVersion;

        public BasicAttributeImpl(Attribute attribute, String columnName, boolean hasVersion) {
            this.attribute = attribute;
            this.columnName = columnName;
            this.hasVersion = hasVersion;
        }

        @Override
        public void setOwner(Type owner) {
            ((AttributeImpl) attribute).setOwner(owner);
        }
    }

    @Data
    @Accessors(fluent = true)
    public static class AnyToOneAttributeImpl implements AnyToOneAttribute, AbstractType {
        @Delegate
        private Attribute attribute;
        private String joinName;
        private String joinColumnName;
        private String referencedColumnName;
        private Supplier<EntityType> referencedSupplier;
        @Getter(lazy = true)
        private final EntityType referenced = referencedSupplier.get();

        public AnyToOneAttributeImpl(Attribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public void setOwner(Type owner) {
            ((AttributeImpl) attribute).setOwner(owner);
        }
    }

    record ProjectionAttributeImpl(Attribute baseField, Attribute field) implements ProjectionAttribute {
    }

    record ProjectionImpl(List<ProjectionAttribute> attributes) implements Projection {
    }
}
