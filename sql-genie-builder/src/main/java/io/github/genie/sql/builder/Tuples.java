package io.github.genie.sql.builder;

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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Tuples {
    public static Tuple of(Object[] data) {
        return new ImmutableTuple<>(data);
    }

    public static <A, B, C, D, E, F, G, H, I, J>
    Tuple10<A, B, C, D, E, F, G, H, I, J> of(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d, e, f, g, h, i, j});
    }

    public static <A, B, C, D, E, F, G, H, I>
    Tuple9<A, B, C, D, E, F, G, H, I> of(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d, e, f, g, h, i});
    }

    public static <A, B, C, D, E, F, G, H>
    Tuple8<A, B, C, D, E, F, G, H> of(A a, B b, C c, D d, E e, F f, G g, H h) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d, e, f, g, h});
    }

    public static <A, B, C, D, E, F, G>
    Tuple7<A, B, C, D, E, F, G> of(A a, B b, C c, D d, E e, F f, G g) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d, e, f, g});
    }

    public static <A, B, C, D, E, F>
    Tuple6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d, e, f});
    }

    public static <A, B, C, D, E>
    Tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d, e});
    }

    public static <A, B, C, D>
    Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
        return new ImmutableTuple<>(new Object[]{a, b, c, d});
    }

    public static <A, B, C>
    Tuple3<A, B, C> of(A a, B b, C c) {
        return new ImmutableTuple<>(new Object[]{a, b, c});
    }

    public static <A, B>
    Tuple2<A, B> of(A a, B b) {
        return new ImmutableTuple<>(new Object[]{a, b});
    }

    public static final class ImmutableTuple<A, B, C, D, E, F, G, H, I, J>
            implements Tuple10<A, B, C, D, E, F, G, H, I, J> {
        private final Object[] data;

        ImmutableTuple(Object[] data) {
            this.data = data;
        }

        @Override
        public <T> T get(int index) {
            return TypeCastUtil.unsafeCast(data[index]);
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public List<Object> toList() {
            ArrayList<Object> list = new ArrayList<>(data.length);
            forEach(list::add);
            return list;
        }

        @Override
        public Object[] toArray() {
            return Arrays.copyOf(data, data.length);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImmutableTuple<?, ?, ?, ?, ?, ?, ?, ?, ?, ?> tuple = (ImmutableTuple<?, ?, ?, ?, ?, ?, ?, ?, ?, ?>) o;
            return Arrays.equals(data, tuple.data);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }

        @NotNull
        @Override
        public Iterator<Object> iterator() {
            return new ArrayIterator<>(data);
        }

    }
}
