package io.github.genie.sql;

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
import io.github.genie.sql.builder.Tuples;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TuplesTest {
    A a = new A();
    B b = new B();
    C c = new C();
    D d = new D();
    E e = new E();
    F f = new F();
    G g = new G();
    H h = new H();
    I i = new I();
    J j = new J();

    @Test
    void of() {
        Date date = new Date();
        Object[] array = {1, "a", date};
        Tuple objects = Tuples.of(array);
        assertEquals(objects.size(), 3);
        assertEquals(objects.<Integer>get(0), 1);
        assertEquals(objects.<String>get(1), "a");
        assertEquals(objects.<Date>get(2), date);
        assertArrayEquals(array, objects.toArray());
        assertEquals(new ArrayList<>(Arrays.asList(array)), new ArrayList<>(objects.toList()));
    }

    @Test
    void testOf() {
        Tuple2<A, B> objects = Tuples.of(a, b);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
    }

    @Test
    void testOf1() {
        Tuple3<A, B, C> objects = Tuples.of(a, b, c);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
    }

    @Test
    void testOf2() {
        Tuple4<A, B, C, D> objects = Tuples.of(a, b, c, d);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
    }

    @Test
    void testOf3() {
        Tuple5<A, B, C, D, E> objects = Tuples.of(a, b, c, d, e);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
        assertEquals(objects.get4(), e);
    }

    @Test
    void testOf4() {
        Tuple6<A, B, C, D, E, F> objects = Tuples.of(a, b, c, d, e, f);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
        assertEquals(objects.get4(), e);
        assertEquals(objects.get5(), f);
    }

    @Test
    void testOf5() {
        Tuple7<A, B, C, D, E, F, G> objects = Tuples.of(a, b, c, d, e, f, g);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
        assertEquals(objects.get4(), e);
        assertEquals(objects.get5(), f);
        assertEquals(objects.get6(), g);
    }

    @Test
    void testOf6() {
        Tuple8<A, B, C, D, E, F, G, H> objects = Tuples.of(a, b, c, d, e, f, g, h);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
        assertEquals(objects.get4(), e);
        assertEquals(objects.get5(), f);
        assertEquals(objects.get6(), g);
        assertEquals(objects.get7(), h);
    }

    @Test
    void testOf7() {
        Tuple9<A, B, C, D, E, F, G, H, I> objects = Tuples.of(a, b, c, d, e, f, g, h, i);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
        assertEquals(objects.get4(), e);
        assertEquals(objects.get5(), f);
        assertEquals(objects.get6(), g);
        assertEquals(objects.get7(), h);
        assertEquals(objects.get8(), i);
    }

    @Test
    void testOf8() {
        Tuple10<A, B, C, D, E, F, G, H, I, J> objects = Tuples.of(a, b, c, d, e, f, g, h, i, j);
        assertEquals(objects.get0(), a);
        assertEquals(objects.get1(), b);
        assertEquals(objects.get2(), c);
        assertEquals(objects.get3(), d);
        assertEquals(objects.get4(), e);
        assertEquals(objects.get5(), f);
        assertEquals(objects.get6(), g);
        assertEquals(objects.get7(), h);
        assertEquals(objects.get8(), i);
        assertEquals(objects.get9(), j);
    }

    static class A {
    }

    static class B {
    }

    static class C {
    }

    static class D {
    }

    static class E {
    }

    static class F {
    }

    static class G {
    }

    static class H {
    }

    static class I {
    }

    static class J {
    }


}