package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.Predicate;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.Query;
import io.github.genie.sql.api.Query.AggAndBuilder;
import io.github.genie.sql.api.Query.AggGroupBy0;
import io.github.genie.sql.api.Query.AggWhere0;
import io.github.genie.sql.api.Query.Collector;
import io.github.genie.sql.api.Query.GroupBy0;
import io.github.genie.sql.api.Query.Having0;
import io.github.genie.sql.api.Query.OrderBy0;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import io.github.genie.sql.api.Query.Select0;
import io.github.genie.sql.api.Query.SliceQueryStructure;
import io.github.genie.sql.api.Query.Where0;
import io.github.genie.sql.api.QueryExecutor;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.builder.DefaultExpressionOperator.ComparableOpsImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.Metadata;
import io.github.genie.sql.builder.DefaultExpressionOperator.NumberOpsImpl;
import io.github.genie.sql.builder.DefaultExpressionOperator.StringOpsImpl;
import io.github.genie.sql.builder.QueryStructures.FromSubQuery;
import io.github.genie.sql.builder.QueryStructures.MultiColumnSelect;
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SelectClauseImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnSelect;
import io.github.genie.sql.builder.exception.BeanReflectiveException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryBuilder<T, U> implements Select0<T, U>, AggWhere0<T, U>, Having0<T, U>, AbstractCollector<U> {

    static final SingleColumnSelect SELECT_ANY =
            new SingleColumnSelect(Integer.class, Expressions.TRUE);

    static final SingleColumnSelect COUNT_ANY =
            new SingleColumnSelect(Integer.class, Expressions.operate(Expressions.TRUE, Operator.COUNT));


    private final QueryExecutor queryExecutor;
    private final QueryStructureImpl queryStructure;

    private final QueryStructurePostProcessor structurePostProcessor;

    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type) {
        this(queryExecutor, type, null);
    }

    public QueryBuilder(QueryExecutor queryExecutor, Class<T> type, QueryStructurePostProcessor structurePostProcessor) {
        this(queryExecutor, new QueryStructureImpl(type), structurePostProcessor);
    }


    QueryBuilder(QueryExecutor queryExecutor, QueryStructureImpl queryStructure, QueryStructurePostProcessor structurePostProcessor) {
        this.queryExecutor = queryExecutor;
        this.queryStructure = queryStructure;
        this.structurePostProcessor = structurePostProcessor == null ? QueryStructurePostProcessor.NONE : structurePostProcessor;
    }

    <X, Y> QueryBuilder<X, Y> update(QueryStructureImpl queryStructure) {
        return new QueryBuilder<>(queryExecutor, queryStructure, structurePostProcessor);
    }

    @Override
    public <R> Where0<T, R> select(Class<R> projectionType) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.select = new SelectClauseImpl(projectionType);
        return update(metadata);
    }


    @Override
    public <R> AggWhere0<T, R> select(Path<T, ? extends R> expression) {
        QueryStructureImpl metadata = queryStructure.copy();
        Expression paths = Expressions.of(expression);
        Class<?> type = getType(expression);
        metadata.select = new SingleColumnSelect(type, paths);
        return update(metadata);
    }

    @Override
    public AggWhere0<T, Object[]> select(Collection<Path<T, ?>> paths) {
        return select(Expressions.toExpressionList(paths));
    }

    @Override
    public AggWhere0<T, Object[]> select(List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.select = new MultiColumnSelect(expressions.stream().map(ExpressionHolder::expression).toList());
        return update(metadata);
    }

    @Override
    public <R> AggWhere0<T, R> select(ExpressionHolder<T, R> paths) {
        QueryStructureImpl metadata = queryStructure.copy();
        Expression expression = paths.expression();
        Class<?> type = Object.class;
        metadata.select = new SingleColumnSelect(type, expression);
        return update(metadata);
    }

    private Class<?> getType(Path<?, ?> path) {
        Class<?> fromClause = queryStructure.from().type();
        String name = Util.getReferenceMethodName(path);
        Method method;
        try {
            method = fromClause.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new BeanReflectiveException(e);
        }
        return method.getReturnType();
    }

    @Override
    public AggGroupBy0<T, U> where(ExpressionHolder<T, Boolean> predicate) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.where = predicate.expression();
        return update(metadata);
    }

    @Override
    public Collector<U> orderBy(List<? extends Order<T>> builder) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.orderBy = builder;
        return update(metadata);
    }

    @Override
    public int count() {
        QueryStructure metadata = buildCountData();
        metadata = structurePostProcessor.preCountQuery(this, metadata);
        return queryExecutor.<Number>getList(metadata).get(0).intValue();
    }

    @NotNull
    private QueryStructures.QueryStructureImpl buildCountData() {
        QueryStructureImpl structure = queryStructure.copy();
        structure.lockType = LockModeType.NONE;
        structure.orderBy = List.of();
        if (requiredCountSubQuery(queryStructure)) {
            structure.select = COUNT_ANY;
            return new QueryStructureImpl(COUNT_ANY, new FromSubQuery(structure));
        } else if (queryStructure.groupBy() != null && !queryStructure.groupBy().isEmpty()) {
            structure.select = SELECT_ANY;
            structure.fetch = List.of();
            return new QueryStructureImpl(COUNT_ANY, new FromSubQuery(structure));
        } else {
            structure.select = COUNT_ANY;
            structure.fetch = List.of();
            return structure;
        }
    }

    private boolean requiredCountSubQuery(QueryStructureImpl metadata) {
        Selection select = metadata.select();
        if (select instanceof SingleColumnSelect) {
            Expression column = ((SingleColumnSelect) select).column();
            return requiredCountSubQuery(column);
        } else if (select instanceof MultiColumn) {
            List<? extends Expression> columns = ((MultiColumn) select).columns();
            if (requiredCountSubQuery(columns)) {
                return true;
            }
        }
        return requiredCountSubQuery(metadata.having());
    }

    private boolean requiredCountSubQuery(List<? extends Expression> columns) {
        for (Expression column : columns) {
            if (requiredCountSubQuery(column)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiredCountSubQuery(Expression column) {
        if (column instanceof Column) {
            return false;
        } else if (column instanceof Operation operation) {
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
        QueryStructure metadata = buildListData(offset, maxResult, lockModeType);
        metadata = structurePostProcessor.preListQuery(this, metadata);
        return queryList(metadata);
    }

    public <X> List<X> queryList(QueryStructure metadata) {
        return queryExecutor.getList(metadata);
    }

    @NotNull
    private QueryStructures.QueryStructureImpl buildListData(int offset, int maxResult, LockModeType lockModeType) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.offset = offset;
        metadata.limit = maxResult;
        metadata.lockType = lockModeType;
        return metadata;
    }

    @Override
    public boolean exist(int offset) {
        QueryStructure metadata = buildExistData(offset);
        metadata = structurePostProcessor.preExistQuery(this, metadata);
        return !queryList(metadata).isEmpty();
    }

    @NotNull
    private QueryStructures.QueryStructureImpl buildExistData(int offset) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.select = SELECT_ANY;
        metadata.offset = offset;
        metadata.limit = 1;
        metadata.fetch = List.of();
        metadata.orderBy = List.of();
        return metadata;
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
    public Having0<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.groupBy = expressions.stream().map(ExpressionHolder::expression).toList();
        return update(metadata);
    }

    @Override
    public Having0<T, U> groupBy(Path<T, ?> path) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.groupBy = List.of(Expressions.of(path));
        return update(metadata);
    }

    @Override
    public Having0<T, U> groupBy(Collection<Path<T, ?>> paths) {
        return groupBy(Expressions.toExpressionList(paths));
    }

    @Override
    public GroupBy0<T, T> fetch(List<PathOperator<T, ?, Predicate<T>>> expressions) {
        QueryStructureImpl metadata = queryStructure.copy();
        List<Column> list = new ArrayList<>(expressions.size());
        for (PathOperator<T, ?, Predicate<T>> expression : expressions) {
            Expression expr = expression.expression();
            if (expr instanceof Column column) {
                list.add(column);
            }
        }
        metadata.fetch = list;
        return update(metadata);
    }

    @Override
    public GroupBy0<T, T> fetch(Collection<Path<T, ?>> paths) {
        return fetch(Expressions.toExpressionList(paths));
    }

    @Override
    public OrderBy0<T, U> having(ExpressionHolder<T, Boolean> predicate) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.having = predicate.expression();
        return update(metadata);
    }


    @Override
    public <N extends Number & Comparable<N>> NumberOperator<T, N, AggAndBuilder<T, U>> where(NumberPath<T, N> path) {
        return new NumberOpsImpl<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    @NotNull
    private Query.AggAndBuilder<T, U> newChanAndBuilder(Metadata<AggAndBuilder<T, U>> metadata) {
        return new AndBuilderImpl<>(QueryBuilder.this, metadata);
    }

    @Override
    public <N extends Comparable<N>> ComparableOperator<T, N, AggAndBuilder<T, U>> where(ComparablePath<T, N> path) {
        return new ComparableOpsImpl<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    @Override
    public StringOperator<T, AggAndBuilder<T, U>> where(StringPath<T> path) {
        return new StringOpsImpl<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    @Override
    public AggAndBuilder<T, U> where(BooleanPath<T> path) {
        return newChanAndBuilder(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }


    @Override
    public <N> PathOperator<T, N, AggAndBuilder<T, U>> where(Path<T, N> path) {
        return new DefaultExpressionOperator<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    QueryStructureImpl queryStructure() {
        return queryStructure;
    }
}
