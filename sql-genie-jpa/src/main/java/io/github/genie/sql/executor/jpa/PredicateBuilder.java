package io.github.genie.sql.executor.jpa;


import io.github.genie.sql.api.Expression;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class PredicateBuilder extends ExpressionBuilder {

    public PredicateBuilder(Root<?> root, CriteriaBuilder cb) {
        super(root, cb);
    }

    public Predicate toPredicate(Expression expression) {
        javax.persistence.criteria.Expression<?> result = toExpression(expression);
        if (result instanceof Predicate) {
            return (Predicate) result;
        }
        return cb.isTrue(cast(toExpression(expression)));
    }


}