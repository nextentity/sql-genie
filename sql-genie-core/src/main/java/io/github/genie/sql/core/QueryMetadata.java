package io.github.genie.sql.core;


import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Paths;

import java.util.List;

public interface QueryMetadata {

    SelectClause selectClause();

    Class<?> fromClause();

    Meta whereClause();

    List<? extends Meta> groupByClause();

    List<? extends Ordering<?>> orderByClause();

    Meta havingClause();

    Integer offset();

    Integer limit();

    LockModeType lockModeType();

    List<? extends Paths> fetchPaths();
}
