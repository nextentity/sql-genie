package io.github.genie.sql.api;

import io.github.genie.sql.api.ExpressionOperator.ComparableOperator;
import io.github.genie.sql.api.ExpressionOperator.NumberOperator;
import io.github.genie.sql.api.ExpressionOperator.PathOperator;
import io.github.genie.sql.api.ExpressionOperator.Predicate;
import io.github.genie.sql.api.ExpressionOperator.StringOperator;
import io.github.genie.sql.api.Path.BooleanPath;
import io.github.genie.sql.api.Path.ComparablePath;
import io.github.genie.sql.api.Path.NumberPath;
import io.github.genie.sql.api.Path.StringPath;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface Query {

    <T> Select0<T, T> from(Class<T> type);

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

        GroupBy0<T, T> fetch(List<PathOperator<T, ?, Predicate<T>>> expressions);

        GroupBy0<T, T> fetch(Collection<Path<T, ?>> paths);

        default GroupBy0<T, T> fetch(Path<T, ?> path) {
            return fetch(List.of(path));
        }


        default GroupBy0<T, T> fetch(Path<T, ?> p0, Path<T, ?> p1) {
            return fetch(List.of(p0, p1));
        }

        default GroupBy0<T, T> fetch(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p3) {
            return fetch(List.of(p0, p1, p3));
        }

    }

    interface Select<T> {

        <R> Where0<T, R> select(Class<R> projectionType);

        AggWhere0<T, Object[]> select(List<? extends ExpressionBuilder<T, ?>> paths);

        <R> AggWhere0<T, R> select(Path<T, ? extends R> expression);

        AggWhere0<T, Object[]> select(Collection<Path<T, ?>> paths);

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1) {
            return select(List.of(p0, p1));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return select(List.of(p0, p1, p2));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return select(List.of(p0, p1, p2, p3));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return select(List.of(p0, p1, p2, p3, p4));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5) {
            return select(List.of(p0, p1, p2, p3, p4, p5));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6) {
            return select(List.of(p0, p1, p2, p3, p4, p5, p6));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7) {
            return select(List.of(p0, p1, p2, p3, p4, p5, p6, p7));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8) {
            return select(List.of(p0, p1, p2, p3, p4, p5, p6, p7, p8));
        }

        default AggWhere0<T, Object[]> select(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                              Path<T, ?> p5, Path<T, ?> p6, Path<T, ?> p7, Path<T, ?> p8, Path<T, ?> p9) {
            return select(List.of(p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
        }


    }


    interface AggWhere<T, U> extends Where<T, U> {

        AggGroupBy0<T, U> where(ExpressionBuilder<T, Boolean> predicate);

        @Override
        <N> PathOperator<T, N, AggAndBuilder<T, U>> where(Path<T, N> path);

        @Override
        <N extends Comparable<N>> ComparableOperator<T, N, AggAndBuilder<T, U>> where(ComparablePath<T, N> path);

        @Override
        <N extends Number & Comparable<N>> NumberOperator<T, N, AggAndBuilder<T, U>> where(NumberPath<T, N> path);

        @Override
        StringOperator<T, AggAndBuilder<T, U>> where(StringPath<T> path);

        @Override
        AggAndBuilder<T, U> where(BooleanPath<T> path);

    }

    interface Where<T, U> {

        OrderBy0<T, U> where(ExpressionBuilder<T, Boolean> predicate);

        <N> PathOperator<T, N, ? extends AndBuilder<T, U>> where(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, ? extends AndBuilder<T, U>> where(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, ? extends AndBuilder<T, U>> where(ComparablePath<T, N> path);

        StringOperator<T, ? extends AndBuilder<T, U>> where(StringPath<T> path);

        AndBuilder<T, U> where(BooleanPath<T> path);

    }

    interface AggAndBuilder<T, U> extends AndBuilder<T, U>, AggGroupBy0<T, U> {
        @Override
        <N> PathOperator<T, N, AggAndBuilder<T, U>> and(Path<T, N> path);

        @Override
        <N extends Number & Comparable<N>> NumberOperator<T, N, AggAndBuilder<T, U>> and(NumberPath<T, N> path);

        @Override
        <N extends Comparable<N>> ComparableOperator<T, N, AggAndBuilder<T, U>> and(ComparablePath<T, N> path);

        @Override
        AggAndBuilder<T, U> and(BooleanPath<T> path);

        @Override
        StringOperator<T, AggAndBuilder<T, U>> and(StringPath<T> path);

        @Override
        AggAndBuilder<T, U> and(ExpressionBuilder<T, Boolean> predicate);


    }

    interface AndBuilder<T, U> extends OrderBy0<T, U> {

        <N> PathOperator<T, N, ? extends AndBuilder<T, U>> and(Path<T, N> path);

        <N extends Number & Comparable<N>> NumberOperator<T, N, ? extends AndBuilder<T, U>> and(NumberPath<T, N> path);

        <N extends Comparable<N>> ComparableOperator<T, N, ? extends AndBuilder<T, U>> and(ComparablePath<T, N> path);

        StringOperator<T, ? extends AndBuilder<T, U>> and(StringPath<T> path);

        AndBuilder<T, U> and(BooleanPath<T> path);

        AndBuilder<T, U> and(ExpressionBuilder<T, Boolean> predicate);

    }

    interface GroupBy<T, U> {
        Having0<T, U> groupBy(List<? extends ExpressionBuilder<T, ?>> expressions);

        Having0<T, U> groupBy(Path<T, ?> path);

        Having0<T, U> groupBy(Collection<Path<T, ?>> paths);

        default Having0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1) {
            return groupBy(List.of(p0, p1));
        }

        default Having0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2) {
            return groupBy(List.of(p0, p1, p2));
        }

        default Having0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3) {
            return groupBy(List.of(p0, p1, p2, p3));
        }

        default Having0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4) {
            return groupBy(List.of(p0, p1, p2, p3, p4));
        }

        default Having0<T, U> groupBy(Path<T, ?> p0, Path<T, ?> p1, Path<T, ?> p2, Path<T, ?> p3, Path<T, ?> p4,
                                       Path<T, ?> p5) {
            return groupBy(List.of(p0, p1, p2, p3, p4, p5));
        }
    }

    interface Having<T, U> {

        OrderBy0<T, U> having(ExpressionBuilder<T, Boolean> predicate);

    }

    interface OrderBy<T, U> {

        Collector<U> orderBy(List<? extends Order<T>> path);

        default Collector<U> orderBy(Order<T> path) {
            return orderBy(List.of(path));
        }

        default Collector<U> orderBy(Order<T> p0, Order<T> p1) {
            return orderBy(List.of(p0, p1));
        }

        default Collector<U> orderBy(Order<T> p0, Order<T> p1, Order<T> p2) {
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
            return list.isEmpty() ? null : list.getFirst();
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
            return list.isEmpty() ? null : list.getFirst();
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
            return list.isEmpty() ? null : list.getFirst();
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
            return list.isEmpty() ? null : list.getFirst();
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

        Slice<T> slice(int offset, int limit);

        Slice<T> slice(Sliceable sliceable);

        QueryStructureBuilder buildMetadata();

    }

    interface QueryStructureBuilder {

        QueryStructure count();

        QueryStructure getList(int offset, int maxResult, LockModeType lockModeType);

        QueryStructure exist(int offset);

        default SliceQueryStructure slice(Sliceable sliceable) {
            QueryStructure count = count();
            QueryStructure list = getList(sliceable.offset(), sliceable.limit(), LockModeType.NONE);
            return new SliceQueryStructure(count, list);
        }

        SliceQueryStructure slice(int offset, int limit);

    }

    record SliceQueryStructure(QueryStructure count, QueryStructure list) {
    }

}
