package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.*;
import io.github.genie.sql.core.SelectClause.MultiColumn;
import io.github.genie.sql.core.SelectClause.SingleColumn;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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

        Meta having = Metas.TRUE;

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
            return having;
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
                   + (isEmpty(fetch) ? "" : " fetch " + toString(fetch))
                   + " from " + from.getName()
                   + (where == null || Metas.isTrue(where) ? "" : " where " + where)
                   + (isEmpty(groupBy) ? "" : " group by " + toString(groupBy))
                   + (having == null || Metas.isTrue(having) ? "" : " having " + having)
                   + (isEmpty(orderBy) ? "" : " orderBy " + toString(orderBy))
                   + (offset == null ? "" : " offset " + offset)
                   + (limit == null ? "" : " limit " + limit)
                   + (lockType == null || lockType == LockModeType.NONE ? "" : " lock(" + lockType + ")");
        }

        private boolean isEmpty(Collection<?> objects) {
            return objects == null || objects.isEmpty();
        }

        private String toString(List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    record OrderingImpl<T>(Meta meta, SortOrder order) implements Ordering<T> {
        public static <T> OrderingImpl<T> of(TypedExpression<T, ?> meta, SortOrder order) {
            return new OrderingImpl<>(meta.meta(), order);
        }

        @Override
        public String toString() {
            return meta + " " + order;
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
            return String.valueOf(columns);
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
                return Stream.concat(Stream.of(l), r.stream())
                        .map(this::toString)
                        .collect(Collectors.joining(" " + operator() + ' '));
            } else if (r.isEmpty()) {
                return operator().sign() + '(' + l + ')';
            } else if (r.size() == 1) {
                return toString(l) + ' ' + operator().sign() + ' ' + toString(r.get(0));
            } else {
                return toString(l) + " " + operator().sign() + toString(r);
            }
        }

        @NotNull
        private static String toString(List<?> list) {
            return '(' + list.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")) + ')';
        }

        private String toString(Meta subMeta) {
            if (subMeta instanceof Operation o) {
                if (o.operator().priority() > operator().priority()) {
                    return "(" + subMeta + ')';
                }
            }
            return subMeta.toString();
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
