package io.github.genie.sql.api;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Lists {

    @SafeVarargs
    static <E> List<E> of(E... es) {
        return Arrays.asList(es);
    }

    static <E> List<E> of(E e) {
        return Collections.singletonList(e);
    }

    static <E> List<E> of() {
        return Collections.emptyList();
    }

    static <T> Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        final Iterator<T> iterator = getIterator(seed, hasNext, next);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    @NotNull
    static <T> Iterator<T> getIterator(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        Object NONE = new Object();
        return new Iterator<T>() {
            @SuppressWarnings("unchecked")
            T t = (T) NONE;

            @Override
            public boolean hasNext() {
                return t == NONE || hasNext.test(t);
            }

            @Override
            public T next() {
                T result = (t == NONE) ? seed : t;
                t = next.apply(result);
                return result;
            }
        };
    }

    static <T> List<T> concat(Collection<? extends T> collection, Collection<? extends T> value) {
        return Stream.concat(collection.stream(), value.stream())
                .collect(Collectors.toList());
    }

}
