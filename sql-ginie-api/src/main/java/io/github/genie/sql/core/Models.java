package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionBuilder.Constant;
import io.github.genie.sql.core.ExpressionBuilder.Expression;
import io.github.genie.sql.core.ExpressionBuilder.Operation;
import io.github.genie.sql.core.ExpressionBuilder.Paths;
import io.github.genie.sql.core.Selection.MultiColumn;
import io.github.genie.sql.core.Selection.SingleColumn;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Models {

    static class QueryMetadataImpl implements QueryStructure, Cloneable {

        Selection select;

        Class<?> from;

        Expression where = Metas.TRUE;

        List<? extends Expression> groupBy = List.of();

        List<? extends Ordering<?>> orderBy = List.of();

        Expression having = Metas.TRUE;

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
        public Selection select() {
            return select;
        }

        @Override
        public Class<?> from() {
            return from;
        }

        @Override
        public Expression where() {
            return where;
        }

        @Override
        public List<? extends Expression> groupBy() {
            return groupBy;
        }

        @Override
        public List<? extends Ordering<?>> orderBy() {
            return orderBy;
        }

        @Override
        public Expression having() {
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

    record OrderingImpl<T>(Expression meta, SortOrder order) implements Ordering<T> {
        public static <T> OrderingImpl<T> of(ExpressionBuilder<T, ?> meta, SortOrder order) {
            return new OrderingImpl<>(meta.build(), order);
        }

        @Override
        public String toString() {
            return meta + " " + order;
        }
    }

    record SelectClauseImpl(Class<?> resultType) implements Selection {
        @Override
        public String toString() {
            return resultType.getName();
        }

    }

    record MultiColumnSelect(List<? extends Expression> columns) implements MultiColumn {
        @Override
        public String toString() {
            return String.valueOf(columns);
        }

    }

    record SingleColumnSelect(Class<?> resultType, Expression column) implements SingleColumn {
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

    record OperationMeta(Expression operand,
                         Operator operator,
                         List<? extends Expression> args)
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
        Expression l = o.operand();
        List<? extends Expression> r;
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

    private static String toString(Operation parent, Expression subMeta) {
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
