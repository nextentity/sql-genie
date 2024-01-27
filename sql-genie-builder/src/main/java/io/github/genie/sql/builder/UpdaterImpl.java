package io.github.genie.sql.builder;

import io.github.genie.sql.api.Update;
import io.github.genie.sql.api.Updater;

import java.util.List;

public class UpdaterImpl<T> implements Updater<T> {
    private final Update update;
    private final Class<T> entityType;

    public UpdaterImpl(Update update, Class<T> entityType) {
        this.entityType = entityType;
        this.update = update;
    }

    @Override
    public List<T> insert(List<T> entities) {
        return update.insert(entities, entityType);
    }

    @Override
    public List<T> update(List<T> entities) {
        return update.update(entities, entityType);
    }

    @Override
    public void delete(Iterable<T> entities) {
        update.delete(entities, entityType);
    }

    @Override
    public T updateNonNullColumn(T entity) {
        return update.updateNonNullColumn(entity, entityType);
    }
}
