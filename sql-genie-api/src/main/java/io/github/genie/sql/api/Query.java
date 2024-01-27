package io.github.genie.sql.api;

import io.github.genie.sql.api.ExpressionHolder.ColumnHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static io.github.genie.sql.api.Order.SortOrder.ASC;
import static io.github.genie.sql.api.Order.SortOrder.DESC;

public interface Query {

    <T> Select<T> from(Class<T> type);

    interface Select<T> extends Fetch<T> {

        <R> Where<T, R> select(Class<R> projectionType);

        Where0<T, Object[]> select(List<? extends ExpressionHolder<T, ?>> paths);

        Where0<T, Object[]> select(Function<EntityRoot<T>, List<? extends ExpressionHolder<T, ?>>> selectBuilder);

        <R> Where0<T, R> select(ExpressionHolder<T, R> expression);

        <R> Where0<T, R> select(Path<T, ? extends R> path);

        Where0<T, Object[]> select(Collection<Path<T, ?>> paths);

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1) {
            return select(Lists.of(p0, p1));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return select(Lists.of(p0, p1, p2));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return select(Lists.of(p0, p1, p2, p3));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return select(Lists.of(p0, p1, p2, p3, p4));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                           Path<T, ?> p5) {
            return select(Lists.of(p0, p1, p2, p3, p4, p5));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                           Path<T, ?> p5, Path<T, ?> p6) {
            return select(Lists.of(p0, p1, p2, p3, p4, p5, p6));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                           Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7) {
            return select(Lists.of(p0, p1, p2, p3, p4, p5, p6, p7));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                           Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8) {
            return select(Lists.of(p0, p1, p2, p3, p4, p5, p6, p7, p8));
        }

