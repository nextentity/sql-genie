package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.EntityRoot;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Lists;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.Query.AndBuilder0;
import io.github.genie.sql.api.Query.Collector;
import io.github.genie.sql.api.Query.GroupBy;
import io.github.genie.sql.api.Query.Having;
import io.github.genie.sql.api.Query.OrderBy;
import io.github.genie.sql.api.Query.OrderOperator;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import io.github.genie.sql.api.Query.SliceQueryStructure;
import io.github.genie.sql.api.Query.Where0;
import io.github.genie.sql.api.QueryExecutor;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.api.TypedExpression;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.PathOperatorImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOperatorImpl;
import io.github.genie.sql.builder.QueryBuilder.AndBuilderImpl;
import io.github.genie.sql.builder.QueryStructures.FromSubQuery;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("PatternVariableCanBeUsed")
public class QueryConditionBuilder<T, U> implements Where0<T, U>, Having<T, U>, AbstractCollector<U> {

    static final SingleColumnImpl SELECT_ANY =
            new SingleColumnImpl(Integer.class, Expressions.TRUE, false);

    static final SingleColumnImpl COUNT_ANY =
            new SingleColumnImpl(Integer.class, Expressions.operate(Expressions.TRUE, Operator.COUNT), false);

    final QueryExecutor queryExecutor;
    final QueryStructureImpl queryStructure;

    protected final QueryStructurePostProcessor structurePostProcessor;

    public QueryConditionBuilder(QueryExecutor queryExecutor, Class<T> type) {
        this(queryExecutor, type, null);
    }

    public QueryConditionBuilder(QueryExecutor queryExecutor, Class<T> type, QueryStructurePostProcessor structurePostProcessor) {
        this(queryExecutor, new QueryStructureImpl(type), structurePostProcessor);
    }

    QueryConditionBuilder(QueryExecutor queryExecutor, QueryStructureImpl queryStructure, QueryStructurePostProcessor structurePostProcessor) {
        this.queryExecutor = queryExecutor;
        this.queryStructure = queryStructure;
        this.structurePostProcessor = structurePostProcessor == null ? QueryStructurePostProcessor.NONE : structurePostProcessor;
    }

    <X, Y> QueryConditionBuilder<X, Y> update(QueryStructureImpl queryStructure) {
        return new QueryConditionBuilder<>(queryExecutor, queryStructure, structurePostProcessor);
    }

