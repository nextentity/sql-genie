package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.Constant;
import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Operation;
import io.github.genie.sql.core.Expression.Paths;
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
                   + (isEmpty(fetch) ? "" : " fetch " + Models.toString(fetch))
                   + " from " + from.getName()
                   + (where == null || Metas.isTrue(where) ? "" : " where " + where)
                   + (isEmpty(groupBy) ? "" : " group by " + Models.toString(groupBy))
                   + (having == null || Metas.isTrue(having) ? "" : " having " + having)
                   + (isEmpty(orderBy) ? "" : " orderBy " + Models.toString(orderBy))
                   + (offset == null ? "" : " offset " + offset)
                   + (limit == null ? "" : " limit " + limit)
                   + (lockType == null || lockType == LockModeType.NONE ? "" : " lock(" + lockType + ")");
        }

        private static boolean isEmpty(Collection<?> objects) {
            return objects == null || objects.isEmpty();
        }

    }

    record OrderingImpl<T>(Meta meta, SortOrder order) implements Ordering<T> {
        public static <T> OrderingImpl<T> of(Expression<T, ?> meta, SortOrder order) {
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
        @Override
        public int offset() {
            return sliceable.offset();
        }

        @Override
        public int limit() {
            return sliceable.limit();
        }
    }

    record SliceableImpl(int offset, int limit) implements Sliceable {
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
            return Models.toString(this);
        }

    }

    record PathsMeta(List<String> paths) implements Paths {
        @Override
        public String toString() {
            return String.join(".", paths);
        }
    }

    public static String toString(Operation o) {
        Meta l = o.operand();
        List<? extends Meta> r;
        if (o.args() != null) {
            r = o.args();
        } else {
            r = List.of();
        }
        if (o.operator().isMultivalued()) {
            return Stream.concat(Stream.of(l), r.stream())
                    .map(it -> toString(o, it))
                    .collect(Collectors.joining(" " + o.operator() + ' '));
        } else if (r.isEmpty()) {
            return o.operator().sign() + '(' + l + ')';
        } else if (r.size() == 1) {
            return toString(o, l) + ' ' + o.operator().sign() + ' ' + toString(o, r.get(0));
        } else {
            return toString(o, l) + " " + o.operator().sign() + '(' + toString(r) + ')';
        }
    }

    @NotNull
    private static String toString(List<?> list) {
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    private static String toString(Operation parent, Meta subMeta) {
        if (subMeta instanceof Operation o) {
            if (o.operator().priority() > parent.operator().priority()) {
                return "(" + subMeta + ')';
            }
        }
        return subMeta.toString();
    }

    private Models() {
    }
}
