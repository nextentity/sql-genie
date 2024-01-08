package io.github.genie.sql.builder;

import io.github.genie.sql.api.Column;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.ExpressionHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Query.Having;
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
import io.github.genie.sql.api.Query.OrderBy;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
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
import io.github.genie.sql.builder.QueryStructures.QueryStructureImpl;
import io.github.genie.sql.builder.QueryStructures.SingleColumnSelect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class QueryConditionBuilder<T, U> implements Where0<T, U>, Having<T, U>, AbstractCollector<U> {

    static final SingleColumnSelect SELECT_ANY =
            new SingleColumnSelect(Integer.class, Expressions.TRUE);

    static final SingleColumnSelect COUNT_ANY =
            new SingleColumnSelect(Integer.class, Expressions.operate(Expressions.TRUE, Operator.COUNT));


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
    QueryStructures.QueryStructureImpl buildCountData() {
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

    boolean requiredCountSubQuery(QueryStructureImpl metadata) {
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
    QueryStructures.QueryStructureImpl buildListData(int offset, int maxResult, LockModeType lockModeType) {
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
    QueryStructures.QueryStructureImpl buildExistData(int offset) {
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
    public Having<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.groupBy = expressions.stream().map(ExpressionHolder::expression).toList();
        return update(metadata);
    }

    @Override
    public Having<T, U> groupBy(Path<T, ?> path) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.groupBy = List.of(Expressions.of(path));
        return update(metadata);
    }

    @Override
    public Having<T, U> groupBy(Collection<Path<T, ?>> paths) {
        return groupBy(Expressions.toExpressionList(paths));
    }


    @Override
    public OrderBy<T, U> having(ExpressionHolder<T, Boolean> predicate) {
        QueryStructureImpl metadata = queryStructure.copy();
        metadata.having = predicate.expression();
        return update(metadata);
    }


    @Override
    public <N extends Number & Comparable<N>> NumberOperator<T, N, AndBuilder0<T, U>> where(NumberPath<T, N> path) {
        return new NumberOpsImpl<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    @NotNull
    protected AndBuilder0<T, U> newChanAndBuilder(Metadata<AndBuilder0<T, U>> metadata) {
        return new AndBuilderImpl<>(QueryConditionBuilder.this, metadata);
    }

    @Override
    public <N extends Comparable<N>> ComparableOperator<T, N, AndBuilder0<T, U>> where(ComparablePath<T, N> path) {
        return new ComparableOpsImpl<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    @Override
    public StringOperator<T, AndBuilder0<T, U>> where(StringPath<T> path) {
        return new StringOpsImpl<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    @Override
    public AndBuilder0<T, U> where(BooleanPath<T> path) {
        return newChanAndBuilder(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }


    @Override
    public <N> PathOperator<T, N, AndBuilder0<T, U>> where(Path<T, N> path) {
        return new DefaultExpressionOperator<>(new Metadata<>(List.of(), Expressions.TRUE, Expressions.of(path), this::newChanAndBuilder));
    }

    QueryStructureImpl queryStructure() {
        return queryStructure;
    }
}
