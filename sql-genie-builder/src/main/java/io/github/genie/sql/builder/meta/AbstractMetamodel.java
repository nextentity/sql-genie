package io.github.genie.sql.builder.meta;

import io.github.genie.sql.builder.PathReference;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.meta.Metamodels.AnyToOneAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.AnyToOneProjectionAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.AttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.BasicAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.ProjectionAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.RootEntity;
import io.github.genie.sql.builder.meta.Metamodels.RootProjection;
import io.github.genie.sql.builder.reflect.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractMetamodel implements Metamodel {

    private final Map<Class<?>, EntityType> entityTypes = new ConcurrentHashMap<>();
    private final Map<List<Class<?>>, Projection> projections = new ConcurrentHashMap<>();

    @Override
    public EntityType getEntity(Class<?> entityType) {
        return entityTypes.computeIfAbsent(entityType, this::createEntityType);
    }

    @Override
    public Projection getProjection(Class<?> entityType, Class<?> projectionType) {
        List<Class<?>> key = Arrays.asList(entityType, projectionType);
        return projections.computeIfAbsent(key, k -> createProjection(entityType, projectionType));
    }

    @NotNull
    protected Projection createProjection(Class<?> baseType, Class<?> projectionType) {
        EntityType entity = getEntity(baseType);
        ArrayList<ProjectionAttribute> list = new ArrayList<>();
        List<ProjectionAttribute> immutable = Collections.unmodifiableList(list);
        RootProjection result = new RootProjection(projectionType, immutable, entity);
        getProjectionAttributes(projectionType, result, entity, list, 0, 2);
        list.trimToSize();
        return result;
    }

    private void getProjectionAttributes(Class<?> projectionType,
                                         Type owner,
                                         EntityType entity,
                                         ArrayList<ProjectionAttribute> list,
                                         int deep,
                                         int maxDeep) {
        if (deep == maxDeep) {
            return;
        }
        List<Attribute> attributes = getProjectionAttributes(projectionType, owner);
        for (Attribute attribute : attributes) {
            Attribute entityAttribute = getEntityAttribute(attribute, entity);
            if (entityAttribute == null) {
                continue;
            }
            if (entityAttribute instanceof EntityType) {
                AnyToOneProjectionAttributeImpl o = new AnyToOneProjectionAttributeImpl(attribute, entityAttribute);
                getProjectionAttributes(attribute.javaType(), o,
                        (EntityType) entityAttribute, list, deep + 1, maxDeep);
            } else if (attribute.javaType() == entityAttribute.javaType()) {
                list.add(new ProjectionAttributeImpl(attribute, entityAttribute));
            }
        }
    }

    private List<Attribute> getProjectionAttributes(Class<?> projectionType, Type owner) {
        if (projectionType.isInterface()) {
            return getInterfaceAttributes(projectionType, owner);
        }
        return getBeanAttributes(projectionType, owner);
    }

    protected Attribute getEntityAttribute(Attribute attribute, EntityType entity) {
        Attribute entityAttribute = getEntityAttributeByAnnotation(attribute, entity);
        return entityAttribute == null
                ? entity.getAttribute(attribute.name())
                : entityAttribute;
    }

    private Attribute getEntityAttributeByAnnotation(Attribute attribute, EntityType entity) {
        EntityAttribute entityAttribute = getAnnotation(attribute, EntityAttribute.class);
        if (entityAttribute == null || entityAttribute.value().isEmpty()) {
            return null;
        }
        String value = entityAttribute.value();
        String[] split = value.split("\\.");
        Type cur = entity;
        for (String s : split) {
            if (cur instanceof EntityType) {
                cur = ((EntityType) cur).getAttribute(s);
            } else {
                throw new IllegalStateException("entity attribute " + value + " not exist");
            }
        }
        if (cur instanceof BasicAttribute) {
            if (attribute.javaType() != cur.javaType()) {
                throw new IllegalStateException("entity attribute " + value + " type mismatch");
            }
            return (Attribute) cur;
        } else {
            throw new IllegalStateException("entity attribute " + value + " not exist");
        }
    }

    @NotNull
    private List<Attribute> getInterfaceAttributes(Class<?> clazz, Type owner) {
        return Arrays.stream(clazz.getMethods())
                .map(it -> newAttribute(null, it, null, owner))
                .collect(Collectors.toList());
    }

    protected abstract String getTableName(Class<?> javaType);

    protected abstract boolean isMarkedId(Attribute attribute);

    protected abstract String getReferencedColumnName(Attribute attribute);

    protected abstract String getJoinColumnName(Attribute attribute);

    protected abstract boolean isVersionField(Attribute attribute);

    protected abstract boolean isTransient(Attribute attribute);

    protected abstract boolean isBasicField(Attribute attribute);

    protected abstract boolean isAnyToOne(Attribute attribute);

    protected abstract String getColumnName(Attribute attribute);

    protected abstract Field[] getSuperClassField(Class<?> baseClass, Class<?> superClass);

    protected RootEntity createEntityType(Class<?> entityType) {
        RootEntity result = new RootEntity();
        return createEntityType(entityType, result, result);
    }

    protected RootEntity createEntityType(Class<?> entityType, RootEntity result, Type owner) {
        result.javaType(entityType);
        Map<String, Attribute> map = new HashMap<>();
        result.attributes(Collections.unmodifiableMap(map));
        result.tableName(getTableName(entityType));
        List<Attribute> attributes = getBeanAttributes(entityType, owner);
        boolean hasVersion = false;
        for (Attribute attr : attributes) {
            if (map.containsKey(attr.name())) {
                throw new IllegalStateException("Duplicate key");
            }
            if (isTransient(attr)) {
                continue;
            }

            Attribute attribute;
            if (isBasicField(attr)) {
                boolean versionColumn = false;
                if (isVersionField(attr)) {
                    if (hasVersion) {
                        log.warn("duplicate attributes: " + attr.name() + ", ignored");
                    } else {
                        versionColumn = hasVersion = true;
                    }
                }
                attribute = new BasicAttributeImpl(attr, getColumnName(attr), versionColumn);
                if (versionColumn) {
                    result.version(attribute);
                }

            } else if (isAnyToOne(attr)) {
                AnyToOneAttributeImpl ato = new AnyToOneAttributeImpl(attr);
                ato.joinName(getJoinColumnName(attr));
                ato.referencedColumnName(getReferencedColumnName(attr));
                ato.referencedSupplier(() -> createEntityType(attr.javaType(), new RootEntity(), ato));
                attribute = ato;
            } else {
                log.warn("ignored attribute " + attr.field());
                continue;
            }

            boolean isMarkedId = isMarkedId(attribute);
            if (isMarkedId || result.id() == null && "id".equals(attr.name())) {
                result.id(attribute);
            }
            map.put(attribute.name(), attribute);
        }
        setAnyToOneAttributeColumnName(map);
        return result;
    }

    protected void setAnyToOneAttributeColumnName(Map<String, Attribute> map) {
        for (Entry<String, Attribute> entry : map.entrySet()) {
            Attribute value = entry.getValue();
            if (value instanceof AnyToOneAttributeImpl) {
                AnyToOneAttributeImpl attr = (AnyToOneAttributeImpl) value;
                String joinColumnName = getJoinColumnName(map, attr);
                attr.joinColumnName(joinColumnName);
            }
        }
    }

    protected String getJoinColumnName(Map<String, Attribute> map, AnyToOneAttributeImpl attr) {
        String joinName = attr.joinName();
        Attribute join = map.get(joinName);
        return join instanceof BasicAttribute
                ? ((BasicAttribute) join).columnName()
                : joinName;
    }

    protected List<Attribute> getBeanAttributes(Class<?> type, Type owner) {
        Map<String, PropertyDescriptor> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                Field field = ReflectUtil.getDeclaredField(type, descriptor.getName());
                if (field != null) {
                    map.put(field.getName(), descriptor);
                }
            }
        } catch (IntrospectionException e) {
            throw new BeanReflectiveException(e);
        }
        List<Attribute> attributes = getDeclaredFields(type).stream()
                .map(field -> newAttribute(owner, field, map.remove(field.getName())))
                .collect(Collectors.toList());
        map.values().stream()
                .map(descriptor -> newAttribute(owner, null, descriptor))
                .forEach(attributes::add);
        return attributes;
    }

    protected Collection<Field> getDeclaredFields(Class<?> clazz) {
        Map<String, Field> map = new LinkedHashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        putFieldsIfAbsent(map, fields);
        getSuperClassDeclaredFields(clazz, clazz.getSuperclass(), map);
        return map.values();
    }

    protected void putFieldsIfAbsent(Map<String, Field> map, Field[] fields) {
        for (Field field : fields) {
            if (filterDeclaredField(field)) {
                map.putIfAbsent(field.getName(), field);
            }
        }
    }

    protected void getSuperClassDeclaredFields(Class<?> baseClass, Class<?> clazz, Map<String, Field> map) {
        if (clazz == null) {
            return;
        }
        Field[] superClassField = getSuperClassField(baseClass, clazz);
        if (superClassField != null) {
            putFieldsIfAbsent(map, superClassField);
        }
        Class<?> superclass = clazz.getSuperclass();
        getSuperClassDeclaredFields(baseClass, superclass, map);
    }

    private Attribute newAttribute(Type owner, Field field, PropertyDescriptor descriptor) {
        Method getter, setter;
        if (descriptor != null) {
            getter = descriptor.getReadMethod();
            setter = descriptor.getWriteMethod();
        } else {
            getter = setter = null;
        }
        return newAttribute(field, getter, setter, owner);
    }

    protected boolean filterDeclaredField(@NotNull Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers);
    }

    protected Attribute newAttribute(Field field, Method getter, Method setter, Type owner) {
        Class<?> javaType = getter != null ? getter.getReturnType() : field.getType();
        String name = field != null ? field.getName() : PathReference.getPropertyName(getter.getName());
        return new AttributeImpl(javaType, owner, name, getter, setter, field);
    }

    protected <T extends Annotation> T getAnnotation(Attribute attribute, Class<T> annotationClass) {
        T column = null;
        if (attribute.field() != null) {
            column = attribute.field().getAnnotation(annotationClass);
        }
        if (column == null) {
            Method getter = attribute.getter();
            if (getter != null) {
                column = getter.getAnnotation(annotationClass);
            }
        }
        return column;
    }

}
