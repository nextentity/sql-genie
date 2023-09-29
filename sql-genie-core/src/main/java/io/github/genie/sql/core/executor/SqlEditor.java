package io.github.genie.sql.core.executor;


import io.github.genie.sql.core.Expression.Constant;
import io.github.genie.sql.core.Expression.Meta;
import io.github.genie.sql.core.Expression.Operation;
import io.github.genie.sql.core.Expression.Paths;
import io.github.genie.sql.core.*;
import io.github.genie.sql.core.Ordering.SortOrder;
import io.github.genie.sql.core.SelectClause.MultiColumn;
import io.github.genie.sql.core.SelectClause.SingleColumn;
import io.github.genie.sql.core.executor.JdbcQueryExecutor.ColumnProjection;
import io.github.genie.sql.core.mapping.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.github.genie.sql.core.Ordering.SortOrder.DESC;

class SqlEditor {


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
    protected final Map<Paths, Integer> joins = new LinkedHashMap<>();
    protected final QueryMetadata queryMetadata;
    protected final TableMapping tableMapping;
    protected final MappingFactory mappers;

    public SqlEditor(QueryMetadata queryMetadata, Class<?> type, MappingFactory mappers) {
        this.queryMetadata = queryMetadata;
        this.mappers = mappers;
        this.tableMapping = mappers.getMapping(type);
    }

    protected JdbcQueryExecutor.PreparedSql build() {
        List<ColumnProjection> projectionPaths = buildProjectionPaths();
        sql.append(SELECT);
        appendSelects(projectionPaths);
        appendFetchPath(projectionPaths);
        appendTableName();
        int sqlIndex = sql.length();
        appendWhere();
        appendGroupBy();
        appendOrderBy();
        appendHaving();
        appendOffsetAndLimit();
        insertJoin(sqlIndex);
        appendLockModeType(queryMetadata.lockModeType());
        return new PreparedSqlImpl(sql.toString(), args, projectionPaths);
    }

    private List<ColumnProjection> buildProjectionPaths() {
        List<ColumnProjection> projectionPaths;
        SelectClause selected = queryMetadata.selectClause();
        if (selected instanceof SingleColumn singleColumn) {
            projectionPaths = List.of(new ColumnProjection(singleColumn.column(), null));
        } else if (selected instanceof MultiColumn multiColumn) {
            projectionPaths = multiColumn.columns().stream()
                    .map(expression -> new ColumnProjection(expression, null))
                    .toList();
        } else {
            projectionPaths = getProjectionSelects();
        }
        return projectionPaths;
    }

    private static int unwrap(Integer offset) {
        return offset == null ? -1 : offset;
    }

    protected List<ColumnProjection> getProjectionSelects() {
        List<ColumnProjection> columns = new ArrayList<>();
        TableMapping projectionMapping = mappers.getMapping(queryMetadata.selectClause().resultType());
        for (FieldMapping mapping : projectionMapping.fields()) {
            if (!(mapping instanceof ColumnMapping column)) {
                continue;
            }
            if (tableMapping.getFieldMapping(column.fieldName()) == null) {
                continue;
            }
            Paths paths = Expressions.ofPath(column.fieldName());
            columns.add(new ColumnProjection(paths, mapping));
        }
        return columns;
    }

    private void appendSelects(List<ColumnProjection> baseColumns) {
        String join = NONE_DELIMITER;
        for (ColumnProjection projection : baseColumns) {
            sql.append(join);
            appendExpression(projection.expression());
            join = DELIMITER;
        }
    }

