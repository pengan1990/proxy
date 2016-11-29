package handler;

import config.model.TableConfig;
import conn.NonBlockSession;
import org.apache.log4j.Logger;
import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.Wildcard;
import parse.ast.expression.primary.function.FunctionExpression;
import parse.ast.expression.primary.function.groupby.*;
import parse.ast.fragment.GroupBy;
import parse.ast.fragment.Limit;
import parse.ast.fragment.OrderBy;
import parse.ast.fragment.SortOrder;
import parse.ast.stmt.SQLStatement;
import parse.ast.stmt.dml.DMLSelectStatement;
import parse.util.Pair;
import parse.visitor.MySQLOutputASTVisitor;

import java.sql.SQLFeatureNotSupportedException;
import java.util.*;

/**
 * Created by pengan on 16-11-8.
 * <p/>
 * 每种操作都可能出现多列 exp: select min(id), min(name) from table
 * <p/>
 * 作用：
 * <p/>
 * row 数据返回的时候对数据进行的操作 对列　或者　对结果
 * <p/>
 * 预先将sql语句配置 @MySQLOutputASTVisitor 仅仅负责拼装sql语句
 */
public class RouteHintHandler {
    /**
     * 1 如果 select expression 的所有列返回的数据都是一行 比如： count min max sum
     *      sql 语句先不用变更 直接发送到后端
     *      avg 的sql如果发送到多个节点 需要重写 需要添加一列 获取数量
     *
     */

    /**
     * 1 limit 带有order by的情况
     *      但是order by的字段并未出现在 selectExpression当中
     * 2 limit 带有order by情况
     *      但是order by字段出现在select Expression当中
     *
     * limit 操作 如果查出来的列有主键 那么就用主键进行排序 否则用拆分键
     *      如果列当中不包含拆分建列 就直接返回异常
     *
     * limit 如果不带有order by 需要添加order by字段 用拆分字段排序就好
     */

    /**
     * 1 order by 只是涉及到排序 按照每行进行排序就OK
     *      order by的字段需要添加到select当中
     *
     */

    /**
     * 正常使用情况： 使用group by 要么获取group 函数结果 要么获取group by的字段
     * 1 group by 带有order by的情况
     * 用order by 的字段必须得在group by当中
     * 比如： group by id2, id3 order by id1 这种分组完全没有意义
     * 用order by检查 是否需要添加 order by的字段
     * group by的字段是否都在order by当中
     * <p/>
     * 2 group by 不带有order by的情况
     * group by的字段来进行order by
     */

    /**
     * min max sum avg count carry limit sql we have to stop limit
     */
    /**
     *
     */
    private static final Logger logger = Logger.getLogger(RouteHintHandler.class);

    public enum ColumnOps {COUNT, MIN, MAX, SUM, AVG, OTHER}

    private boolean validation;
    private boolean isWildcardExist;
    private int wildcardColPosition;
    private int index;
    // column set
    private Set<Integer> countColumnSet;
    private Set<Integer> minColumnSet;
    private Set<Integer> maxColumnSet;
    private Set<Integer> sumColumnSet;

    // avg column occur in pair Integer[] size == 2
    private Set<Integer> avgColumnSet;
    // avg column index, count column index for avg column
    private Map<Integer, Integer> avgAddColumns;

    // <对应列index, 添加的辅助列> avg column store map
    private Map<Integer, Pair<Expression, String>> additionPairs;

    // 对数据的结果集进行操作
    private Map<Integer, SortOrder> orderByColumns; // order by需要排序 需要比较order by的列值大小
    private Set<Integer> groupByColumnSet; // group by需要合并 需要判断group by的列值是否相等
    // start, start + offset
    private long[] limit;

    private final Map<String, Integer> fieldPositionMap;
    private final Map<Integer, Pair<Expression, String>> additionAvgPairs;
    private final Set<Integer> deleteColumnSet;
    private final NonBlockSession session;

    public RouteHintHandler(NonBlockSession session) {
        this.session = session;
        this.isWildcardExist = false;
        this.wildcardColPosition = 0;
        this.countColumnSet = new LinkedHashSet<Integer>();
        this.minColumnSet = new LinkedHashSet<Integer>();
        this.maxColumnSet = new LinkedHashSet<Integer>();
        this.sumColumnSet = new LinkedHashSet<Integer>();
        this.avgColumnSet = new LinkedHashSet<Integer>();
        this.avgAddColumns = new LinkedHashMap<Integer, Integer>();

        this.fieldPositionMap = new LinkedHashMap<String, Integer>();
        this.additionPairs = new LinkedHashMap<Integer, Pair<Expression, String>>();
        this.orderByColumns = new LinkedHashMap<Integer, SortOrder>();
        this.groupByColumnSet = new LinkedHashSet<Integer>();
        this.limit = new long[]{0, 0};
        this.deleteColumnSet = new LinkedHashSet<Integer>();
        this.additionAvgPairs = new LinkedHashMap<Integer, Pair<Expression, String>>();
    }