    @Override
    public GroupBy<T, U> where(ExpressionHolder<T, Boolean> predicate) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.where = predicate.expression();
        return update(structure);
    }

    @Override
    public GroupBy<T, U> where(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder) {
        return where(predicateBuilder.apply(EntityRootImpl.of()));
    }

    @Override
    public Collector<U> orderBy(List<? extends Order<T>> orders) {
        return addOrderBy(orders);
    }

    @Override
    public Collector<U> orderBy(Function<EntityRoot<T>, List<? extends Order<T>>> ordersBuilder) {
        return orderBy(ordersBuilder.apply(EntityRootImpl.of()));
    }

    @Override
    public OrderOperator<T, U> orderBy(Collection<Path<T, Comparable<?>>> paths) {
        return new OrderOperatorImpl<>(this, paths);
    }

    QueryConditionBuilder<T, U> addOrderBy(List<? extends Order<T>> orders) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.orderBy = structure.orderBy == null
                ? orders
                : Util.concat(structure.orderBy, orders);
        return update(structure);
    }

    @Override
    public long count() {
        QueryStructure structure = buildCountData();
        structure = structurePostProcessor.preCountQuery(this, structure);
        return queryExecutor.<Number>getList(structure).get(0).longValue();
    }

    @NotNull
    QueryStructures.QueryStructureImpl buildCountData() {
        QueryStructureImpl structure = queryStructure.copy();
        structure.lockType = LockModeType.NONE;
        structure.orderBy = Lists.of();
        if (queryStructure.select().distinct()) {
            return new QueryStructureImpl(COUNT_ANY, new FromSubQuery(structure));
        } else if (requiredCountSubQuery(queryStructure)) {
            structure.select = COUNT_ANY;
            return new QueryStructureImpl(COUNT_ANY, new FromSubQuery(structure));
        } else if (queryStructure.groupBy() != null && !queryStructure.groupBy().isEmpty()) {
            structure.select = SELECT_ANY;
            structure.fetch = Lists.of();
            return new QueryStructureImpl(COUNT_ANY, new FromSubQuery(structure));
        } else {
            structure.select = COUNT_ANY;
            structure.fetch = Lists.of();
            return structure;
        }
    }

    boolean requiredCountSubQuery(QueryStructureImpl structure) {
        Selection select = structure.select();
        if (select instanceof SingleColumnImpl) {
            Expression column = ((SingleColumnImpl) select).column();
            return requiredCountSubQuery(column);
        } else if (select instanceof MultiColumn) {
            List<? extends Expression> columns = ((MultiColumn) select).columns();
            if (requiredCountSubQuery(columns)) {
                return true;
            }
        }
        return requiredCountSubQuery(structure.having());
    }

    protected boolean requiredCountSubQuery(List<? extends Expression> columns) {
        for (Expression column : columns) {
            if (requiredCountSubQuery(column)) {
                return true;
            }
        }
        return false;
    }

    protected boolean requiredCountSubQuery(Expression column) {
        if (column instanceof Column) {
            return false;
        } else if (column instanceof Operation) {
            Operation operation = (Operation) column;
            Expression expression = operation.operand();
            if (requiredCountSubQuery(expression)) {
                return true;
            }
            List<? extends Expression> args = operation.args();
            if (args != null) {
                for (Expression arg : args) {
                    if (requiredCountSubQuery(arg)) {
                        return true;
                    }
                }
            }
            return operation.operator().isAgg();
        }
        return false;
    }

    @Override
    public List<U> getList(int offset, int maxResult, LockModeType lockModeType) {
        QueryStructure structure = buildListData(offset, maxResult, lockModeType);
        structure = structurePostProcessor.preListQuery(this, structure);
        return queryList(structure);
    }

    public <X> List<X> queryList(QueryStructure structure) {
        return queryExecutor.getList(structure);
    }

    @NotNull
    QueryStructures.QueryStructureImpl buildListData(int offset, int maxResult, LockModeType lockModeType) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.offset = offset;
        structure.limit = maxResult;
        structure.lockType = lockModeType;
        return structure;
    }

    @Override
    public boolean exist(int offset) {
        QueryStructure structure = buildExistData(offset);
        structure = structurePostProcessor.preExistQuery(this, structure);
        return !queryList(structure).isEmpty();
    }

    @NotNull
    QueryStructures.QueryStructureImpl buildExistData(int offset) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.select = SELECT_ANY;
        structure.offset = offset;
        structure.limit = 1;
        structure.fetch = Lists.of();
        structure.orderBy = Lists.of();
        return structure;
    }

    @Override
    public QueryStructureBuilder buildMetadata() {
        return new QueryStructureBuilder() {
            @Override
            public QueryStructure count() {
                return buildCountData();
            }

            @Override
            public QueryStructure getList(int offset, int maxResult, LockModeType lockModeType) {
                return buildListData(offset, maxResult, lockModeType);
            }

            @Override
            public QueryStructure exist(int offset) {
                return buildExistData(offset);
            }

            @Override
            public SliceQueryStructure slice(int offset, int limit) {
                return new SliceQueryStructure(
                        buildCountData(),
                        buildListData(offset, limit, LockModeType.NONE)
                );
            }

        };
    }

    @Override
    public Having<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.groupBy = expressions.stream()
                .map(ExpressionHolder::expression)
                .collect(Collectors.toList());
        return update(structure);
    }

    @Override
    public Having<T, U> groupBy(Function<EntityRoot<T>, List<? extends ExpressionHolder<T, ?>>> expressionBuilder) {
        return groupBy(expressionBuilder.apply(EntityRootImpl.of()));
    }

    @Override
    public Having<T, U> groupBy(Path<T, ?> path) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.groupBy = Lists.of(Expressions.of(path));
        return update(structure);
    }

    @Override
    public Having<T, U> groupBy(Collection<Path<T, ?>> paths) {
        return groupBy(Expressions.toExpressionList(paths));
    }

    @Override
    public OrderBy<T, U> having(ExpressionHolder<T, Boolean> predicate) {
        QueryStructureImpl structure = queryStructure.copy();
        structure.having = predicate.expression();
        return update(structure);
    }

    @Override
    public OrderBy<T, U> having(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder) {
        return having(predicateBuilder.apply(EntityRootImpl.of()));
    }

    @Override
    public <N extends Number & Comparable<N>> NumberOperator<T, N, AndBuilder0<T, U>> where(NumberPath<T, N> path) {
        return new NumberOperatorImpl<>(root().get(path), this::newAndBuilder);
    }

    @Override
    public <N extends Comparable<N>> ComparableOperator<T, N, AndBuilder0<T, U>> where(ComparablePath<T, N> path) {
        return new ComparableOperatorImpl<>(root().get(path), this::newAndBuilder);
    }

    @NotNull
    private AndBuilderImpl<T, U> newAndBuilder(TypedExpression<?, ?> expression) {
        return new AndBuilderImpl<>(QueryConditionBuilder.this, TypeCastUtil.unsafeCast(expression));
    }

    @Override
    public StringOperator<T, AndBuilder0<T, U>> where(StringPath<T> path) {
        return new StringOperatorImpl<>(root().get(path), this::newAndBuilder);
    }

    @Override
    public AndBuilder0<T, U> where(BooleanPath<T> path) {
        return new AndBuilderImpl<>(this, root().get(path));
    }

    public EntityRoot<T> root() {
        return EntityRootImpl.of();
    }

    @Override
    public <N> PathOperator<T, N, AndBuilder0<T, U>> where(Path<T, N> path) {
        return new PathOperatorImpl<>(root().get(path), this::newAndBuilder);
    }

    QueryStructureImpl queryStructure() {
        return queryStructure;
    }

}
