package io.github.genie.sql.core;

import java.util.List;
import java.util.stream.Collectors;

class QueryMetadataImpl implements QueryMetadata, Cloneable {

    SelectClause selectClause;

    Class<?> fromClause;

    Expression.Meta whereClause = BasicExpressions.TRUE;

    List<? extends Expression.Meta> groupByClause = List.of();

    List<? extends Ordering<?>> orderByClause = List.of();

    Expression.Meta havingClause = BasicExpressions.TRUE;

    List<? extends Expression.Paths> fetchPaths = List.of();

    Integer offset;

    Integer limit;

    LockModeType lockModeType = LockModeType.NONE;


    public QueryMetadataImpl(Class<?> fromClause) {
        this.fromClause = fromClause;
        this.selectClause = new SelectClauseImpl(fromClause);
    }


    protected QueryMetadataImpl copy() {
        try {
            return (QueryMetadataImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SelectClause selectClause() {
        return selectClause;
    }

    @Override
    public Class<?> fromClause() {
        return fromClause;
    }

    @Override
    public Expression.Meta whereClause() {
        return whereClause;
    }

    @Override
    public List<? extends Expression.Meta> groupByClause() {
        return groupByClause;
    }

    @Override
    public List<? extends Ordering<?>> orderByClause() {
        return orderByClause;
    }

    @Override
    public Expression.Meta havingClause() {
        return havingClause;
    }

    @Override
    public Integer offset() {
        return offset;
    }

    @Override
    public Integer limit() {
        return limit;
    }

    @Override
    public LockModeType lockModeType() {
        return lockModeType;
    }

    @Override
    public List<? extends Expression.Paths> fetchPaths() {
        return fetchPaths;
    }

    @Override
    public String toString() {

        return "select " + selectClause
               + " from " + fromClause.getName()
               + " where " + whereClause
               + " group by " + groupByClause.stream()
                       .map(String::valueOf)
                       .collect(Collectors.joining(","))
               + " having " + havingClause
               + "offset " + offset
               + " limit " + limit
               + " lock(" + lockModeType + ")";

    }
}
