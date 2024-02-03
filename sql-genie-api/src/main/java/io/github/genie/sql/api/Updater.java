package io.github.genie.sql.api;

import java.util.List;

public interface Updater<T> {

    T insert(T entity);

    List<T> insert(List<T> entities);

    List<T> update(List<T> entities);

    T update(T entity);

    void delete(Iterable<T> entities);

    void delete(T entity);

    T updateNonNullColumn(T entity);

}
