package io.github.genie.sql.core.mapping;

@SuppressWarnings("unused")
public interface MappingFactory {
    TableMapping getMapping(Class<?> type);

    Projection getProjection(Class<?> baseType, Class<?> projectionType);

}