    public ColumnOps judgeColumnOperation(int columnSet) {
        logger.debug("judgeColumnOperation");
        if (countColumnSet.contains(columnSet)) {
            return ColumnOps.COUNT;
        } else if (minColumnSet.contains(columnSet)) {
            return ColumnOps.MIN;
        } else if (maxColumnSet.contains(columnSet)) {
            return ColumnOps.MAX;
        } else if (sumColumnSet.contains(columnSet)) {
            return ColumnOps.SUM;
        } else if (avgColumnSet.contains(columnSet)) {
            return ColumnOps.AVG;
        } else {
            return ColumnOps.OTHER;
        }
    }

    public void reset() {
        logger.debug("reset");
        this.validation = false;
        this.wildcardColPosition = 0;
        this.isWildcardExist = false;
        this.index = 0;
        setLimit(0, 0);
        this.sumColumnSet.clear();
        this.minColumnSet.clear();
        this.maxColumnSet.clear();
        this.avgColumnSet.clear();
        this.countColumnSet.clear();
        this.avgAddColumns.clear();

        this.fieldPositionMap.clear();
        this.additionAvgPairs.clear();
        this.additionPairs.clear();
        this.orderByColumns.clear();
        this.groupByColumnSet.clear();
        this.deleteColumnSet.clear();
    }

    public boolean isComplexQuery() {
        logger.debug("isComplexQuery");
        // if have sum count min max or avg  or order by or group by or limit
        return validation;
    }

    public void refresh(int fieldCount) {
        logger.debug("refresh");
        if (isWildcardExist) {
            int increment = fieldCount - index; // fieldCount - index
            addSetIncrement(increment, sumColumnSet, countColumnSet, minColumnSet,
                    maxColumnSet, avgColumnSet, groupByColumnSet, deleteColumnSet);
            addMapIncrement(increment, orderByColumns, avgAddColumns);
        }
    }

