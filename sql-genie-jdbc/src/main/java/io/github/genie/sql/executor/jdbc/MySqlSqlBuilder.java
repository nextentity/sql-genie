package io.github.genie.sql.executor.jdbc;

import io.github.genie.sql.api.Constant;
import io.github.genie.sql.api.Expression;
import io.github.genie.sql.api.Operation;
import io.github.genie.sql.api.Column;
import io.github.genie.sql.builder.Expressions;
import io.github.genie.sql.api.LockModeType;
import io.github.genie.sql.api.Operator;
import io.github.genie.sql.api.Order;
import io.github.genie.sql.api.Order.SortOrder;
import io.github.genie.sql.api.QueryStructure;
import io.github.genie.sql.api.Selection;
import io.github.genie.sql.api.Selection.MultiColumn;
import io.github.genie.sql.api.Selection.SingleColumn;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor.PreparedSql;
import io.github.genie.sql.executor.jdbc.JdbcQueryExecutor.QuerySqlBuilder;
import io.github.genie.sql.builder.meta.AnyToOneAttribute;
import io.github.genie.sql.builder.meta.Attribute;
import io.github.genie.sql.builder.meta.BasicAttribute;
import io.github.genie.sql.builder.meta.EntityType;
import io.github.genie.sql.builder.meta.Metamodel;
import io.github.genie.sql.builder.meta.Projection;
import io.github.genie.sql.builder.meta.ProjectionAttribute;
import io.github.genie.sql.builder.meta.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MySqlSqlBuilder implements QuerySqlBuilder {
    @Override
    public PreparedSql build(QueryStructure metadata, Metamodel mappings) {
        return new Builder(metadata, metadata.from(), mappings).build();
    }


    static class Builder {


        public static final String NONE_DELIMITER = "";
        public static final String DELIMITER = ",";
        public static final String FOR_SHARE = " for share";
        public static final String FOR_UPDATE = " for update";
        public static final String FOR_UPDATE_NOWAIT = " for update nowait";
        public static final String SELECT = "select ";
        public static final String FROM = "from ";
        public static final String WHERE = " where ";
        public static final String HAVING = " having ";
        public static final String ORDER_BY = " order by ";
        public static final String DESC = "desc";
        public static final String ASC = "asc";
        public static final String ON = " on ";

        protected final StringBuilder sql = new StringBuilder();
        protected final List<Object> args = new ArrayList<>();
        protected final Map<Column, Integer> joins = new LinkedHashMap<>();
        protected final QueryStructure queryMetadata;
        protected final EntityType entityType;
        protected final Metamodel mappers;
        protected final List<Expression> selectMetas = new ArrayList<>();
        protected final List<Attribute> selectFields = new ArrayList<>();


        public Builder(QueryStructure queryMetadata, Class<?> type, Metamodel mappers) {
            this.queryMetadata = queryMetadata;
            this.mappers = mappers;
            this.entityType = mappers.getEntity(type);
        }

        protected PreparedSql build() {
            buildProjectionPaths();
            sql.append(SELECT);
            appendSelects();
            appendFetchPath();
            appendTableName();
            int sqlIndex = sql.length();
            appendWhere();
            appendGroupBy();
            appendOrderBy();
            appendHaving();
            appendOffsetAndLimit();
            insertJoin(sqlIndex);
            appendLockModeType(queryMetadata.lockType());
            return new PreparedSqlImpl(sql.toString(), args, selectFields);
        }

        private void buildProjectionPaths() {
            Selection selected = queryMetadata.select();
            if (selected instanceof SingleColumn singleColumn) {
                selectMetas.add(singleColumn.column());
            } else if (selected instanceof MultiColumn multiColumn) {
                selectMetas.addAll(multiColumn.columns());
            } else if (queryMetadata.select().resultType() == queryMetadata.from()) {
                EntityType table = mappers
                        .getEntity(queryMetadata.select().resultType());
                for (Attribute mapping : table.fields()) {
                    if (!(mapping instanceof BasicAttribute column)) {
                        continue;
                    }
                    Column columns = Expressions.ofPath(column.name());
                    selectMetas.add(columns);
                    selectFields.add(mapping);
                }
            } else {
                Projection projectionMapping = mappers
                        .getProjection(queryMetadata.from(), queryMetadata.select().resultType());
                for (ProjectionAttribute mapping : projectionMapping.attributes()) {
                    if (!(mapping.baseField() instanceof BasicAttribute column)) {
                        continue;
                    }

                    Column columns = Expressions.ofPath(column.name());
                    selectMetas.add(columns);
                    selectFields.add(mapping.field());
                }
            }
        }

        private static int unwrap(Integer offset) {
            return offset == null ? -1 : offset;
        }

        private void appendSelects() {
            String join = NONE_DELIMITER;
            for (Expression meta : selectMetas) {
                sql.append(join);
                appendExpression(meta);
                join = DELIMITER;
            }
        }

        protected void appendFetchPath() {
            List<? extends Column> fetchClause = queryMetadata.fetch();
            if (fetchClause != null) {
                for (Column fetch : fetchClause) {
                    Attribute attribute = getAttribute(fetch);
                    if (!(attribute instanceof AnyToOneAttribute am)) {
                        continue;
                    }
                    EntityType entityTypeInfo = am.referenced();
                    for (Attribute field : entityTypeInfo.fields()) {
                        if (!(field instanceof BasicAttribute mapping)) {
                            continue;
                        }
                        sql.append(",");
                        Column column = Expressions.concat(fetch, mapping.name());
                        appendPaths(column);
                        selectMetas.add(column);
                        selectFields.add(field);
                    }
                }
            }
        }


        protected void appendLockModeType(LockModeType lockModeType) {
            if (lockModeType == LockModeType.PESSIMISTIC_READ) {
                sql.append(FOR_SHARE);
            } else if (lockModeType == LockModeType.PESSIMISTIC_WRITE) {
                sql.append(FOR_UPDATE);
            } else if (lockModeType == LockModeType.PESSIMISTIC_FORCE_INCREMENT) {
                sql.append(FOR_UPDATE_NOWAIT);
            }
        }

        private void appendTableName() {
            appendBlank()
                    .append(FROM + "`")
                    .append(entityType.tableName())
                    .append("` ");
            appendRootTableAlias();
        }

        protected StringBuilder appendRootTableAlias() {
            return appendRootTableAlias(sql);
        }

        protected StringBuilder appendRootTableAlias(StringBuilder sql) {
            String table = entityType.tableName();
            return sql.append(table, 0, 1);
        }

        protected StringBuilder appendTableAlias(String table, Object index, StringBuilder sql) {
            return appendBlank(sql).append(table, 0, 1).append(index);
        }

        protected StringBuilder appendBlank() {
            return appendBlank(sql);
        }

        protected StringBuilder appendBlank(StringBuilder sql) {
            return sql.isEmpty() || " (,+-*/=><".indexOf(sql.charAt(sql.length() - 1)) >= 0 ? sql : sql.append(' ');
        }


        protected void appendWhere() {
            Expression where = queryMetadata.where();
            if (where == null || Expressions.isTrue(where)) {
                return;
            }
            sql.append(WHERE);
            appendExpression(where);
        }

        protected void appendHaving() {
            Expression having = queryMetadata.having();
            if (having == null || Expressions.isTrue(having)) {
                return;
            }
            sql.append(HAVING);
            appendExpression(having);
        }

        protected void appendExpression(Expression expr) {
            appendExpression(args, expr);
        }


        protected void appendExpression(List<Object> args, Expression meta) {
            if (meta instanceof Constant constant) {
                appendConstant(args, constant);
            } else if (meta instanceof Column column) {
                appendPaths(column);
            } else if (meta instanceof Operation operation) {
                appendOperation(args, operation);
            } else {
                throw new UnsupportedOperationException("unknown type " + meta.getClass());
            }
        }

        private void appendConstant(List<Object> args, Constant constant) {
            Object value = constant.value();
            if (value instanceof Boolean b) {
                appendBlank().append(b ? 1 : 0);
            } else {
                appendBlank().append('?');
                args.add(value);
            }
        }

        private void appendOperation(List<Object> args, Operation operation) {
            Operator operator = operation.operator();
            Expression leftOperand = operation.operand();
            Operator operator0 = getOperator(leftOperand);
            List<? extends Expression> rightOperand = operation.args();
            switch (operator) {
                case NOT -> {
                    appendOperator(operator);
                    sql.append(' ');
                    if (operator0 != null && operator0.priority() > operator.priority()) {
                        sql.append('(');
                        appendExpression(args, leftOperand);
                        sql.append(')');
                    } else {
                        appendExpression(args, leftOperand);
                    }
                }
                case AND, OR, LIKE, MOD, GT, EQ, NE, GE, LT,
                        LE, ADD, SUBTRACT, MULTIPLY, DIVIDE -> {
                    appendBlank();
                    if (operator0 != null && operator0.priority() > operator.priority()) {
                        sql.append('(');
                        appendExpression(args, leftOperand);
                        sql.append(')');
                    } else {
                        appendExpression(args, leftOperand);
                    }
                    for (Expression value : rightOperand) {
                        appendOperator(operator);
                        Operator operator1 = getOperator(value);
                        if (operator1 != null && operator1.priority() >= operator.priority()) {
                            sql.append('(');
                            appendExpression(args, value);
                            sql.append(')');
                        } else {
                            appendExpression(args, value);
                        }
                    }
                }
                case LOWER, UPPER, SUBSTRING, TRIM, LENGTH,
                        NULLIF, IF_NULL, MIN, MAX, COUNT, AVG, SUM -> {
                    appendOperator(operator);
                    sql.append('(');
                    appendExpression(args, leftOperand);
                    for (Expression expression : rightOperand) {
                        sql.append(',');
                        appendExpression(args, expression);
                    }
                    sql.append(")");
                }
                case IN -> {
                    if (rightOperand.isEmpty()) {
                        appendBlank().append(0);
                    } else {
                        appendBlank();
                        appendExpression(leftOperand);
                        appendOperator(operator);
                        char join = '(';
                        for (Expression expression : rightOperand) {
                            sql.append(join);
                            appendExpression(args, expression);
                            join = ',';
                        }
                        sql.append(")");
                    }
                }
                case BETWEEN -> {
                    appendBlank();
                    appendExpression(args, leftOperand);
                    appendOperator(operator);
                    appendBlank();
                    Expression operate = Expressions
                            .operate(rightOperand.get(0), Operator.AND, List.of(rightOperand.get(1)));
                    appendExpression(args, operate);
                }
                case IS_NULL, IS_NOT_NULL -> {
                    appendBlank();
                    if (operator0 != null && operator0.priority()
                                             > operator.priority()) {
                        sql.append('(');
                        appendExpression(args, leftOperand);
                        sql.append(')');
                    } else {
                        appendExpression(args, leftOperand);
                    }
                    appendBlank();
                    appendOperator(operator);
                }
                default -> throw new UnsupportedOperationException("unknown operator " + operator);
            }
        }

        private void appendOperator(Operator jdbcOperator) {
            String sign = jdbcOperator.sign();
            if (Character.isLetter(sign.charAt(0))) {
                appendBlank();
            }
            sql.append(sign);
        }


        protected void appendPaths(Column column) {
            appendBlank();
            List<String> expression = column.paths();
            StringBuilder sb = sql;
            int iMax = expression.size() - 1;
            if (iMax == -1)
                return;
            int i = 0;
            if (expression.size() == 1) {
                appendRootTableAlias().append(".");
            }
            Class<?> type = entityType.javaType();

            Column join = Expressions.ofPaths(List.of(expression.get(0)));

            for (String path : expression) {
                EntityType info = mappers.getEntity(type);
                Attribute attribute = info.getAttribute(path);
                if (i++ == iMax) {
                    if (attribute instanceof AnyToOneAttribute joinColumnMapper) {
                        sb.append(joinColumnMapper.joinColumnName());
                    } else if (attribute instanceof BasicAttribute basicColumnMapper) {
                        sb.append(basicColumnMapper.columnName());
                    } else {
                        throw new IllegalStateException();
                    }
                    return;
                } else {
                    joins.putIfAbsent(join, joins.size());
                    if (i == iMax) {
                        Integer index = joins.get(join);
                        appendTableAttribute(sb, attribute, index).append('.');
                    }
                }
                type = attribute.javaType();
                join = Expressions.concat(join, path);
            }
        }

        protected void insertJoin(int sqlIndex) {
            StringBuilder sql = new StringBuilder();

            joins.forEach((k, v) -> {
                Attribute attribute = getAttribute(k);
                EntityType entityTypeInfo = mappers.getEntity(attribute.javaType());
                sql.append(" left join `").append(entityTypeInfo.tableName()).append("`");

                appendTableAttribute(sql, attribute, v);
                sql.append(ON);
                Column parent = getParent(k);
                if (parent == null) {
                    appendRootTableAlias(sql);
                } else {
                    Integer parentIndex = joins.get(parent);
                    Attribute parentAttribute = getAttribute(parent);
                    appendTableAttribute(sql, parentAttribute, parentIndex);
                }
                if (attribute instanceof AnyToOneAttribute join) {
                    sql.append(".").append(join.joinColumnName()).append("=");
                    appendTableAttribute(sql, attribute, v);
                    String referenced = join.referencedColumnName();
                    if (referenced.isEmpty()) {
                        referenced = ((BasicAttribute) entityTypeInfo.id()).columnName();
                    }
                    sql.append(".").append(referenced);
                } else {
                    throw new IllegalStateException();
                }
            });
            this.sql.insert(sqlIndex, sql);

        }

        private static Column getParent(Column k) {
            if (k == null || k.paths().size() <= 1) {
                return null;
            }
            List<String> paths = new ArrayList<>(k.paths());
            paths.remove(paths.size() - 1);
            return Expressions.ofPaths(paths);
        }

        Operator getOperator(Expression e) {
            return e instanceof Operation expression ? expression.operator() : null;
        }

        protected StringBuilder appendTableAttribute(StringBuilder sb, Attribute attribute, Integer index) {
            EntityType information = mappers.getEntity(attribute.javaType());
            String tableName = information.tableName();
            return appendTableAlias(tableName, index, sb);
        }

        protected Attribute getAttribute(Column path) {
            Type schema = entityType;
            for (String s : path.paths()) {
                if (schema instanceof AnyToOneAttribute associationProperty) {
                    schema = associationProperty.referenced();
                }
                if (schema instanceof EntityType ts) {
                    schema = ts.getAttribute(s);
                } else {
                    throw new IllegalStateException();
                }
            }
            return (Attribute) schema;
        }

        protected void appendOffsetAndLimit() {
            int offset = unwrap(queryMetadata.offset());
            int limit = unwrap(queryMetadata.limit());
            if (offset >= 0 || limit >= 0) {
                sql.append(" limit ")
                        .append(Math.max(offset, 0))
                        .append(',')
                        .append(limit < 0 ? Long.MAX_VALUE : limit);
            }
        }

        private void appendGroupBy() {
            List<? extends Expression> groupBy = queryMetadata.groupBy();
            if (groupBy != null && !groupBy.isEmpty()) {
                sql.append(" group by ");
                boolean first = true;
                for (Expression e : groupBy) {
                    if (first) {
                        first = false;
                    } else {
                        sql.append(",");
                    }
                    appendExpression(e);
                }
            }
        }

        protected void appendOrderBy() {
            List<? extends Order<?>> orders = queryMetadata.orderBy();
            if (orders != null && !orders.isEmpty()) {
                sql.append(ORDER_BY);
                boolean first = true;
                for (Order<?> order : orders) {
                    if (first) {
                        first = false;
                    } else {
                        sql.append(",");
                    }
                    appendExpression(order.expression());
                    sql.append(" ").append(order.order() == SortOrder.DESC ? DESC : ASC);
                }

            }
        }
    }

    public record PreparedSqlImpl(String sql, List<?> args, List<Attribute> selected) implements PreparedSql {
    }
}
