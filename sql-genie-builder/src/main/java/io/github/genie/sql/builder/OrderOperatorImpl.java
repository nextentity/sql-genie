package io.github.genie.sql.builder;

import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.Path;
import io.github.genie.sql.api.Query.Collector;
import io.github.genie.sql.api.Query.OrderBy;
import io.github.genie.sql.api.Query.OrderOperator;
import io.github.genie.sql.api.Query.QueryStructureBuilder;
import io.github.genie.sql.api.Slice;
import io.github.genie.sql.api.Sliceable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OrderOperatorImpl<T, U> implements OrderOperator<T, U> {
    private final QueryConditionBuilder<T, U> builder;
    private final Collection<Path<T, Comparable<?>>> orderByPaths;

    public OrderOperatorImpl(QueryConditionBuilder<T, U> builder, Collection<Path<T, Comparable<?>>> orderByPaths) {
        this.builder = builder;
        this.orderByPaths = orderByPaths;
    }


    @NotNull
    private List<Order<T>> asOrderList(Order.SortOrder sort) {
        return orderByPaths
                .stream()
                .map(path -> Q.orderBy(path, sort))
                .collect(Collectors.toList());
    }

    @Override
    public OrderBy<T, U> sort(SortOrder order) {
        return builder.addOrderBy(asOrderList(order));
    }

    @Override
    public Collector<U> orderBy(List<? extends Order<T>> orders) {
        return asc().orderBy(orders);
    }

    @Override
    public OrderOperator<T, U> orderBy(Collection<Path<T, Comparable<?>>> paths) {
        return asc().orderBy(paths);
    }

    @Override
    public int count() {
        return asc().count();
    }

    @Override
    public List<U> getList(int offset, int maxResult, LockModeType lockModeType) {
        return asc().getList(offset, maxResult, lockModeType);
    }

    @Override
    public boolean exist(int offset) {
        return asc().exist(offset);
    }

    @Override
    public <R> R slice(Sliceable<U, R> sliceable) {
        return asc().slice(sliceable);
    }

    @Override
    public Slice<U> slice(int offset, int limit) {
        return asc().slice(offset, limit);
    }

    @Override
    public QueryStructureBuilder buildMetadata() {
        return asc().buildMetadata();
    }


}
