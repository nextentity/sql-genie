package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.*;
import io.github.genie.sql.core.SelectClause.MultiColumn;
import io.github.genie.sql.core.SelectClause.SingleColumn;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Models {

    static class QueryMetadataImpl implements QueryMetadata, Cloneable {

        SelectClause select;

        Class<?> from;

        Meta where = Metas.TRUE;

        List<? extends Meta> groupBy = List.of();

        List<? extends Ordering<?>> orderBy = List.of();

        Meta havingClause = Metas.TRUE;

        List<? extends Paths> fetch = List.of();

        Integer offset;

        Integer limit;

        LockModeType lockType = LockModeType.NONE;


        public QueryMetadataImpl(Class<?> from) {
            this.from = from;
            this.select = new SelectClauseImpl(from);
        }


        protected QueryMetadataImpl copy() {
            try {
                return (QueryMetadataImpl) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SelectClause select() {
            return select;
        }

        @Override
        public Class<?> from() {
            return from;
        }

        @Override
        public Meta where() {
            return where;
        }

        @Override
        public List<? extends Meta> groupBy() {
            return groupBy;
        }

        @Override
        public List<? extends Ordering<?>> orderBy() {
            return orderBy;
        }

        @Override
        public Meta having() {
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
        public LockModeType lockType() {
            return lockType;
        }

        @Override
        public List<? extends Paths> fetch() {
            return fetch;
        }

        @Override
        public String toString() {

            return "select " + select
                   + " from " + from.getName()
                   + " where " + where
                   + " group by " + groupBy.stream()
                           .map(String::valueOf)
                           .collect(Collectors.joining(","))
                   + " having " + havingClause
                   + "offset " + offset
                   + " limit " + limit
                   + " lock(" + lockType + ")";

        }
    }

    record OrderingImpl<T>(Meta meta, SortOrder order) implements Ordering<T> {
        public static <T> OrderingImpl<T> of(TypedExpression<T, ?> meta, SortOrder order) {
            return new OrderingImpl<>(meta.meta(), order);
        }

    }

    record SelectClauseImpl(Class<?> resultType) implements SelectClause {
        @Override
        public String toString() {
            return resultType.getName();
        }

    }

    record MultiColumnSelect(List<? extends Meta> columns) implements MultiColumn {
        @Override
        public String toString() {
            return columns.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

    }

    record SingleColumnSelect(Class<?> resultType, Meta column) implements SingleColumn {
        @Override
        public String toString() {
            return String.valueOf(column);
        }
    }

    record SliceImpl<T>(List<T> data, long total, Sliceable sliceable) implements Slice<T> {
    }

    record SliceableImpl(int offset, int size) implements Slice.Sliceable {
    }

    record ConstantMeta(Object value) implements Constant {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record OperationMeta(Meta operand,
                         Operator operator,
                         List<? extends Meta> args)
            implements Operation {

        @Override
        public String toString() {
            Meta l = operand();
            List<? extends Meta> r;
            if (args() != null) {
                r = args();
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

    record PathsMeta(List<String> paths) implements Paths {
        @Override
        public String toString() {
            return String.join(".", paths);
        }
    }

    private Models() {
    }
}
