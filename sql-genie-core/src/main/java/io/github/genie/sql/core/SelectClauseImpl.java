package io.github.genie.sql.core;

import java.util.List;
import java.util.stream.Collectors;

public class SelectClauseImpl implements SelectClause {

    Class<?> resultType;

    public SelectClauseImpl(Class<?> resultType) {
        this.resultType = resultType;
    }

    @Override
    public Class<?> resultType() {
        return resultType;
    }


    @Override
    public String toString() {
        return resultType.getName();
    }

    static class MultiColumnSelect implements MultiColumn {
        List<? extends Expression> columns;

        public MultiColumnSelect(List<? extends Expression> columns) {
            this.columns = columns;
        }

        @Override
        public List<? extends Expression> columns() {
            return columns;
        }

        @Override
        public String toString() {
            return columns.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

    }

    static class SingleColumnSelect implements SingleColumn {
        Class<?> resultType;

        Expression column;

        public SingleColumnSelect(Class<?> resultType, Expression column) {
            this.resultType = resultType;
            this.column = column;
        }

        @Override
        public Expression column() {
            return column;
        }

        @Override
        public Class<?> resultType() {
            return resultType;
        }

        @Override
        public String toString() {
            return String.valueOf(column);
        }
    }


}
