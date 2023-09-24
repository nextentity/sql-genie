package io.github.genie.sql.core;


import java.util.List;

public interface QueryMetadata {

    SelectClause selectClause();

    Class<?> fromClause();

    Expression.Meta whereClause();

    List<? extends Expression.Meta> groupByClause();

    List<? extends Ordering<?>> orderByClause();

    Expression.Meta havingClause();

    Integer offset();

    Integer limit();

    LockModeType lockModeType();

    List<? extends Expression.Paths> fetchPaths();
}
