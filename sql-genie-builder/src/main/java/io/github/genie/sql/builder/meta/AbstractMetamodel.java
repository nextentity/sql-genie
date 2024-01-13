package io.github.genie.sql.builder.meta;

import io.github.genie.sql.builder.Util;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import io.github.genie.sql.builder.meta.Metamodels.AnyToOneAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.AttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.BasicAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.EntityTypeImpl;
import io.github.genie.sql.builder.meta.Metamodels.ProjectionAttributeImpl;
import io.github.genie.sql.builder.meta.Metamodels.ProjectionImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("PatternVariableCanBeUsed")
@Slf4j
public abstract class AbstractMetamodel implements Metamodel {

    private final Map<Class<?>, EntityType> entityTypes = new ConcurrentHashMap<>();
    private final Map<List<Class<?>>, Projection> projections = new ConcurrentHashMap<>();

    @Override
    public EntityType getEntity(Class<?> entityType) {
        return entityTypes.computeIfAbsent(entityType, type -> createEntityType(type, null));
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
        ProjectionImpl result = new ProjectionImpl(projectionType, list, entity);
        if (projectionType.isInterface()) {
            Method[] methods = projectionType.getMethods();
            for (Method method : methods) {
                if (method.getParameterCount() == 0) {
                    String name = Util.getPropertyName(method.getName());
                    Attribute baseField = entity.getAttribute(name);
                    if (baseField != null && baseField.getter().getReturnType() == method.getReturnType()) {
                        Attribute attribute = newAttribute(null, method, null, null);
                        list.add(new ProjectionAttributeImpl(baseField, attribute));
                    }
                }
            }
        } else {
            List<Attribute> fields = getAllFields(projectionType, result);
            for (Attribute field : fields) {
                Attribute baseField = entity.getAttribute(field.name());
                if (field.javaType() == baseField.javaType()) {
                    list.add(new ProjectionAttributeImpl(baseField, field));
                }
            }
        }
        list.trimToSize();
        return result;
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

    protected EntityTypeImpl createEntityType(Class<?> entityType, Type owner) {
        EntityTypeImpl result = new EntityTypeImpl();
        result.javaType(entityType);
        Map<String, Attribute> map = new HashMap<>();
        result.attributeMap(map);
        result.tableName(getTableName(entityType));
        result.owner(owner);
        List<Attribute> allFields = getAllFields(entityType, result);
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
                ato.referencedSupplier(() -> createEntityType(field.javaType(), ato));
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

    protected List<Attribute> getAllFields(Class<?> type, Type owner) {
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
        return Arrays.stream(type.getDeclaredFields())
                .filter(this::isMappedField)
                .map(field -> newAttribute(owner, field, map.get(field.getName())))
                .filter(attribute -> !isTransient(attribute))
                .collect(Collectors.toList());
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

    protected boolean isMappedField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers());
    }

    protected Attribute newAttribute(Field field, Method getter, Method setter, Type owner) {
        Class<?> javaType = getter != null ? getter.getReturnType() : field.getType();
        String name = field != null ? field.getName() : Util.getPropertyName(getter.getName());
        return new AttributeImpl(javaType, owner, name, getter, setter, field);
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
