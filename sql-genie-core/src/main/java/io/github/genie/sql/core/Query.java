package io.github.genie.sql.core;

import io.github.genie.sql.core.ExpressionOps.*;
import io.github.genie.sql.core.Path.BooleanPath;
import io.github.genie.sql.core.Path.ComparablePath;
import io.github.genie.sql.core.Path.NumberPath;
import io.github.genie.sql.core.Path.StringPath;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface Query {

    <T> Select0<T, T> from(Class<T> type);

    static Query createQuery(QueryExecutor executor) {
        return new Query() {
            @Override
            public <T> Select0<T, T> from(Class<T> type) {
                return new QueryBuilder<>(executor, type);
            }
        };
    }


    interface Select0<T, U> extends Fetch<T>, Select<T>, GroupBy0<T, U> {
    }

    interface GroupBy0<T, U> extends GroupBy<T, U>, Where0<T, U> {
    }

    interface Where0<T, U> extends Where<T, U>, OrderBy0<T, U> {
    }

    interface OrderBy0<T, U> extends OrderBy<T, U>, Collector<U> {
    }

    interface AggWhere0<T, U> extends AggWhere<T, U>, AggGroupBy0<T, U> {
    }

    interface AggGroupBy0<T, U> extends OrderBy0<T, U>, GroupBy<T, U> {
    }

    interface Having0<T, U> extends Having<T, U>, OrderBy0<T, U> {
    }


    interface Fetch<T> {

        GroupBy0<T, T> fetch(List<PathExpr<T, ?>> expressions);

        default GroupBy0<T, T> fetch(Path<T, ?> path) {
            return fetch(Metas.toExpressionList(path));
        }


        default GroupBy0<T, T> fetch(Path<T, ?> p0, Path<T, ?> p1) {
            return fetch(Metas.toExpressionList(p0, p1));
        }

        default GroupBy0<T, T> fetch(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p3) {
            return fetch(Metas.toExpressionList(p0, p1, p3));
        }

    }

    interface Select<T> {

        <R> Where0<T, R> select(Class<R> projectionType);

        AggWhere0<T, Object[]> select(List<? extends Expression<T, ?>> paths);

        <R> AggWhere0<T, R> select(Path<T, ? extends R> expression);

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1) {
            return select(Metas.toExpressionList(p0, p1));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return select(Metas.toExpressionList(p0, p1, p2));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return select(Metas.toExpressionList(p0, p1, p2, p3));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return select(Metas.toExpressionList(p0, p1, p2, p3, p4));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5) {
            return select(Metas.toExpressionList(p0, p1, p2, p3, p4, p5));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6) {
            return select(Metas.toExpressionList(p0, p1, p2, p3, p4, p5, p6));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7) {
            return select(Metas.toExpressionList(p0, p1, p2, p3, p4, p5, p6, p7));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8) {
            return select(Metas.toExpressionList(p0, p1, p2, p3, p4, p5, p6, p7, p8));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8, Path<T, ?> p9) {
            return select(Metas.toExpressionList(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
        }


    }


    interface AggWhere<T, U> extends Where<T, U> {

        AggGroupBy0<T, U> where(Expression<T, Boolean> predicate);

        @Override
        <N> PathOps<T, N, AggAndBuilder<T, U>> where(Path<T, N> path);

        @Override
        <N extends Comparable<N>> ComparableOps<T, N, AggAndBuilder<T, U>> where(ComparablePath<T, N> path);

        @Override
        <N extends Number & Comparable<N>> NumberOps<T, N, AggAndBuilder<T, U>> where(NumberPath<T, N> path);

        @Override
        StringOps<T, AggAndBuilder<T, U>> where(StringPath<T> path);

        @Override
        AggAndBuilder<T, U> where(BooleanPath<T> path);

    }

    interface Where<T, U> {

        OrderBy0<T, U> where(Expression<T, Boolean> predicate);

        <N> PathOps<T, N, ? extends AndBuilder<T, U>> where(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOps<T, N, ? extends AndBuilder<T, U>> where(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOps<T, N, ? extends AndBuilder<T, U>> where(ComparablePath<T, N> path);

        StringOps<T, ? extends AndBuilder<T, U>> where(StringPath<T> path);

        AndBuilder<T, U> where(BooleanPath<T> path);

    }

    interface AggAndBuilder<T, U> extends AndBuilder<T, U>, AggGroupBy0<T, U> {
        @Override
        <N> PathOps<T, N, AggAndBuilder<T, U>> and(Path<T, N> path);

        @Override
        <N extends Number & Comparable<N>> NumberOps<T, N, AggAndBuilder<T, U>> and(NumberPath<T, N> path);

        @Override
        <N extends Comparable<N>> ComparableOps<T, N, AggAndBuilder<T, U>> and(ComparablePath<T, N> path);

        @Override
        AggAndBuilder<T, U> and(BooleanPath<T> path);

        @Override
        StringOps<T, AggAndBuilder<T, U>> and(StringPath<T> path);

        @Override
        AggAndBuilder<T, U> and(Expression<T, Boolean> predicate);


    }

    interface AndBuilder<T, U> extends OrderBy0<T, U> {

        <N> PathOps<T, N, ? extends AndBuilder<T, U>> and(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOps<T, N, ? extends AndBuilder<T, U>> and(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOps<T, N, ? extends AndBuilder<T, U>> and(ComparablePath<T, N> path);

        StringOps<T, ? extends AndBuilder<T, U>> and(StringPath<T> path);

        AndBuilder<T, U> and(BooleanPath<T> path);

        AndBuilder<T, U> and(Expression<T, Boolean> predicate);

    }

    interface GroupBy<T, U> {
        OrderBy0<T, U> groupBy(List<? extends Expression<T, ?>> expressions);

        Having0<T, U> groupBy(Path<T, ?> path);

        default OrderBy0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1) {
            return groupBy(Metas.toExpressionList(p0, p1));
        }

        default OrderBy0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return groupBy(Metas.toExpressionList(p0, p1, p2));
        }

        default OrderBy0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return groupBy(Metas.toExpressionList(p0, p1, p2, p3));
        }

        default OrderBy0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return groupBy(Metas.toExpressionList(p0, p1, p2, p3, p4));
        }

        default OrderBy0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                       Path<T, ?> p5) {
            return groupBy(Metas.toExpressionList(p0, p1, p2, p3, p4, p5));
        }
    }

    interface Having<T, U> {

        OrderBy0<T, U> having(Expression<T, Boolean> predicate);

    }

    interface OrderBy<T, U> {

        Collector<U> orderBy(List<? extends Ordering<T>> path);

        default Collector<U> orderBy(Ordering<T> path) {
            return orderBy(List.of(path));
        }

        default Collector<U> orderBy(Ordering<T> p0, Ordering<T> p1) {
            return orderBy(List.of(p0, p1));
        }

        default Collector<U> orderBy(Ordering<T> p0, Ordering<T> p1, Ordering<T> p2) {
            return orderBy(List.of(p0, p1, p2));
        }

    }


    interface Collector<T> {

        int count();

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

        default List<T> getList(int offset) {
            return getList(offset, -1);
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

        default <R> R getResult(@NotNull Function<? super Collector<T>, R> function) {
            return function.apply(this);
        }

        default Slice<T> slice(int offset, int limit) {
            return slice(Sliceable.of(offset, limit));
        }

        default Slice<T> slice(Sliceable sliceable) {
            int count = count();
            if (count <= sliceable.offset()) {
                return Slice.of(List.of(), count, sliceable);
            } else {
                List<T> list = getList(sliceable.offset(), sliceable.limit());
                return Slice.of(list, count, sliceable);
            }
        }

        MetadataBuilder buildMetadata();

    }

    interface MetadataBuilder {

        QueryMetadata count();

        QueryMetadata getList(int offset, int maxResult, LockModeType lockModeType);

        QueryMetadata exist(int offset);

        default SliceMeta slice(Sliceable sliceable) {
            QueryMetadata count = count();
            QueryMetadata list = getList(sliceable.offset(), sliceable.limit(), LockModeType.NONE);
            return new SliceMeta(count, list);
        }

        default SliceMeta slice(int offset, int limit) {
            return slice(Sliceable.of(offset, limit));
        }

    }

    record SliceMeta(QueryMetadata count, QueryMetadata list) {
    }

}
