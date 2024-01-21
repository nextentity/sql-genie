package io.github.genie.sql.builder.meta;

import io.github.genie.sql.api.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Metamodels {

    @Getter
    @RequiredArgsConstructor
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
        @Getter(lazy = true)
        private final int layer = Attribute.super.layer();

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
            if (owner != null
                && !(owner instanceof RootEntity)
                && !(owner instanceof RootProjection)) {
                return owner + "." + name;
            }
            return name;
        }


    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    public static class RootEntity implements EntityType {

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
            return "Entity{" + javaType.getSimpleName() + "}";
        }

        @Override
        public String name() {
            return javaType.getSimpleName();
        }

        @Override
        public int layer() {
            return 0;
        }
    }

    @Getter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    public static class BasicAttributeImpl implements BasicAttribute {
        @Delegate
        private final Attribute attribute;
        private final String columnName;
        private final boolean hasVersion;

        @Override
        public String toString() {
            return attribute.toString();
        }
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
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

        @Override
        public String toString() {
            return attribute.toString();
        }
    }

    @Getter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    static final class ProjectionAttributeImpl implements ProjectionAttribute {
        @Delegate
        private final Attribute attribute;
        private final Attribute entityAttribute;

        @Override
        public String toString() {
            return attribute.toString();
        }
    }

    @Getter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    static final class AnyToOneProjectionAttributeImpl implements AnyToOneProjectionAttribute {
        @Delegate
        private final Attribute attribute;
        private final Attribute entityAttribute;

        @Override
        public String toString() {
            return attribute.toString();
        }

        @Override
        public Collection<? extends Attribute> attributes() {
            throw new UnsupportedOperationException();
        }
    }


    @Getter
    @AllArgsConstructor
    @Accessors(fluent = true)
    static final class RootProjection implements Projection {
        private final Class<?> javaType;
        private final List<ProjectionAttribute> attributes;
        private final EntityType entityType;
        private final Type owner;

        @Override
        public Type owner() {
            return owner;
        }

        @Override
        public String name() {
            return javaType.getSimpleName();
        }

        @Override
        public int layer() {
            return 0;
        }

        @Override
        public String toString() {
            return javaType.getSimpleName();
        }
    }
}