    private void addMapIncrement(int increment, Map<Integer, SortOrder> orderByColumns,
                                 Map<Integer, Integer> avgAddColumns) {
        logger.debug("addMapIncrement");
        Map<Integer, SortOrder> newlyOrderByColumns = new LinkedHashMap<Integer, SortOrder>();
        int keyResult;

        for (Map.Entry<Integer, SortOrder> entry : orderByColumns.entrySet()) {
            keyResult = entry.getKey();
            if (entry.getKey() > wildcardColPosition) {
                keyResult = keyResult + increment;
            }
            newlyOrderByColumns.put(keyResult, entry.getValue());
        }
        orderByColumns.clear();
        this.orderByColumns = newlyOrderByColumns;

        int valueResult;
        Map<Integer, Integer> newlyAvgAddColumns = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : avgAddColumns.entrySet()) {
            keyResult = entry.getKey();
            valueResult = entry.getValue();
            if (keyResult > wildcardColPosition) {
                keyResult += increment;
            }
            if (valueResult > wildcardColPosition) {
                valueResult += increment;
            }
            newlyAvgAddColumns.put(keyResult, valueResult);
        }
        avgAddColumns.clear();
        this.avgAddColumns = newlyAvgAddColumns;
    }

    private void addSetIncrement(int increment, Set<Integer>... sets) {
        logger.debug("addSetIncrement");
        List<Integer> results = new LinkedList<Integer>();
        int changeResult;
        for (Set<Integer> set : sets) {
            for (Integer column : set) {
                // column position ge wildcard column position
                changeResult = column;
                if (column > wildcardColPosition) {
                    changeResult += increment;
                }
                results.add(0, changeResult);
            }
            set.clear();
            set.addAll(results);
            results.clear();
        }
    }

    /**
     * here may be rewrite sql
     * <p/>
     * rule to add select expression list : always add after the original
     *
     * @param select
     */
    public void handle(DMLSelectStatement select, TableConfig table) throws SQLFeatureNotSupportedException {
        logger.debug("handle");
        List<Pair<Expression, String>> exprList = select.getSelectExprList();


        Count id = null;
        Avg avg = null;
        for (Pair<Expression, String> pair : exprList) {
            if (pair.getKey() instanceof Sum) {
                this.sumColumnSet.add(index);
            } else if (pair.getKey() instanceof Min) {
                this.minColumnSet.add(index);
            } else if (pair.getKey() instanceof Max) {
                this.maxColumnSet.add(index);
            } else if (pair.getKey() instanceof Count) {
                this.countColumnSet.add(index);
            } else if (pair.getKey() instanceof Avg) {
                this.avgColumnSet.add(index);
                avg = (Avg) pair.getKey();
                id = new Count(avg.getArguments());
                additionAvgPairs.put(index, new Pair<Expression, String>(id, "_COUNT_FOR_AVG_" + index));
            } else if (pair.getKey() instanceof Wildcard) {
                // if contains in * all column index have to add column
                this.isWildcardExist = true;
                this.wildcardColPosition = index;
            } else if (pair.getKey() instanceof Identifier) {
                fieldPositionMap.put(((Identifier) pair.getKey()).getIdTextUpUnescape(), index);// exact column
                fieldPositionMap.put(pair.getValue(), index); // alias column
            }
            index++;
        }

        // 先将avg map添加到select expression list
        for (Map.Entry<Integer, Pair<Expression, String>> entry : additionAvgPairs.entrySet()) {
            exprList.add(entry.getValue());
            fieldPositionMap.put(entry.getValue().getValue(), index);
            avgAddColumns.put(entry.getKey(), index);
            deleteColumnSet.add(index++);
        }
        additionAvgPairs.clear();

        rewriteGroupBy(select);

        rewriteLimit(select, table);

        rewriteOrderBy(select);

        session.getRoutes().setNodeSql(genSQL(select));

        // calculate only once
        validation = this.sumColumnSet.size() > 0 ||
                this.countColumnSet.size() > 0 ||
                this.minColumnSet.size() > 0 ||
                this.maxColumnSet.size() > 0 ||
                this.avgColumnSet.size() > 0 ||
                this.orderByColumns.size() > 0 ||
                this.groupByColumnSet.size() > 0 ||
                this.deleteColumnSet.size() > 0 ||
                (this.limit[0] != this.limit[1]);
    }

    private static StringBuilder genSQL(SQLStatement ast) {
        StringBuilder s = new StringBuilder();
        ast.accept(new MySQLOutputASTVisitor(s));
        return s;
    }

    /**
     * rewrite limit
     * <p/>
     * maybe add order by column
     *
     * @param select
     * @param table
     */
    public void rewriteLimit(DMLSelectStatement select, TableConfig table) {
        logger.debug("rewriteLimit");
        Limit limit = select.getLimit();
        if (limit == null) {
            this.limit[0] = 0;
            this.limit[1] = 0;
            return;
        }
        try {
            long startOffset = Long.parseLong(String.valueOf(limit.getOffset()));
            long endOffset = Long.parseLong(String.valueOf(limit.getSize()));
            setLimit(startOffset, endOffset + startOffset);

            // 这里需要重设limit 重新设置的任务就交给后面的MySQlOutPutVisitor
        } catch (Throwable exp) {
            setLimit(0, 0);
            logger.error(exp.getLocalizedMessage());
        }

        if (select.getOrder() == null && select.getGroup() == null && this.limit[0] != this.limit[1]) {
            /**
             * only limit : assert using sum or min or max or avg
             *
             * only one row : no need to add rule column
             */
            if (this.sumColumnSet.size() > 0 ||
                    this.countColumnSet.size() > 0 ||
                    this.minColumnSet.size() > 0 ||
                    this.maxColumnSet.size() > 0 ||
                    this.avgColumnSet.size() > 0) {
                // assert : only one row send forward here must
                setLimit(0, 1);
                return;
            }

            // if rule column not in selectExpression list add rule column to select expression list
            String ruleColum = table.getRules().get(0).getColumns().get(0);
            Identifier id = null;
            id = new Identifier(null, ruleColum);
            OrderBy orderBy = new OrderBy(id, SortOrder.ASC);
            select.setOrder(orderBy);
        }
    }

    /**
     * validate order by: not support order by count(id)
     *
     * @param select
     * @throws SQLFeatureNotSupportedException
     */
    public void rewriteOrderBy(DMLSelectStatement select) throws SQLFeatureNotSupportedException {
        logger.debug("rewriteOrderBy");
        OrderBy orderBy = select.getOrder();
        if (orderBy == null) {
            return;
        }

        List<Pair<Expression, String>> selectExprs = select.getSelectExprList();
        List<Pair<Expression, SortOrder>> orderByOrderList = orderBy.getOrderByList();
        Identifier id = null;
        for (Pair<Expression, SortOrder> pair : orderByOrderList) {
            if (!(pair.getKey() instanceof Identifier)) {
                // if group by column are not identifier
                throw new SQLFeatureNotSupportedException("order by column are not identifier");
            }
            id = (Identifier) pair.getKey();
            if (!fieldPositionMap.containsKey(id.getIdTextUpUnescape())) {
                // select column not contain order by column
                selectExprs.add(new Pair<Expression, String>(id, id.getIdTextUpUnescape()));
                fieldPositionMap.put(id.getIdTextUpUnescape(), index);
                orderByColumns.put(index, pair.getValue()); // default
                deleteColumnSet.add(index++);
                continue;
            }
            orderByColumns.put(fieldPositionMap.get(id.getIdTextUpUnescape()), pair.getValue());
        }
    }

    /**
     * validate group operation
     * <p/>
     * maybe add order by column
     *
     * @param select
     * @throws SQLFeatureNotSupportedException
     */
    public void rewriteGroupBy(DMLSelectStatement select) throws SQLFeatureNotSupportedException {
        logger.debug("rewriteGroupBy");
        OrderBy orderBy = select.getOrder();
        GroupBy groupBy = select.getGroup();

        if (groupBy == null) {
            // if orderBy == null return
            return;
        }

        if (isWildcardExist) {
            // * in select expression list throw error
            throw new SQLFeatureNotSupportedException("group by not support * in select expression");
        }

        if (orderBy == null) {
            // order by is null using group by order list
            select.setOrder(new OrderBy(groupBy.getOrderByList()));
            return;
        }

        // rewrite order by
        List<Pair<Expression, SortOrder>> groupByOrderList = groupBy.getOrderByList();
        List<Pair<Expression, SortOrder>> orderByOrderList = orderBy.getOrderByList();
        if (groupByOrderList.size() < orderByOrderList.size()) {
            String errMsg = "group by order list size less then order by order list size ";
            throw new SQLFeatureNotSupportedException(errMsg);
        }

        Identifier id = null;
        Map<String, Pair<Expression, SortOrder>> orderColumns = new LinkedHashMap<String, Pair<Expression, SortOrder>>();
        for (Pair<Expression, SortOrder> pair : groupByOrderList) {
            if (!(pair.getKey() instanceof Identifier)) {
                // if group by column are not identifier
                throw new SQLFeatureNotSupportedException("group by column are not identifier");
            }
            id = (Identifier) pair.getKey();
            orderColumns.put(id.getIdTextUpUnescape(), pair);
        }

        for (Pair<Expression, String> pair : select.getSelectExprList()) {
            if (pair.getKey() instanceof FunctionExpression &&
                    !isSupportFunction(pair.getKey())) {
                String errMsg = "group function :" + ((FunctionExpression) pair.getKey()).getFunctionName()
                        + " is no supported";
                throw new SQLFeatureNotSupportedException(errMsg);
            }
            if (pair.getKey() instanceof Identifier &&
                    !orderColumns.containsKey(((Identifier) pair.getKey()).getIdTextUpUnescape())) {
                String errMsg = "select identifier " + ((Identifier) pair.getKey()).getIdTextUpUnescape() +
                        " not contains in group by order list";
                throw new SQLFeatureNotSupportedException(errMsg);
            }
        }

        for (Pair<Expression, SortOrder> pair : orderByOrderList) {
            if (!(pair.getKey() instanceof Identifier)) {
                // if group by column are not identifier
                throw new SQLFeatureNotSupportedException("order by column are not identifier");
            }
            id = (Identifier) pair.getKey();
            if (!orderColumns.containsKey(id.getIdTextUpUnescape())) {
                String errMsg = "order by column:" + id.getIdTextUpUnescape() + " not in group by order list";
                throw new SQLFeatureNotSupportedException(errMsg);
            }
            orderColumns.remove(id.getIdTextUpUnescape());
        }
        orderBy.getOrderByList().addAll(orderColumns.values());

        // rewrite select expression list
        List<Pair<Expression, String>> selectExpressionList = select.getSelectExprList();
        for (String key : orderColumns.keySet()) { // group by columns
            if (!fieldPositionMap.containsKey(key)) {
                // select expression list not contain group by column
                id = new Identifier(null, key);
                selectExpressionList.add(new Pair<Expression, String>(id, key));
                fieldPositionMap.put(key, index); // map应该也添加
                deleteColumnSet.add(index++);
            }
            groupByColumnSet.add(fieldPositionMap.get(key)); //
        }

        // cleanup
        orderColumns.clear();
    }

    private static boolean isSupportFunction(Expression key) {
        logger.debug("isSupportFunction");
        if (key instanceof Sum
                || key instanceof Count
                || key instanceof Min
                || key instanceof Max
                || key instanceof Avg) {
            return true;
        }
        return false;
    }

    public Map<Integer, SortOrder> getOrderByColumns() {
        return orderByColumns;
    }

    public Set<Integer> getGroupByColumnSet() {
        return groupByColumnSet;
    }

    public long[] getLimit() {
        return limit;
    }

    public Set<Integer> getDeleteColumnSet() {
        return deleteColumnSet;
    }

    public Map<Integer, Integer> getAvgAddColumns() {
        return avgAddColumns;
    }

    public void setLimit(long start, long end) {
        this.limit[0] = start;
        this.limit[1] = end;
    }

}
