package io.github.genie.sql.meta;

import io.github.genie.sql.builder.meta.AbstractMetamodel;
import io.github.genie.sql.builder.meta.Attribute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JpaMetamodel extends AbstractMetamodel {
    private static final List<Class<? extends Annotation>> JOIN_ANNOTATIONS =
            Arrays.asList(ManyToOne.class, OneToMany.class, ManyToMany.class, OneToOne.class);

    @Override
    protected String getTableName(Class<?> javaType) {
        String tableName = getTableNameByAnnotation(javaType);
        return tableName != null ? tableName : getTableNameByClassName(javaType);
    }

    protected String getTableNameByClassName(Class<?> javaType) {
        String tableName;
        tableName = camelbackToUnderline(javaType.getSimpleName());
        tableName = unwrapSymbol(tableName);
        return tableName;
    }

    protected String camelbackToUnderline(String simpleName) {
        return simpleName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    protected String getTableNameByAnnotation(Class<?> javaType) {
        Table table = javaType.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        Entity entity = javaType.getAnnotation(Entity.class);
        if (entity != null && !entity.name().isEmpty()) {
            return entity.name();
        }
        return null;
    }

    @Override
    protected boolean isMarkedId(Attribute attribute) {
        Id id = getAnnotation(attribute, Id.class);
        return id != null;
    }

    @Override
    protected String getReferencedColumnName(Attribute attribute) {
        JoinColumn annotation = getAnnotation(attribute, JoinColumn.class);
        String referencedColumnName = null;
        if (annotation != null) {
            referencedColumnName = annotation.referencedColumnName();
        }
        return referencedColumnName;
    }

    @Override
    protected String getJoinColumnName(Attribute attribute) {
        JoinColumn annotation = getAnnotation(attribute, JoinColumn.class);
        String joinColumnName = null;
        if (annotation != null) {
            joinColumnName = annotation.name();
        }
        return joinColumnName;
    }

    @Override
    protected boolean isVersionField(Attribute attribute) {
        Version version = getAnnotation(attribute, Version.class);
        if (version != null) {
            Class<?> type = attribute.javaType();
            if (isSupportVersion(type)) {
                return true;
            } else {
                throw new IllegalStateException("not support version type: " + type);
            }
        }
        return false;
    }

    protected boolean isSupportVersion(Class<?> type) {
        return type == long.class || type == Long.class || type == Integer.class || type == int.class;
    }

    @Override
    protected boolean isTransient(Attribute attribute) {
        return attribute == null || getAnnotation(attribute, Transient.class) != null;
    }

    @Override
    protected boolean isBasicField(Attribute attribute) {
        for (Class<? extends Annotation> type : JOIN_ANNOTATIONS) {
            if (getAnnotation(attribute, type) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isAnyToOne(Attribute attribute) {
        return getAnnotation(attribute, ManyToOne.class) != null || getAnnotation(attribute, OneToOne.class) != null;
    }

    protected String getColumnName(Attribute attribute) {
        String columnName = getColumnNameByAnnotation(attribute);
        if (columnName == null) {
            columnName = camelbackToUnderline(attribute.name());
        }
        return unwrapSymbol(columnName);
    }

    @Override
    protected Field[] getSuperClassField(Class<?> baseClass, Class<?> superClass) {
        MappedSuperclass mappedSuperclass = superClass.getAnnotation(MappedSuperclass.class);
        if (mappedSuperclass != null) {
            return superClass.getDeclaredFields();
        } else {
            return new Field[0];
        }
    }

    protected String unwrapSymbol(String symbol) {
        while (symbol.startsWith("`") && symbol.endsWith("`")) {
            symbol = symbol.substring(1, symbol.length() - 1);
        }
        return symbol;
    }

    protected String getColumnNameByAnnotation(Attribute attribute) {
        Column column = getAnnotation(attribute, Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        }
        return null;
    }
}
