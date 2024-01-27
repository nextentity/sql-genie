package io.github.genie.sql.api;

import java.util.List;

public interface Updater<T> {

    default T insert(T entity) {
        return insert(Lists.of(entity)).get(0);
    }

    List<T> insert(List<T> entities);

    List<T> update(List<T> entities);

    default T update(T entity) {
        return update(Lists.of(entity)).get(0);
    }

    void delete(Iterable<T> entities);

    default void delete(T entity) {
        delete(Lists.of(entity));
    }

    T updateNonNullColumn(T entity);

}
