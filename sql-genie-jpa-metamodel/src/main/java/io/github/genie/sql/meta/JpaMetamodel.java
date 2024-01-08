package io.github.genie.sql.meta;

import io.github.genie.sql.builder.meta.AbstractMetamodel;
import io.github.genie.sql.builder.meta.Attribute;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.lang.annotation.Annotation;
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

    private static String camelbackToUnderline(String simpleName) {
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
            if (isSupportVersion(type)) {
                return true;
            } else {
                throw new IllegalStateException("not support version type: " + type);
            }
        }
        return false;
    }

    private static boolean isSupportVersion(Class<?> type) {
        return type == long.class || type == Long.class || type == Integer.class || type == int.class;
    }

    @Override
    protected boolean isTransient(Attribute field) {
        return getAnnotation(field, Transient.class) != null;
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
        String columnName = getColumnNameByAnnotation(field);
        if (columnName == null) {
            columnName = camelbackToUnderline(field.name());
        }
        return unwrapSymbol(columnName);
    }

    private static String unwrapSymbol(String symbol) {
        if (symbol.startsWith("`") && symbol.endsWith("`")) {
            symbol = symbol.substring(1, symbol.length() - 1);
        }
        return symbol;
    }

    private String getColumnNameByAnnotation(Attribute field) {
        Column column = getAnnotation(field, Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        }
        return null;
    }
}
