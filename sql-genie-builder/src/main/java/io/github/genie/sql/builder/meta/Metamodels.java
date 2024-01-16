package io.github.genie.sql.builder.meta;

import io.github.genie.sql.api.Column;
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

    @Data
    @Accessors(fluent = true)
    public static class AttributeImpl implements Attribute {

        private Class<?> javaType;
        private Type owner;
        private String name;
        private Method getter;
        private Method setter;
        private Field field;
        @Getter(lazy = true)
        private final List<? extends Attribute> referencedAttributes = Attribute.super.referencedAttributes();
        @Getter(lazy = true)
        private final Column column = Attribute.super.column();

        public AttributeImpl(Class<?> javaType, Type owner, String name, Method getter, Method setter, Field field) {
            this.javaType = javaType;
            this.owner = owner;
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.field = field;
        }

        @Override
        public String toString() {
            return name();
        }


    }

    @Data
    @Accessors(fluent = true)
    public static class EntityTypeImpl implements EntityType {

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
    public static class BasicAttributeImpl implements BasicAttribute {
        @Delegate
        private final Attribute attribute;
        private final String columnName;
        private final boolean hasVersion;
    }

    @Data
    @Accessors(fluent = true)
    public static class AnyToOneAttributeImpl implements AnyToOneAttribute {
        @Delegate
        private Attribute attribute;
        private String joinName;
        private String joinColumnName;
        private String referencedColumnName;
        private Supplier<EntityType> referencedSupplier;
        @Delegate
        @Getter(lazy = true)
        private final EntityType referenced = referencedSupplier.get();

        public AnyToOneAttributeImpl(Attribute attribute) {
            this.attribute = attribute;
        }
    }

    @Data
    @Accessors(fluent = true)
    static final class ProjectionAttributeImpl implements ProjectionAttribute {
        @Delegate
        private final Attribute attribute;
        private final Attribute entityAttribute;
    }

    @Data
    @Accessors(fluent = true)
    static final class ProjectionImpl implements Projection {
        private final Class<?> javaType;
        private final List<ProjectionAttribute> attributes;
        private final EntityType entityType;
        private final Type owner;

        @Override
        public Type owner() {
            return owner;
        }

    }
}
