package io.github.genie.sql.core.mapping;

public interface MappingFactory {
    TableMapping getMapping(Class<?> type);

}
