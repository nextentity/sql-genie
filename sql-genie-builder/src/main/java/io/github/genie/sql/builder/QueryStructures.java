package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Constant;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.From;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.api.Selection.SingleColumn;
import io.github.genie.sql.api.Slice;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class QueryStructures {

    static class QueryStructureImpl implements QueryStructure, Cloneable {

        Selection select;

        From from;

        Expression where = ExpressionBuilders.TRUE;

        List<? extends Expression> groupBy = List.of();

        List<? extends Order<?>> orderBy = List.of();

        Expression having = ExpressionBuilders.TRUE;

        List<? extends Column> fetch = List.of();

        Integer offset;

        Integer limit;

        LockModeType lockType = LockModeType.NONE;

        public QueryStructureImpl(Selection select, From from) {
            this.select = select;
            this.from = from;
        }

        public QueryStructureImpl(Class<?> from) {
            this.from = new FromEntity(from);
            this.select = new SelectClauseImpl(from);
        }


        protected QueryStructureImpl copy() {
            try {
                return (QueryStructureImpl) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Selection select() {
            return select;
        }

        @Override
        public From from() {
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
        public List<? extends Order<?>> orderBy() {
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
        public List<? extends Column> fetch() {
            return fetch;
        }

        @Override
        public String toString() {

            return "select " + select
                   + (isEmpty(fetch) ? "" : " fetch " + QueryStructures.toString(fetch))
                   + " from " + from.type().getName()
                   + (where == null || ExpressionBuilders.isTrue(where) ? "" : " where " + where)
                   + (isEmpty(groupBy) ? "" : " group by " + QueryStructures.toString(groupBy))
                   + (having == null || ExpressionBuilders.isTrue(having) ? "" : " having " + having)
                   + (isEmpty(orderBy) ? "" : " orderBy " + QueryStructures.toString(orderBy))
                   + (offset == null ? "" : " offset " + offset)
                   + (limit == null ? "" : " limit " + limit)
                   + (lockType == null || lockType == LockModeType.NONE ? "" : " lock(" + lockType + ")");
        }

        private static boolean isEmpty(Collection<?> objects) {
            return objects == null || objects.isEmpty();
        }

    }

    record FromEntity(Class<?> type) implements From.Entity {
    }

    record FromSubQuery(QueryStructure queryStructure) implements From.SubQuery {
    }

    record OrderImpl<T>(Expression expression, SortOrder order) implements Order<T> {
        public static <T> OrderImpl<T> of(ExpressionHolder<T, ?> holder, SortOrder order) {
            return new OrderImpl<>(holder.expression(), order);
        }

        @Override
        public String toString() {
            return expression + " " + order;
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

    record SliceImpl<T>(List<T> data, long total, int offset, int limit) implements Slice<T> {
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
            return QueryStructures.toString(this);
        }

    }

    record ColumnMeta(List<String> paths) implements Column {
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

    private QueryStructures() {
    }
}