    protected void appendFetchPath(List<ColumnProjection> selectedPath) {
        List<? extends Paths> fetchClause = queryMetadata.fetchPaths();
        if (fetchClause != null) {
            for (Paths fetch : fetchClause) {
                FieldMapping attribute = getAttribute(fetch);
                if (!(attribute instanceof AssociationMapping am)) {
                    continue;
                }
                TableMapping entityInfo = am.referenced();
                for (FieldMapping field : entityInfo.fields()) {
                    if (!(field instanceof ColumnMapping cm)) {
                        continue;
                    }
                    sql.append(",");
                    Paths path =
                            Expressions.concat(fetch, cm.fieldName());
                    appendPath(path);
                    selectedPath.add(new ColumnProjection(path, field));
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
                .append(tableMapping.tableName())
                .append("` ");
        appendRootTableAlias();
    }

    protected StringBuilder appendRootTableAlias() {
        return appendRootTableAlias(sql);
    }

    protected StringBuilder appendRootTableAlias(StringBuilder sql) {
        String table = tableMapping.tableName();
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
        Meta where = queryMetadata.whereClause();
        if (where == null || Expressions.isTrue(where)) {
            return;
        }
        sql.append(WHERE);
        appendExpression(where);
    }

    protected void appendHaving() {
        Meta having = queryMetadata.havingClause();
        if (having == null || Expressions.isTrue(having)) {
            return;
        }
        sql.append(HAVING);
        appendExpression(having);
    }

    protected void appendExpression(Meta expr) {
        appendExpressions(args, expr);
    }


    protected void appendExpressions(List<Object> args, Meta e) {
        if (e instanceof Constant ce) {
            Object value = ce.value();
            boolean isNumber = false;
            if (value != null) {
                Class<?> valueType = value.getClass();
                if (valueType.isPrimitive() || Number.class.isAssignableFrom(valueType)) {
                    isNumber = true;
                }
            }
            if (isNumber) {
                appendBlank().append(value);
            } else {
                appendBlank().append('?');
                args.add(value);
            }
        } else if (e instanceof Paths pe) {
            appendBlank();
            appendPath(pe);
        } else if (e instanceof Operation oe) {
            Operator operator = oe.operator();
            Meta leftOperand = oe.leftOperand();
            Operator operator0 = getOperator(leftOperand);
            List<? extends Meta> rightOperand = oe.rightOperand();
            switch (operator) {
                case NOT -> {
                    appendOperator(operator);
                    sql.append(' ');
                    if (operator0 != null && operator0.priority()
                                             > operator.priority()) {
                        sql.append('(');
                        appendExpressions(args, leftOperand);
                        sql.append(')');
                    } else {
                        appendExpressions(args, leftOperand);
                    }
                }
                case AND, OR, LIKE, MOD, GT, EQ, NE, GE, LT,
                        LE, ADD, SUBTRACT, MULTIPLY, DIVIDE -> {
                    appendBlank();
                    if (operator0 != null && operator0.priority()
                                             > operator.priority()) {
                        sql.append('(');
                        appendExpressions(args, leftOperand);
                        sql.append(')');
                    } else {
                        appendExpressions(args, leftOperand);
                    }
                    for (Meta value : rightOperand) {
                        appendOperator(operator);
                        Operator operator1 = getOperator(value);
                        if (operator1 != null && operator1.priority()
                                                 >= operator.priority()) {
                            sql.append('(');
                            appendExpressions(args, value);
                            sql.append(')');
                        } else {
                            appendExpressions(args, value);
                        }
                    }
                }
                case LOWER, UPPER, SUBSTRING, TRIM, LENGTH,
                        NULLIF, IF_NULL, MIN, MAX, COUNT, AVG, SUM -> {
                    appendOperator(operator);
                    sql.append('(');
                    appendExpressions(args, leftOperand);
                    for (Meta expression : rightOperand) {
                        sql.append(',');
                        appendExpressions(args, expression);
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
                        for (Meta expression : rightOperand) {
                            sql.append(join);
                            appendExpressions(args, expression);
                            join = ',';
                        }
                        sql.append(")");
                    }
                }
                case BETWEEN -> {
                    appendBlank();
                    appendExpressions(args, leftOperand);
                    appendOperator(operator);
                    appendBlank();
                    Meta operate = Expressions
                            .operate(rightOperand.get(0), Operator.AND, List.of(rightOperand.get(1)));
                    appendExpressions(args, operate);
                }
                case IS_NULL, IS_NOT_NULL -> {
                    appendBlank();
                    if (operator0 != null && operator0.priority()
                                             > operator.priority()) {
                        sql.append('(');
                        appendExpressions(args, leftOperand);
                        sql.append(')');
                    } else {
                        appendExpressions(args, leftOperand);
                    }
                    appendBlank();
                    appendOperator(operator);
                }
                default -> throw new UnsupportedOperationException("unknown operator " + operator);
            }
        } else {
            throw new UnsupportedOperationException("unknown expression type " + e.getClass());
        }
    }

    private void appendOperator(Operator jdbcOperator) {
        String sign = jdbcOperator.sign();
        if (Character.isLetter(sign.charAt(0))) {
            appendBlank();
        }
        sql.append(sign);
    }


    protected void appendPath(Paths paths) {
        List<String> expression = paths.paths();
        StringBuilder sb = sql;
        int iMax = expression.size() - 1;
        if (iMax == -1)
            return;
        int i = 0;
        if (expression.size() == 1) {
            appendRootTableAlias().append(".");
        }
        Class<?> type = tableMapping.javaType();

        Paths join = Expressions.ofPaths(List.of(expression.get(0)));

        for (String path : expression) {
            TableMapping info = mappers.getMapping(type);
            FieldMapping attribute = info.getFieldMapping(path);
            if (i++ == iMax) {
                if (attribute instanceof AssociationMapping joinColumnMapper) {
                    sb.append(joinColumnMapper.joinColumnName());
                } else if (attribute instanceof ColumnMapping basicColumnMapper) {
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
            FieldMapping attribute = getAttribute(k);
            TableMapping entityInfo = mappers.getMapping(attribute.javaType());
            sql.append(" left join `").append(entityInfo.tableName()).append("`");

            appendTableAttribute(sql, attribute, v);
            sql.append(ON);
            Paths parent = getParent(k);
            if (parent == null) {
                appendRootTableAlias(sql);
            } else {
                Integer parentIndex = joins.get(parent);
                FieldMapping parentAttribute = getAttribute(parent);
                appendTableAttribute(sql, parentAttribute, parentIndex);
            }
            if (attribute instanceof AssociationMapping join) {
                sql.append(".").append(join.joinColumnName()).append("=");
                appendTableAttribute(sql, attribute, v);
                String referenced = join.referencedColumnName();
                if (referenced.isEmpty()) {
                    referenced = ((ColumnMapping) entityInfo.id()).columnName();
                }
                sql.append(".").append(referenced);
            } else {
                throw new IllegalStateException();
            }
        });
        this.sql.insert(sqlIndex, sql);

    }

    private static Paths getParent(Paths k) {
        if (k == null || k.paths().size() <= 1) {
            return null;
        }
        List<String> paths = new ArrayList<>(k.paths());
        paths.remove(paths.size() - 1);
        return Expressions.ofPaths(paths);
    }

    Operator getOperator(Meta e) {
        return e instanceof Operation expression ? expression.operator() : null;
    }

    protected StringBuilder appendTableAttribute(StringBuilder sb, FieldMapping attribute, Integer index) {
        TableMapping information = mappers.getMapping(attribute.javaType());
        String tableName = information.tableName();
        return appendTableAlias(tableName, index, sb);
    }

    protected FieldMapping getAttribute(Paths path) {
        Mapping mapping = tableMapping;
        for (String s : path.paths()) {
            mapping = mapping.getFieldMapping(s);
        }
        return (FieldMapping) mapping;
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
        List<? extends Meta> groupBy = queryMetadata.groupByClause();
        if (groupBy != null && !groupBy.isEmpty()) {
            sql.append(" group by ");
            boolean first = true;
            for (Meta e : groupBy) {
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
        List<? extends Ordering<?>> orders = queryMetadata.orderByClause();
        if (orders != null && !orders.isEmpty()) {
            sql.append(ORDER_BY);
            boolean first = true;
            for (Ordering<?> order : orders) {
                if (first) {
                    first = false;
                } else {
                    sql.append(",");
                }
                appendExpression(order.meta());
                sql.append(" ").append(order.order() == SortOrder.DESC ? DESC : ASC);
            }

        }
    }
}


