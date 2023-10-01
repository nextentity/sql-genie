package io.github.genie.sql.core.mapping;

import java.util.Collection;

public interface TableMapping extends Mapping {

    FieldMapping id();

    String tableName();

    Collection<? extends FieldMapping> fields();

    Class<?> javaType();

}
