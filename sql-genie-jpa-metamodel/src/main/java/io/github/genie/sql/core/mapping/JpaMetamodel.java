package io.github.genie.sql.core.mapping;

import io.github.genie.sql.builder.meta.AbstractMetamodel;
import io.github.genie.sql.builder.meta.Attribute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JpaMetamodel extends AbstractMetamodel {
    private static final List<Class<? extends Annotation>> JOIN_ANNOTATIONS =
            Arrays.asList(ManyToOne.class, OneToMany.class, ManyToMany.class, OneToOne.class);

    @Override
    protected String getTableName(Class<?> javaType) {
        Table table = javaType.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        Entity entity = javaType.getAnnotation(Entity.class);
        if (entity != null && !entity.name().isEmpty()) {
            return entity.name();
        }
        String tableName = javaType.getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        if (tableName.startsWith("`") && tableName.endsWith("`")) {
            tableName = tableName.substring(1, tableName.length() - 1);
        }
        return tableName;
    }

    @Override
    protected boolean isMarkedId(Attribute field) {
        Id id = getAnnotation(field, Id.class);
        return id != null;
    }

    @Override
    protected String getReferencedColumnName(Attribute field) {
        JoinColumn annotation = getAnnotation(field, JoinColumn.class);
        String referencedColumnName = null;
        if (annotation != null) {
            referencedColumnName = annotation.referencedColumnName();
        }
        return referencedColumnName;
    }

    @Override
    protected String getJoinColumnName(Attribute field) {
        JoinColumn annotation = getAnnotation(field, JoinColumn.class);
        String joinColumnName = null;
        if (annotation != null) {
            joinColumnName = annotation.name();
        }
        return joinColumnName;
    }

    @Override
    protected boolean isVersionField(Attribute field) {
        Version version = getAnnotation(field, Version.class);

        if (version != null) {
            Class<?> type = field.javaType();
            if (type == long.class || type == Long.class || type == Integer.class || type == int.class) {
                return true;
            } else {
                throw new IllegalStateException("not support version type: " + type);
            }
        }
        return false;
    }

    @Override
    protected boolean isTransient(Attribute field) {
        if (field.field().getAnnotation(Transient.class) != null) {
            return true;
        }
        if (field.getter() == null) {
            return false;
        }
        return field.getter().getAnnotation(Transient.class) != null;
    }

    @Override
    protected boolean isBasicField(Attribute field) {
        for (Class<? extends Annotation> type : JOIN_ANNOTATIONS) {
            if (getAnnotation(field, type) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isAnyToOne(Attribute field) {
        return getAnnotation(field, ManyToOne.class) != null || getAnnotation(field, OneToOne.class) != null;
    }

    protected String getColumnName(Attribute field) {
        Column column = getAnnotation(field, Column.class);
        String columnName;
        if (column != null && !column.name().isEmpty()) {
            columnName = column.name();
        } else {
            columnName = field.name().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }
        if (columnName.startsWith("`") && columnName.endsWith("`")) {
            columnName = columnName.substring(1, columnName.length() - 1);
        }
        return columnName;
    }
}
