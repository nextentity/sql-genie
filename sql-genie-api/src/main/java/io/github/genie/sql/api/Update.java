package io.github.genie.sql.api;

import java.util.List;

public interface Update {

    default <T> T insert(T entity, Class<T> entityType) {
        return insert(Lists.of(entity), entityType).get(0);
    }

    <T> List<T> insert(List<T> entities, Class<T> entityType);

    <T> List<T> update(List<T> entities, Class<T> entityType);

    default <T> T update(T entity, Class<T> entityType) {
        return update(Lists.of(entity), entityType).get(0);
    }

    <T> void delete(List<T> entities, Class<T> entityType);

    default <T> void delete(T entity, Class<T> entityType) {
        delete(Lists.of(entity), entityType);
    }

    <T> T updateNonNullColumn(T entity, Class<T> entityType);

}
