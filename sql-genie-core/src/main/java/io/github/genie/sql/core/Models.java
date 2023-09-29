package io.github.genie.sql.core;

import io.github.genie.sql.core.SelectClause.MultiColumn;
import io.github.genie.sql.core.SelectClause.SingleColumn;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Models {

    static class QueryMetadataImpl implements QueryMetadata, Cloneable {

        SelectClause selectClause;

        Class<?> fromClause;

        Expression.Meta whereClause = Metas.TRUE;

        List<? extends Expression.Meta> groupByClause = List.of();

        List<? extends Ordering<?>> orderByClause = List.of();

        Expression.Meta havingClause = Metas.TRUE;

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

    record OrderingImpl<T>(Expression.Meta meta, SortOrder order) implements Ordering<T> {
        public static <T> OrderingImpl<T> of(Expression.TypedExpression<T, ?> meta, SortOrder order) {
            return new OrderingImpl<>(meta.meta(), order);
        }

    }

    record SelectClauseImpl(Class<?> resultType) implements SelectClause {
        @Override
        public String toString() {
            return resultType.getName();
        }

    }

    record MultiColumnSelect(List<? extends Expression.Meta> columns) implements MultiColumn {
        @Override
        public String toString() {
            return columns.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

    }

    record SingleColumnSelect(Class<?> resultType, Expression.Meta column) implements SingleColumn {
        @Override
        public String toString() {
            return String.valueOf(column);
        }
    }

    record SliceImpl<T>(List<T> data, long total, Sliceable sliceable) implements Slice<T> {
    }

    record SliceableImpl(int offset, int size) implements Slice.Sliceable {
    }

    record ConstantMeta(Object value) implements Expression.Constant {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record OperationMeta(Expression.Meta leftOperand,
                         Operator operator,
                         List<? extends Expression.Meta> rightOperand)
            implements Expression.Operation {

        @Override
        public String toString() {
            Expression.Meta l = leftOperand();
            List<? extends Expression.Meta> r;
            if (rightOperand() != null) {
                r = rightOperand();
            } else {
                r = List.of();
            }
            if (operator().isMultivalued()) {
                return '(' + Stream.concat(Stream.of(l), r.stream())
                        .map(String::valueOf)
                        .collect(Collectors.joining(" " + operator() + ' '))
                       + ')';
            } else if (r.isEmpty()) {
                return "(" + l + ' ' + operator().sign() + ')';
            } else if (r.size() == 1) {
                return "(" + l + ' ' + operator().sign() + ' ' + r.get(0) + ")";
            } else {
                return "(" + l + ' ' + operator().sign() + ' ' + r + ")";
            }
        }

    }

    record PathsMeta(List<String> paths) implements Expression.Paths {
        @Override
        public String toString() {
            return String.join(".", paths);
        }
    }

    private Models() {
    }
}
