package io.github.genie.sql.api;

import java.util.List;

public interface ExpressionBuilder<T> {
    List<? extends ExpressionHolder<T, ?>> apply(Root<T> root);
}
