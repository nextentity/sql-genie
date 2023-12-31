package io.github.genie.sql.builder.meta;

@SuppressWarnings("unused")
public interface Metamodel {
    EntityType getEntity(Class<?> type);

    Projection getProjection(Class<?> baseType, Class<?> projectionType);

}
