package io.github.genie.sql.core;

import io.github.genie.sql.core.Expression.TypedExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public interface Query {

    <T> Build<T, T> from(Class<T> type);

    static Query createQuery(QueryExecutor executor) {
        return new Query() {
            @Override
            public <T> Build<T, T> from(Class<T> type) {
                return Util.cast(new QueryBuilder<>(executor, type));
            }
        };
    }


    interface Build<T, U> extends
            Fetch<T>,
            Select<T>,
            Where<T, U>,
            GroupBy<T, U>,
            OrderBy<T, U>,
            Collector<U> {
    }


    interface Fetch<T> {

        <B extends Where<T, T>
                & GroupBy<T, T>
                & OrderBy<T, T>
                & Collector<T>>
        B fetch(List<OperateableExpression<T, ?>> path);


        default <B extends Where<T, T>
                & GroupBy<T, T>
                & OrderBy<T, T>
                & Collector<T>>
        B fetch(Path<T, ?> path) {
            return fetch(Stream.of(path)
                    .map(BasicExpressions::of)
                    .<OperateableExpression<T, ?>>map(x -> () -> x)
                    .toList());
        }


        default <B extends Where<T, T>
                & GroupBy<T, T>
                & OrderBy<T, T>
                & Collector<T>>
        B fetch(Path<T, ?> p0, Path<T, ?> p1) {
            return fetch(Stream.of(p0, p1)
                    .map(BasicExpressions::of)
                    .<OperateableExpression<T, ?>>map(x -> () -> x)
                    .toList());
        }

        default <B extends Where<T, T>
                & GroupBy<T, T>
                & OrderBy<T, T>
                & Collector<T>>
        B fetch(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p3) {
            return fetch(Stream.of(p0, p1, p3)
                    .map(BasicExpressions::of)
                    .<OperateableExpression<T, ?>>map(x -> () -> x)
                    .toList());
        }
    }

    interface Select<T> {

        <R, B extends Where<T, R> & OrderBy<T, R> & Collector<R>>
        B select(Class<R> projectionType);

        <B extends AggregatableWhere<T, Object[]>
                & GroupBy<T, Object[]>
                & OrderBy<T, Object[]>
                & Collector<Object[]>>
        B select(List<? extends TypedExpression<T, ?>> paths);

        <R, B extends AggregatableWhere<T, R> & GroupBy<T, R> & OrderBy<T, R> & Collector<R>>
        B select(Path<T, ? extends R> expression);

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1) {
            return select(BasicExpressions.list(p0, p1));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return select(BasicExpressions.list(p0, p1, p2));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return select(BasicExpressions.list(p0, p1, p2, p3));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return select(BasicExpressions.list(p0, p1, p2, p3, p4));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                 Path<T, ?> p5) {
            return select(BasicExpressions.list(p0, p1, p2, p3, p4, p5));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                 Path<T, ?> p5, Path<T, ?> p6) {
            return select(BasicExpressions.list(p0, p1, p2, p3, p4, p5, p6));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                 Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7) {
            return select(BasicExpressions.list(p0, p1, p2, p3, p4, p5, p6, p7));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                 Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8) {
            return select(BasicExpressions.list(p0, p1, p2, p3, p4, p5, p6, p7, p8));
        }

        default <B extends AggregatableWhere<T, Object[]> & GroupBy<T, Object[]> & OrderBy<T, Object[]> & Collector<Object[]>>
        B select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                 Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8, Path<T, ?> p9) {
            return select(BasicExpressions.list(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
        }


    }

    interface OrderByBuilder<T, U> extends OrderBy<T, U>, Collector<U> {

    }

    interface GroupByBuilder<T, U> extends OrderByBuilder<T, U>, GroupBy<T, U> {

    }

    interface AggregatableWhere<T, U> extends Where<T, U> {

        GroupByBuilder<T, U> where(TypedExpression<T, Boolean> predicate);

    }

    interface Where<T, U> {

        OrderByBuilder<T, U> where(TypedExpression<T, Boolean> predicate);

    }

    interface GroupBy<T, U> {
        <B extends OrderBy<T, U> & Collector<U>> B groupBy(List<OperateableExpression<T, ?>> expressions);

        <B extends OrderBy<T, U> & Having<T, U> & Collector<U>>
        B groupBy(Path<T, ?> path);

        default <B extends OrderBy<T, U> & Having<T, U> & Collector<U>>
        B groupBy(Path<T, ?> p0, Path<T, ?> p1) {
            return groupBy(BasicExpressions.list(p0, p1));
        }

        default <B extends OrderBy<T, U> & Having<T, U> & Collector<U>>
        B groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return groupBy(BasicExpressions.list(p0, p1, p2));
        }

        default <B extends OrderBy<T, U> & Having<T, U> & Collector<U>>
        B groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return groupBy(BasicExpressions.list(p0, p1, p2, p3));
        }

        default <B extends OrderBy<T, U> & Having<T, U> & Collector<U>>
        B groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return groupBy(BasicExpressions.list(p0, p1, p2, p3, p4));
        }

        default <B extends OrderBy<T, U> & Having<T, U> & Collector<U>>
        B groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                  Path<T, ?> p5) {
            return groupBy(BasicExpressions.list(p0, p1, p2, p3, p4, p5));
        }
    }

    interface Having<T, U> {

        <B extends OrderBy<T, U> & Collector<U>> B having(TypedExpression<T, Boolean> predicate);

    }

    interface OrderBy<T, U> {

        Collector<U> orderBy(List<? extends Ordering<T>> path);

        @SuppressWarnings("unchecked")
        default Collector<U> orderBy(Ordering<T>... orderings) {
            return orderBy(List.of(orderings));
        }

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
            return slice(Slice.sliceable(offset, limit));
        }

        default Slice<T> slice(Slice.Sliceable sliceable) {
            int count = count();
            if (count <= sliceable.offset()) {
                return new SliceImpl<>(List.of(), count, sliceable);
            } else {
                List<T> list = getList(sliceable.offset(), sliceable.size());
                return new SliceImpl<>(list, count, sliceable);
            }
        }

        Metadata metadata();

    }

    interface Metadata {

        QueryMetadata count();

        QueryMetadata getList(int offset, int maxResult, LockModeType lockModeType);

        QueryMetadata exist(int offset);

        default List<QueryMetadata> slice(Slice.Sliceable sliceable) {
            QueryMetadata count = count();
            QueryMetadata list = getList(sliceable.offset(), sliceable.size(), LockModeType.NONE);
            return List.of(count, list);
        }

        default List<QueryMetadata> slice(int offset, int limit) {
            return slice(Slice.sliceable(offset, limit));
        }

    }

}