        default Where0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                           Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8, Path<T, ?> p9) {
            return select(Lists.of(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
        }

        <R> Where<T, R> selectDistinct(Class<R> projectionType);

        Where0<T, Object[]> selectDistinct(List<? extends ExpressionHolder<T, ?>> paths);

        Where0<T, Object[]> selectDistinct(Function<EntityRoot<T>, List<? extends ExpressionHolder<T, ?>>> selectBuilder);

        <R> Where0<T, R> selectDistinct(ExpressionHolder<T, R> expression);

        <R> Where0<T, R> selectDistinct(Path<T, ? extends R> path);

        Where0<T, Object[]> selectDistinct(Collection<Path<T, ?>> paths);

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1) {
            return selectDistinct(Lists.of(p0, p1));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return selectDistinct(Lists.of(p0, p1, p2));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return selectDistinct(Lists.of(p0, p1, p2, p3));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return selectDistinct(Lists.of(p0, p1, p2, p3, p4));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                                   Path<T, ?> p5) {
            return selectDistinct(Lists.of(p0, p1, p2, p3, p4, p5));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                                   Path<T, ?> p5, Path<T, ?> p6) {
            return selectDistinct(Lists.of(p0, p1, p2, p3, p4, p5, p6));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                                   Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7) {
            return selectDistinct(Lists.of(p0, p1, p2, p3, p4, p5, p6, p7));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                                   Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8) {
            return selectDistinct(Lists.of(p0, p1, p2, p3, p4, p5, p6, p7, p8));
        }

        default Where0<T, Object[]> selectDistinct(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                                   Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8, Path<T, ?> p9) {
            return selectDistinct(Lists.of(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
        }

    }

    interface Fetch<T> extends Where<T, T> {

        Where<T, T> fetch(List<ColumnHolder<T, ?>> expressions);

        default Where<T, T> fetch(ColumnHolder<T, ?> path) {
            return fetch(Lists.of(path));
        }

        default Where<T, T> fetch(ColumnHolder<T, ?> p0, ColumnHolder<T, ?> p1) {
            return fetch(Lists.of(p0, p1));
        }

        default Where<T, T> fetch(ColumnHolder<T, ?> p0, ColumnHolder<T, ?> p1, ColumnHolder<T, ?> p3) {
            return fetch(Lists.of(p0, p1, p3));
        }

        Where<T, T> fetch(Collection<Path<T, ?>> paths);

        default Where<T, T> fetch(Path<T, ?> path) {
            return fetch(Lists.of(path));
        }

        default Where<T, T> fetch(Path<T, ?> p0, Path<T, ?> p1) {
            return fetch(Lists.of(p0, p1));
        }

        default Where<T, T> fetch(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p3) {
            return fetch(Lists.of(p0, p1, p3));
        }

    }

    interface Where<T, U> extends OrderBy<T, U> {

        OrderBy<T, U> where(ExpressionHolder<T, Boolean> predicate);

        OrderBy<T, U> where(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

        <N> PathOperator<T, N, ? extends AndBuilder<T, U>> where(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, ? extends AndBuilder<T, U>> where(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, ? extends AndBuilder<T, U>> where(ComparablePath<T, N> path);

        StringOperator<T, ? extends AndBuilder<T, U>> where(StringPath<T> path);

        AndBuilder<T, U> where(BooleanPath<T> path);

    }

    interface Where0<T, U> extends GroupBy<T, U>, Where<T, U> {

        GroupBy<T, U> where(ExpressionHolder<T, Boolean> predicate);

        GroupBy<T, U> where(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

        <N> PathOperator<T, N, AndBuilder0<T, U>> where(Path<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, AndBuilder0<T, U>> where(ComparablePath<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, AndBuilder0<T, U>> where(NumberPath<T, N> path);

        StringOperator<T, AndBuilder0<T, U>> where(StringPath<T> path);

        AndBuilder0<T, U> where(BooleanPath<T> path);

    }

    interface GroupBy<T, U> extends OrderBy<T, U> {
        Having<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions);

        Having<T, U> groupBy(Function<EntityRoot<T>, List<? extends ExpressionHolder<T, ?>>> expressionBuilder);

        Having<T, U> groupBy(Path<T, ?> path);

        Having<T, U> groupBy(Collection<Path<T, ?>> paths);

        default Having<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1) {
            return groupBy(Lists.of(p0, p1));
        }

        default Having<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return groupBy(Lists.of(p0, p1, p2));
        }

        default Having<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return groupBy(Lists.of(p0, p1, p2, p3));
        }

        default Having<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return groupBy(Lists.of(p0, p1, p2, p3, p4));
        }

        default Having<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4, Path<T, ?> p5) {
            return groupBy(Lists.of(p0, p1, p2, p3, p4, p5));
        }
    }

    interface Having<T, U> extends OrderBy<T, U> {

        OrderBy<T, U> having(ExpressionHolder<T, Boolean> predicate);

        OrderBy<T, U> having(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

    }

    interface OrderBy<T, U> extends Collector<U> {

        Collector<U> orderBy(List<? extends Order<T>> orders);

        Collector<U> orderBy(Function<EntityRoot<T>, List<? extends Order<T>>> ordersBuilder);

        default Collector<U> orderBy(Order<T> order) {
            return orderBy(Lists.of(order));
        }

        default Collector<U> orderBy(Order<T> p0, Order<T> p1) {
            return orderBy(Lists.of(p0, p1));
        }

        default Collector<U> orderBy(Order<T> order1, Order<T> order2, Order<T> order3) {
            return orderBy(Lists.of(order1, order2, order3));
        }

        OrderOperator<T, U> orderBy(Collection<Path<T, Comparable<?>>> paths);

        default OrderOperator<T, U> orderBy(Path<T, Comparable<?>> path) {
            return orderBy(Lists.of(path));
        }

        default OrderOperator<T, U> orderBy(Path<T, Comparable<?>> p1, Path<T, Comparable<?>> p2) {
            return orderBy(Lists.of(p1, p2));
        }

        default OrderOperator<T, U> orderBy(Path<T, Comparable<?>> p1, Path<T, Comparable<?>> p2, Path<T, Comparable<?>> p3) {
            return orderBy(Lists.of(p1, p2, p3));
        }

    }

    interface OrderOperator<T, U> extends OrderBy<T, U> {
        default OrderBy<T, U> asc() {
            return sort(ASC);
        }

        default OrderBy<T, U> desc() {
            return sort(DESC);
        }

        OrderBy<T, U> sort(Order.SortOrder order);
    }

    interface Collector<T> {

        long count();

        List<T> getList(int offset, int maxResult, LockModeType lockModeType);

        default List<T> getList(int offset, int maxResult) {
            return getList(offset, maxResult, null);
        }

        boolean exist(int offset);

        default Optional<T> first() {
            return Optional.ofNullable(getFirst());
        }

        default Optional<T> first(int offset) {
            return Optional.ofNullable(getFirst(offset));
        }

        default T getFirst() {
            return getFirst(-1);
        }

        default T getFirst(int offset) {
            List<T> list = getList(offset, 1);
            return list.isEmpty() ? null : list.get(0);
        }

        default T requireSingle() {
            return Objects.requireNonNull(getSingle(-1));
        }

        default Optional<T> single() {
            return Optional.ofNullable(getSingle());
        }

        default Optional<T> single(int offset) {
            return Optional.ofNullable(getSingle(offset));
        }

        default T getSingle() {
            return getSingle(-1);
        }

        default T getSingle(int offset) {
            List<T> list = getList(offset, 2);
            if (list.size() > 1) {
                throw new IllegalStateException("found more than one");
            }
            return list.isEmpty() ? null : list.get(0);
        }

//        default List<T> getList(int offset) {
//            return getList(offset, -1);
//        }

        default List<T> getList() {
            return getList(-1, -1);
        }

        default boolean exist() {
            return exist(-1);
        }

        default Optional<T> first(LockModeType lockModeType) {
            return Optional.ofNullable(getFirst(lockModeType));
        }

        default Optional<T> first(int offset, LockModeType lockModeType) {
            return Optional.ofNullable(getFirst(offset, lockModeType));
        }

        default T getFirst(LockModeType lockModeType) {
            return getFirst(-1, lockModeType);
        }

        default T getFirst(int offset, LockModeType lockModeType) {
            List<T> list = getList(offset, 1, lockModeType);
            return list.isEmpty() ? null : list.get(0);
        }

        default T requireSingle(LockModeType lockModeType) {
            return Objects.requireNonNull(getSingle(-1, lockModeType));
        }

        default Optional<T> single(LockModeType lockModeType) {
            return Optional.ofNullable(getSingle(lockModeType));
        }

        default Optional<T> single(int offset, LockModeType lockModeType) {
            return Optional.ofNullable(getSingle(offset, lockModeType));
        }

        default T getSingle(LockModeType lockModeType) {
            return getSingle(-1, lockModeType);
        }

        default T getSingle(int offset, LockModeType lockModeType) {
            List<T> list = getList(offset, 2, lockModeType);
            if (list.size() > 1) {
                throw new IllegalStateException("found more than one");
            }
            return list.isEmpty() ? null : list.get(0);
        }

        default List<T> getList(int offset, LockModeType lockModeType) {
            return getList(offset, -1, lockModeType);
        }

        default List<T> getList(LockModeType lockModeType) {
            return getList(-1, -1, lockModeType);
        }

        default <R> R getResult(@NotNull Function<? super Collector<T>, R> function) {
            return function.apply(this);
        }

        <R> R slice(Sliceable<T, R> sliceable);

        Slice<T> slice(int offset, int limit);

        QueryStructureBuilder buildMetadata();

    }

    interface AndBuilder<T, U> extends OrderBy<T, U> {

        <N> PathOperator<T, N, ? extends AndBuilder<T, U>> and(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, ? extends AndBuilder<T, U>> and(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, ? extends AndBuilder<T, U>> and(ComparablePath<T, N> path);

        StringOperator<T, ? extends AndBuilder<T, U>> and(StringPath<T> path);

        AndBuilder<T, U> and(BooleanPath<T> path);

        AndBuilder<T, U> and(ExpressionHolder<T, Boolean> predicate);

        AndBuilder<T, U> and(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

    }

    interface AndBuilder0<T, U> extends GroupBy<T, U>, AndBuilder<T, U> {
        <N> PathOperator<T, N, AndBuilder0<T, U>> and(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, AndBuilder0<T, U>> and(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, AndBuilder0<T, U>> and(ComparablePath<T, N> path);

        AndBuilder0<T, U> and(BooleanPath<T> path);

        StringOperator<T, AndBuilder0<T, U>> and(StringPath<T> path);

        AndBuilder0<T, U> and(ExpressionHolder<T, Boolean> predicate);

        AndBuilder0<T, U> and(Function<EntityRoot<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

    }

    interface QueryStructureBuilder {

        QueryStructure count();

        QueryStructure getList(int offset, int maxResult, LockModeType lockModeType);

        QueryStructure exist(int offset);

        SliceQueryStructure slice(int offset, int limit);

    }

    @Data
    @Accessors(fluent = true)
    @SuppressWarnings("ClassCanBeRecord")
    final class SliceQueryStructure {
        private final QueryStructure count;
        private final QueryStructure list;
    }

}
