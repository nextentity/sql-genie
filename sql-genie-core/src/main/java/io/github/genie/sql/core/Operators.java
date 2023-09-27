// package io.github.genie.sql.core;
//
// import io.github.genie.sql.core.ExpressionChainBuilder.*;
// import io.github.genie.sql.core.Path.ComparablePath;
// import io.github.genie.sql.core.Path.NumberPath;
//
// import java.util.ArrayList;
// import java.util.Iterator;
// import java.util.List;
//
// public class Operators<T, U, B> implements PathOperation<T, U, B>, CommonOperation<T, U, B> {
//
//     List<Expression.Meta> expressions = List.of();
//     Expression.Meta left = Metas.of(true);
//     Expression.Meta right = Metas.fromPaths(List.of());
//
//
//     public Operators() {
//     }
//
//     public Operators(List<Meta> expressions, Meta left, Meta right) {
//         this.expressions = expressions;
//         this.left = left;
//         this.right = right;
//     }
//
//     @Override
//     public Meta meta() {
//         Meta merge = merge();
//         if (expressions.isEmpty()) {
//             return merge;
//         }
//         Iterator<Meta> iterator = expressions.iterator();
//         Meta l = iterator.next();
//         List<Meta> r = new ArrayList<>(expressions.size());
//         while (iterator.hasNext()) {
//             r.add(iterator.next());
//         }
//         r.add(merge);
//         return Metas.operate(() -> l, Operator.OR, r);
//     }
//
//     Meta merge() {
//         return Metas.isTrue(left) ? right : Metas.operate(() -> left, Operator.AND, List.of(right));
//     }
//
//     @Override
//     public <V, R extends PathOperation<T, V, B> & CommonOperation<T, V, B>> R get(Path<U, V> path) {
//         // noinspection unchecked
//         return (R) new Operators<>(expressions, left, toPaths(path));
//     }
//
//     private Meta toPaths(Path<?, ?> path) {
//         if (this.right == null) {
//             this.right = Metas.fromPaths(List.of());
//         }
//         Meta basic = right;
//         if (basic instanceof Paths p) {
//             List<String> paths = Util.concat(p.paths(), Metas.asString(path));
//             return Metas.fromPaths(paths);
//         }
//         throw new IllegalStateException();
//     }
//
//     @Override
//     public StringOperation<T, B> get(Path.StringPath<T> path) {
//         return null;
//     }
//
//     @Override
//     public <V extends Number & Comparable<V>> NumberOperation<T, V, B> get(NumberPath<T, V> path) {
//         return null;
//     }
//
//     @Override
//     public <V extends Comparable<V>> ComparableOperation<T, V, B> get(ComparablePath<T, V> path) {
//         return null;
//     }
//
//     @Override
//     public BooleanOperation<T, B> get(Path.BooleanPath<T> path) {
//         return null;
//     }
//
//
// }
