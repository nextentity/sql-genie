package io.github.genie.sql.builder.meta;


import io.github.genie.sql.builder.Util;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.meta.Metamodels.AbstractType;
import io.github.genie.sql.builder.meta.Metamodels.AnyToOneAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.AttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.BasicAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.EntityTypeImpl;
import io.github.genie.sql.builder.meta.Metamodels.ProjectionAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.ProjectionImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractMetamodel implements Metamodel {

    private final Map<Class<?>, EntityType> entityTypes = new ConcurrentHashMap<>();
    private final Map<List<Class<?>>, Projection> projections = new ConcurrentHashMap<>();

    @Override
    public EntityType getEntity(Class<?> entityType) {
        return entityTypes.computeIfAbsent(entityType, this::createEntityType);
    }

    @Override
    public Projection getProjection(Class<?> baseType, Class<?> projectionType) {
        List<Class<?>> key = new ArrayList<>(2);
        key.add(baseType);
        key.add(projectionType);
        return projections.computeIfAbsent(key, k -> createProjection(baseType, projectionType));
    }

    @NotNull
    protected Projection createProjection(Class<?> baseType, Class<?> projectionType) {
        EntityType entity = getEntity(baseType);
        ArrayList<ProjectionAttribute> list = new ArrayList<>();
        if (projectionType.isInterface()) {
            Method[] methods = projectionType.getMethods();
            for (Method method : methods) {
                if (method.getParameterCount() == 0) {
                    String name = Util.getPropertyName(method.getName());
                    Attribute baseField = entity.getAttribute(name);
                    if (baseField != null && baseField.getter().getReturnType() == method.getReturnType()) {
                        AttributeImpl attribute = new AttributeImpl();
                        attribute.getter(method);
                        attribute.name(name);
                        attribute.javaType(method.getReturnType());
                        list.add(new ProjectionAttributeImpl(baseField, attribute));
                    }
                }
            }
        } else {
            List<Attribute> fields = getAllFields(projectionType);
            for (Attribute field : fields) {
                Attribute baseField = entity.getAttribute(field.name());
                if (field.javaType() == baseField.javaType()) {
                    list.add(new ProjectionAttributeImpl(baseField, field));
                }
            }
        }
        list.trimToSize();
        return new ProjectionImpl(list);
    }

    protected abstract String getTableName(Class<?> javaType);

    protected abstract boolean isMarkedId(Attribute field);

    protected abstract String getReferencedColumnName(Attribute field);

    protected abstract String getJoinColumnName(Attribute field);

    protected abstract boolean isVersionField(Attribute field);

    protected abstract boolean isTransient(Attribute field);

    protected abstract boolean isBasicField(Attribute field);

    protected abstract boolean isAnyToOne(Attribute field);

    protected abstract String getColumnName(Attribute field);

    protected EntityTypeImpl createEntityType(Class<?> entityType) {
        EntityTypeImpl result = new EntityTypeImpl();
        result.javaType(entityType);
        Map<String, Attribute> map = new HashMap<>();
        result.attributeMap(map);
        result.tableName(getTableName(entityType));
        List<Attribute> allFields = getAllFields(entityType);
        boolean hasVersion = false;
        for (Attribute field : allFields) {
            if (map.containsKey(field.name())) {
                throw new IllegalStateException("Duplicate key");
            }

            Attribute attribute;
            if (isBasicField(field)) {
                boolean versionColumn = false;
                if (isVersionField(field)) {
                    if (hasVersion) {
                        log.warn("duplicate attributes: " + field.name() + ", ignored");
                    } else {
                        versionColumn = hasVersion = true;
                    }
                }
                attribute = new BasicAttributeImpl(field, getColumnName(field), versionColumn);
                if (versionColumn) {
                    result.version(attribute);
                }

            } else if (isAnyToOne(field)) {
                AnyToOneAttributeImpl ato = new AnyToOneAttributeImpl(field);
                ato.joinName(getJoinColumnName(field));
                ato.referencedColumnName(getReferencedColumnName(field));
                ato.referencedSupplier(() -> {
                    EntityTypeImpl res = createEntityType(field.javaType());
                    for (Map.Entry<String, Attribute> e : res.attributeMap().entrySet()) {
                        setOwner(e.getValue(), ato);
                    }
                    return res;
                });
                attribute = ato;
            } else {
                log.warn("ignored attribute " + field.field());
                continue;
            }

            boolean isMarkedId = isMarkedId(attribute);
            if (isMarkedId || result.id() == null && "id".equals(field.name())) {
                result.id(attribute);
            }

            map.put(attribute.name(), attribute);
            setOwner(attribute, result);
        }
        setAnyToOneAttributeColumnName(map);
        return result;
    }

    protected void setAnyToOneAttributeColumnName(Map<String, Attribute> map) {
        for (Entry<String, Attribute> entry : map.entrySet()) {
            Attribute value = entry.getValue();
            if (value instanceof AnyToOneAttributeImpl attr) {
                String joinColumnName = getJoinColumnName(map, attr);
                attr.joinColumnName(joinColumnName);
            }
        }
    }

    protected String getJoinColumnName(Map<String, Attribute> map, AnyToOneAttributeImpl attr) {
        String joinName = attr.joinName();
        Attribute join = map.get(joinName);
        return join instanceof BasicAttribute basic
                ? basic.columnName()
                : joinName;
    }


    private void setOwner(Attribute attribute, Type owner) {
        if (attribute instanceof AbstractType) {
            ((AbstractType) attribute).setOwner(owner);
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected List<Attribute> getAllFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        Map<Field, Attribute> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String propertyName = descriptor.getName();
                Field field = ReflectUtil.getDeclaredField(type, propertyName);
                if (field == null) {
                    continue;
                }
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                Attribute value = newAttribute(field, getter, setter);
                if (isTransient(value)) {
                    continue;
                }
                map.put(field, value);
            }
        } catch (Exception e) {
            throw new BeanReflectiveException(e);
        }
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                continue;
            }
            map.computeIfAbsent(field, k -> newAttribute(field, null, null));
        }
        return new ArrayList<>(map.values());
    }

    protected Attribute newAttribute(Field field, Method getter, Method setter) {
        AttributeImpl value = new AttributeImpl();
        String name = field != null ? field.getName() : Util.getPropertyName(getter.getName());
        value.name(name);
        value.getter(getter);
        value.setter(setter);
        value.field(field);
        value.javaType(getter != null ? getter.getReturnType() : field.getType());
        return value;
    }

    protected <T extends Annotation> T getAnnotation(Attribute field, Class<T> annotationClass) {
        T column = field.field().getAnnotation(annotationClass);
        if (column == null) {
            Method getter = field.getter();
            if (getter != null) {
                column = getter.getAnnotation(annotationClass);
            }
        }
        return column;
    }

}
