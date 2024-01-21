package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Constant;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.From;
import io.github.genie.sql.api.From.Entity;
import io.github.genie.sql.api.From.SubQuery;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.api.Selection.SingleColumn;
import io.github.genie.sql.api.Slice;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"PatternVariableCanBeUsed", "ClassCanBeRecord"})
final class QueryStructures {

    static class QueryStructureImpl implements QueryStructure, Cloneable {

        Selection select;

        From from;

        Expression where = Expressions.TRUE;

        List<? extends Expression> groupBy = Lists.of();

        List<? extends Order<?>> orderBy = Lists.of();

        Expression having = Expressions.TRUE;

        List<? extends Column> fetch = Lists.of();

        Integer offset;

        Integer limit;

        LockModeType lockType = LockModeType.NONE;

        public QueryStructureImpl(Selection select, From from) {
            this.select = select;
            this.from = from;
        }

        public QueryStructureImpl(Class<?> from) {
            this.from = new FromEntity(from);
            this.select = new SelectClauseImpl(from, false);
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
                   + (where == null || Expressions.isTrue(where) ? "" : " where " + where)
                   + (isEmpty(groupBy) ? "" : " group by " + QueryStructures.toString(groupBy))
                   + (having == null || Expressions.isTrue(having) ? "" : " having " + having)
                   + (isEmpty(orderBy) ? "" : " orderBy " + QueryStructures.toString(orderBy))
                   + (offset == null ? "" : " offset " + offset)
                   + (limit == null ? "" : " limit " + limit)
                   + (lockType == null || lockType == LockModeType.NONE ? "" : " lock(" + lockType + ")");
        }

        private static boolean isEmpty(Collection<?> objects) {
            return objects == null || objects.isEmpty();
        }

    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class FromEntity implements Entity {
        private final Class<?> type;
    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class FromSubQuery implements SubQuery {
        private final QueryStructure queryStructure;
    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class OrderImpl<T> implements Order<T> {
        private final Expression expression;
        private final SortOrder order;

        @Override
        public String toString() {
            return expression + " " + order;
        }
    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class SelectClauseImpl implements Selection {
        private final Class<?> resultType;
        private final boolean distinct;

        @Override
        public String toString() {
            return resultType.getName();
        }

    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class MultiColumnSelect implements MultiColumn {
        private final List<? extends Expression> columns;
        private final boolean distinct;

        @Override
        public String toString() {
            return String.valueOf(columns);
        }

    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class SingleColumnSelect implements SingleColumn {
        private final Class<?> resultType;
        private final Expression column;
        private final boolean distinct;

        @Override
        public String toString() {
            return String.valueOf(column);
        }
    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class SliceImpl<T> implements Slice<T> {
        private final List<T> data;
        private final long total;
        private final int offset;
        private final int limit;
    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class ConstantMeta implements Constant {
        private final Object value;

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class OperationMeta implements Operation {
        private final Expression operand;
        private final Operator operator;
        private final List<? extends Expression> args;

        @Override
        public String toString() {
            return QueryStructures.toString(this);
        }

    }

    @lombok.Data
    @Accessors(fluent = true)
    static final class ColumnMeta implements Column {
        private final String[] paths;
        @Getter(lazy = true)
        private final String identity = String.join(".", paths);

        @Override
        public String toString() {
            return identity();
        }

        @Override
        public int size() {
            return paths.length;
        }

        @Override
        public String get(int i) {
            return paths[i];
        }

        @Override
        public Column get(String path) {
            String[] strings = new String[size() + 1];
            System.arraycopy(paths, 0, strings, 0, paths.length);
            strings[size()] = path;
            return new ColumnMeta(strings);
        }

        @Override
        public Column parent() {
            if (size() <= 1) {
                return null;
            }
            String[] strings = new String[size() - 1];
            System.arraycopy(paths, 0, strings, 0, strings.length);
            return new ColumnMeta(strings);
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return Arrays.stream(paths).iterator();
        }
    }

    public static String toString(Operation o) {
        Expression l = o.operand();
        List<? extends Expression> r;
        if (o.args() != null) {
            r = o.args();
        } else {
            r = Lists.of();
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
        if (subMeta instanceof Operation) {
            Operation o = (Operation) subMeta;
            if (o.operator().priority() > parent.operator().priority()) {
                return "(" + subMeta + ')';
            }
        }
        return subMeta.toString();
    }

    private QueryStructures() {
    }
}
