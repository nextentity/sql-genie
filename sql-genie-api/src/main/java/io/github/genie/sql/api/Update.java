package io.github.genie.sql.api;

import java.util.Arrays;
import java.util.List;

public interface Update {

    default <T> T insert(T entity, Class<T> entityType) {
        return insert(Arrays.asList(entity), entityType).get(0);
    }

    <T> List<T> insert(List<T> entities, Class<T> entityType);

    <T>  List<T> update(List<T> entities, Class<T> entityType);

    <T> T updateNonNullColumn(T entity, Class<T> entityType);

}
