package io.github.genie.sql.api;

import io.github.genie.sql.api.ExpressionHolder.ColumnHolder;
import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import io.github.genie.sql.api.tuple.Tuple;
import io.github.genie.sql.api.tuple.Tuple10;
import io.github.genie.sql.api.tuple.Tuple2;
import io.github.genie.sql.api.tuple.Tuple3;
import io.github.genie.sql.api.tuple.Tuple4;
import io.github.genie.sql.api.tuple.Tuple5;
import io.github.genie.sql.api.tuple.Tuple6;
import io.github.genie.sql.api.tuple.Tuple7;
import io.github.genie.sql.api.tuple.Tuple8;
import io.github.genie.sql.api.tuple.Tuple9;
import lombok.Data;
import lombok.experimental.Accessors;

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

        Where0<T, Tuple> select(List<? extends ExpressionHolder<T, ?>> paths);

        Where0<T, Tuple> select(Function<Root<T>, List<? extends ExpressionHolder<T, ?>>> selectBuilder);

        <R> Where0<T, R> select(ExpressionHolder<T, R> expression);

        <R> Where0<T, R> select(Path<T, ? extends R> path);

        Where0<T, Tuple> select(Collection<Path<T, ?>> paths);

        <A, B> Where0<T, Tuple2<A, B>> select(Path<T, A> a, Path<T, B> b);

        <A, B, C> Where0<T, Tuple3<A, B, C>> select(Path<T, A> a, Path<T, B> b, Path<T, C> c);

        <A, B, C, D> Where0<T, Tuple4<A, B, C, D>> select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d);

        <A, B, C, D, E> Where0<T, Tuple5<A, B, C, D, E>>
        select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e);

        <A, B, C, D, E, F> Where0<T, Tuple6<A, B, C, D, E, F>>
        select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f);

        <A, B, C, D, E, F, G> Where0<T, Tuple7<A, B, C, D, E, F, G>>
        select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g);

        <A, B, C, D, E, F, G, H> Where0<T, Tuple8<A, B, C, D, E, F, G, H>>
        select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h);

        <A, B, C, D, E, F, G, H, I> Where0<T, Tuple9<A, B, C, D, E, F, G, H, I>>
        select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i);

        <A, B, C, D, E, F, G, H, I, J> Where0<T, Tuple10<A, B, C, D, E, F, G, H, I, J>>
        select(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i, Path<T, J> j);

        <R> Where<T, R> selectDistinct(Class<R> projectionType);

        Where0<T, Tuple> selectDistinct(List<? extends ExpressionHolder<T, ?>> paths);

        Where0<T, Tuple> selectDistinct(Function<Root<T>, List<? extends ExpressionHolder<T, ?>>> selectBuilder);

        <R> Where0<T, R> selectDistinct(ExpressionHolder<T, R> expression);

        <R> Where0<T, R> selectDistinct(Path<T, ? extends R> path);

        Where0<T, Tuple> selectDistinct(Collection<Path<T, ?>> paths);

        <A, B> Where0<T, Tuple2<A, B>> selectDistinct(Path<T, A> a, Path<T, B> b);

        <A, B, C> Where0<T, Tuple3<A, B, C>> selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c);

        <A, B, C, D> Where0<T, Tuple4<A, B, C, D>> selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d);

        <A, B, C, D, E> Where0<T, Tuple5<A, B, C, D, E>>
        selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e);

        <A, B, C, D, E, F> Where0<T, Tuple6<A, B, C, D, E, F>>
        selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f);

        <A, B, C, D, E, F, G> Where0<T, Tuple7<A, B, C, D, E, F, G>>
        selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g);

        <A, B, C, D, E, F, G, H> Where0<T, Tuple8<A, B, C, D, E, F, G, H>>
        selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h);

        <A, B, C, D, E, F, G, H, I> Where0<T, Tuple9<A, B, C, D, E, F, G, H, I>>
        selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i);

        <A, B, C, D, E, F, G, H, I, J> Where0<T, Tuple10<A, B, C, D, E, F, G, H, I, J>>
        selectDistinct(Path<T, A> a, Path<T, B> b, Path<T, C> c, Path<T, D> d, Path<T, E> e, Path<T, F> f, Path<T, G> g, Path<T, H> h, Path<T, I> i, Path<T, J> j);


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

        Where<T, U> where(ExpressionHolder<T, Boolean> predicate);

        Where<T, U> where(Function<Root<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

        default Where<T, U> whereIf(boolean predicate, Function<Root<T>, ExpressionHolder<T, Boolean>> predicateBuilder) {
            if (predicate) {
                return where(predicateBuilder.apply(root()));
            }
            return this;
        }

        <N> PathOperator<T, N, ? extends Where<T, U>> where(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, ? extends Where<T, U>> where(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, ? extends Where<T, U>> where(ComparablePath<T, N> path);

        StringOperator<T, ? extends Where<T, U>> where(StringPath<T> path);

    }

    interface Where0<T, U> extends GroupBy<T, U>, Where<T, U> {

        Where0<T, U> where(ExpressionHolder<T, Boolean> predicate);

        Where0<T, U> where(Function<Root<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

        default Where0<T, U> whereIf(boolean predicate, Function<Root<T>, ExpressionHolder<T, Boolean>> predicateBuilder) {
            if (predicate) {
                return where(predicateBuilder.apply(root()));
            }
            return this;
        }

        <N> PathOperator<T, N, Where0<T, U>> where(Path<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, Where0<T, U>> where(ComparablePath<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, Where0<T, U>> where(NumberPath<T, N> path);

        StringOperator<T, Where0<T, U>> where(StringPath<T> path);

    }

    interface GroupBy<T, U> extends OrderBy<T, U> {
        Having<T, U> groupBy(List<? extends ExpressionHolder<T, ?>> expressions);

        Having<T, U> groupBy(Function<Root<T>, List<? extends ExpressionHolder<T, ?>>> expressionBuilder);

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

        OrderBy<T, U> having(Function<Root<T>, ExpressionHolder<T, Boolean>> predicateBuilder);

    }

    interface OrderBy<T, U> extends Collector<U>, RootProvider<T> {

        Collector<U> orderBy(List<? extends Order<T>> orders);

        Collector<U> orderBy(Function<Root<T>, List<? extends Order<T>>> ordersBuilder);

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

    interface RootProvider<T> {
        Root<T> root();
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

        <R> R slice(Sliceable<T, R> sliceable);

        Slice<T> slice(int offset, int limit);

        QueryStructureBuilder buildMetadata();

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
