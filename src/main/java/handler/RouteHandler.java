package handler;

import config.loader.MetaConfig;
import config.model.DataNodeConfig;
import config.model.SchemaConfig;
import config.model.TableConfig;
import config.model.rule.RuleAlgorithm;
import config.model.rule.RuleConfig;
import conn.ServerConnection;
import handler.frontend.*;
import mysql.ErrorCode;
import org.apache.log4j.Logger;
import parse.ast.ASTNode;
import parse.ast.expression.Expression;
import parse.ast.expression.primary.Identifier;
import parse.ast.expression.primary.RowExpression;
import parse.ast.stmt.SQLStatement;
import parse.ast.stmt.dal.DALShowStatement;
import parse.ast.stmt.ddl.DDLAlterTableStatement;
import parse.ast.stmt.ddl.DDLStatement;
import parse.ast.stmt.ddl.DescTableStatement;
import parse.ast.stmt.dml.*;
import parse.recognizer.SQLParserDelegate;
import parse.util.Pair;
import parse.visitor.MySQLOutputASTVisitor;
import parse.visitor.PartitionKeyVisitor;
import route.RouteResult;
import server.ProxyServer;
import util.MySQLMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.util.*;

/**
 * Created by pengan on 16-10-5.
 */
public class RouteHandler implements Handler {
    private static final Logger logger = Logger.getLogger(RouteHandler.class);

    private StringBuilder sql;
    private final List<TableConfig> tables;
    private final ServerConnection conn;

    public RouteHandler(ServerConnection conn) {
        this.conn = conn;
        this.tables = new LinkedList<TableConfig>();
    }

    private static final MetaConfig META_CONFIG = ProxyServer.getINSTANCE().getMetaConfig();

    /**
     * get table first datanode
     * if table not exist just use first schema datanode
     *
     * @param schema
     * @param table
     * @return
     */
    public static DataNodeConfig handleFieldCommand(String schema, String table) {
        TableConfig tableConfig;
        SchemaConfig schemaConfig = META_CONFIG.getSchemas().get(schema);
        Map<String, TableConfig> tables = META_CONFIG.getSchemaTables().get(schemaConfig.getId());
        if (tables.containsKey(table)) {
            tableConfig = tables.get(table);
        } else {
            tableConfig = tables.values().iterator().next();
        }
        return META_CONFIG.getDataNodes().get(tableConfig.getDataNodes()[0]);
    }

    @Override
    public void handle(byte[] data) throws IOException {
        logger.debug("handle");
        MySQLMessage mm = new MySQLMessage(data);
        mm.position(5);
        sql = new StringBuilder();
        try {
            sql.append(mm.readString(conn.getCharset()));
        } catch (UnsupportedEncodingException exp) {
            error(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "unknown character set");
            return;
        }
        logger.debug(sql);
        if (sql == null || sql.length() == 0) {
            error(ErrorCode.ER_EMPTY_QUERY, "Query was empty");
            return;
        }
        SchemaConfig schema = META_CONFIG.getSchemas().get(conn.getSchema());

        SQLStatement ast = null;
        try {
            ast = SQLParserDelegate.parse(sql, conn.getCharset());
        } catch (SQLSyntaxErrorException e) {
            logger.error(e.getLocalizedMessage());
            error(ErrorCode.ER_PARSE_ERROR, " parse " + sql + " error");
            return;
        }

        if (intercept(ast)) {
            return;
        }

        if (schema == null) {
            // TODO: 16-11-5 schem is null no sql allowed except for the interceptions
            error(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }

        RouteResult routes = conn.getSession().getRoutes();
        routes.setOriginalSql(sql);
        try {
            calculateRoute(schema, ast, routes);
            if (routes.getNodeSqls().size() == 0) {
                error(ErrorCode.ER_PARSE_ERROR, "no route for sql: " + routes.getOriginalSql());
            }
        } catch (Throwable exp) {
            logger.error(exp.getLocalizedMessage());
            routes.reset();// clear routes
            error(ErrorCode.ER_PARSE_ERROR, exp.getLocalizedMessage());
        } finally {
            tables.clear();
        }
    }

    /**
     * intercept for specified ast statement
     *
     * @param ast
     */
    private boolean intercept(SQLStatement ast) {
        logger.debug("intercept");
        switch (ast.getStmtType()) {
            case DAL_SHOW_DATABASES:
                // show database
                ShowDatabaseHandler.response(conn);
                break;
            case DAL_SET:
                // keep set
                SetHandler.response(conn, ast);
                break;
            case DML_KILL:
                // keep kill is an trouble
                DMLKIllStatement kill = (DMLKIllStatement) ast;
                KillHandler.response(conn, kill.getConnectionId());
                break;
            case DML_USE:
                // use db
                DMLUseStatement use = (DMLUseStatement) ast;
                UseHandler.response(conn, use.getSchema().getIdTextUpUnescape());
                break;
            case BEGIN:
                TransactionHandler.begin(conn);
                break;
            case COMMIT:
                return TransactionHandler.commit(conn, sql);
            case ROLLBACK:
                return TransactionHandler.rollback(conn, sql);
            default:
                return false;
        }
        return true;
    }

    /**
     * calculate route
     * <p/>
     * use switch rather than if else
     *
     * @param schema
     * @param ast
     */
    private void calculateRoute(SchemaConfig schema, SQLStatement ast, RouteResult routes) throws SQLFeatureNotSupportedException {
        logger.debug("calculateRoute");
        PartitionKeyVisitor visitor = new PartitionKeyVisitor(schema.getTables());
        visitor.setTrimSchema(null);
        ast.accept(visitor);

        switch (ast.getStmtType()) {
            case DAL_SHOW:
                showRoute(visitor, (DALShowStatement) ast, schema, routes);
                return;
            case DML_INSERT:
            case DML_REPLACE:
                if (insertIllegalCheck(visitor, (DMLInsertReplaceStatement) ast, schema)) {
                    // get insert route
                    insertRoute(visitor, (DMLInsertReplaceStatement) ast, schema, routes);
                    if (!conn.isTransaction() && routes.getNodeSqls().size() > 1) {
                        conn.getSession().getHandler().setTransaction(true);
                    }
                    return;
                }
                logger.error("insertIllegalCheck: error");
                break;
            case DML_DELETE:
                if (illegalCheck(visitor, schema)) {
                    // get delete route
                    route(visitor, schema, routes);
                    if (!conn.isTransaction() && routes.getNodeSqls().size() > 1) {
                        conn.getSession().getHandler().setTransaction(true);
                    }
                    return;
                }
                logger.error("deleteIllegalCheck: error");
                break;
            case DML_UPDATE:
                if (updateIllegalCheck(visitor, (DMLUpdateStatement) ast, schema)) {
                    // get update route
                    route(visitor, schema, routes);
                    if (!conn.isTransaction() && routes.getNodeSqls().size() > 1) {
                        conn.getSession().getHandler().setTransaction(true);
                    }
                    return;
                }
                logger.error("updateIllegalCheck: error");
                break;
            case DML_SELECT:
            case DML_SELECT_UNION:
                if (illegalCheck(visitor, schema)) {
                    // get select route
                    route(visitor, schema, routes);
                    // TODO: 16-11-8 support limit & order by & group by & count & min & max & sum & avg
                    if (routes.getNodeSqls().size() < 2 || ast instanceof DMLSelectUnionStatement) {
                        return;
                    }

                    if (tables.size() != 1) {
                        return;
                    }
                    // route getNodeSql size >= 2
                    RouteHintHandler routeHint = conn.getSession().getRouteHint();

                    routeHint.handle((DMLSelectStatement) ast, tables.get(0));
                    return;
                }
                logger.error("illegalCheck: error");
                break;
            case DSEC:
                descRoute(visitor, (DescTableStatement) ast, schema, routes);
                return;
            case DDL:
                if (!(ast instanceof DDLAlterTableStatement) ||
                        alterIllegalCheck(visitor, (DDLAlterTableStatement) ast, schema)) {
                    // send to each data node
                    ddlRoute(visitor, (DDLStatement) ast, schema, routes, sql);
                    return;
                }
                logger.error("DDLStatement: error");
                break;
        }
        String msg = "unsupported ast class: " + ast.getClass() + " and type: " + ast.getStmtType() + " and sql: " + sql;
        logger.error(msg);
        throw new IllegalArgumentException(msg);
    }

    private RouteResult descRoute(PartitionKeyVisitor visitor, DescTableStatement ast, SchemaConfig schema, RouteResult routes) {
        logger.debug("descRoute");
        String table = ast.getTable().getIdTextUpUnescape();
        if (schema.getTables().containsKey(table)) {
            String nodeId = schema.getTables().get(table).getDataNodes()[0];
            routes.getNodeSqls().put(nodeId, sql);
            return routes;
        }
        for (String datanodeId : schema.getMetaDataNodes()) {
            routes.getNodeSqls().put(datanodeId, sql);
        }
        return routes;
    }

    /**
     * ddl if legal then send to each data node
     *
     * @param visitor
     * @param ast
     * @param schema
     * @param routes
     * @param sql
     * @return
     */
    private RouteResult ddlRoute(PartitionKeyVisitor visitor, DDLStatement ast,
                                 SchemaConfig schema, RouteResult routes, StringBuilder sql) {
        logger.debug("ddlRoute");
        for (String table : visitor.getColumnValue().keySet()) {
            // send to each datanode
            for (String datanodeId : schema.getTables().get(table).getDataNodes()) {
                routes.getNodeSqls().put(datanodeId, sql);
            }
        }
        return routes;
    }

    /**
     * alter table change shard column is not allowed
     *
     * @param visitor
     * @param ast
     * @return
     */
    private boolean alterIllegalCheck(PartitionKeyVisitor visitor, DDLAlterTableStatement ast, SchemaConfig schema) {
        logger.debug("alterIllegalCheck");
        String table = ast.getTable().getIdTextUpUnescape();
        TableConfig tableConfig = schema.getTables().get(table);

        if (tableConfig == null) {
            // if table not exist in data node
            return true;
        }
        switch (tableConfig.getRules().size()) {
            case 1:
                for (DDLAlterTableStatement.AlterSpecification alterSpec : ast.getAlters()) {
                    if (alterSpec instanceof DDLAlterTableStatement.DropColumn) {
                        DDLAlterTableStatement.DropColumn drop = (DDLAlterTableStatement.DropColumn) alterSpec;
                        // if rule column contain drop column then exception
                        if (tableConfig.getRouteColumn().contains(drop.getColName().getIdTextUpUnescape())) {
                            String errMsg = "drop column name is shard column: " + drop.getColName().getIdTextUpUnescape();
                            throw new IllegalArgumentException(errMsg);
                        }
                    } else if (alterSpec instanceof DDLAlterTableStatement.ChangeColumn) {
                        DDLAlterTableStatement.ChangeColumn change = (DDLAlterTableStatement.ChangeColumn) alterSpec;

                        // if rule columns contains change column and change column name diff from old
                        if (!change.getOldName().getIdTextUpUnescape().equals(change.getNewName().getIdTextUpUnescape()) &&
                                tableConfig.getRouteColumn().contains(change.getOldName().getIdTextUpUnescape())) {
                            String errMsg = "drop column name is shard column: " +
                                    change.getOldName().getIdTextUpUnescape();
                            throw new IllegalArgumentException(errMsg);
                        }
                    }
                }
                break;
            default:
                String errMsg = "too many rules for table " + table;
                throw new IllegalArgumentException(errMsg);
        }
        return true;
    }

    /**
     * select sql just is ok for each
     * <p/>
     * limitation: for select or delete
     *
     * @param visitor
     * @param schema
     * @return
     */
    private boolean illegalCheck(PartitionKeyVisitor visitor, SchemaConfig schema) {
        logger.debug("illegalCheck");
        // table name , column name, column values
        Map<String, Map<String, List<Object>>> columnValue = visitor.getColumnValue();

        // each table have to registered
        for (String table : columnValue.keySet()) {
            if (!schema.getTables().containsKey(table)) {
                String errMsg = "table not exist in meta info " + table;
                throw new IllegalArgumentException(errMsg);
            }
        }
        return true;
    }

    /**
     * update sql no need to rewrite then just assert the correct data node
     * <p/>
     * limitation: only support one shard rule
     *
     * @param visitor
     * @param schema
     * @param routes
     * @return
     */
    private RouteResult route(PartitionKeyVisitor visitor, SchemaConfig schema, RouteResult routes) {
        logger.debug("routes");
        // each table has its own shard key and shard into different data node throw error

        Set<String> preDataNodes = new LinkedHashSet<String>();
        Set<String> currDataNodes = new LinkedHashSet<String>();
        TableConfig tableConfig = null;
        for (Map.Entry<String, Map<String, List<Object>>> entry : visitor.getColumnValue().entrySet()) {
            // calculate each table shard value
            tableConfig = schema.getTables().get(entry.getKey());
            tables.add(tableConfig);
            if (entry.getValue() == null || entry.getValue().size() == 0) {
                // entry get value then send to all
                for (String datanode : tableConfig.getDataNodes()) {
                    currDataNodes.add(datanode);
                }
            } else if (tableConfig.getRules().size() == 1) { // 支持一种拆分规则
                // has column values then calculate
                RuleConfig rule = tableConfig.getRules().get(0);
                Map<String, List<Object[]>> datanodeMap = ruleCalculate(tableConfig, rule, entry.getValue());
                currDataNodes.addAll(datanodeMap.keySet());
            }
            if (preDataNodes.size() == 0) {
                preDataNodes.addAll(currDataNodes);
                currDataNodes.clear();
                continue;
            }
            if (!preDataNodes.equals(currDataNodes)) {
                // if data node not equal then throw exception that
                String errMsg = "no consistency to each table shard: " + sql;
                throw new IllegalArgumentException(errMsg);
            }
            preDataNodes.clear();
            preDataNodes.addAll(currDataNodes);
            currDataNodes.clear();
        }

        // no need to worry about that preData nodes size is 0
        for (String dataNode : preDataNodes) {
            routes.getNodeSqls().put(dataNode, sql);
        }
        return routes;
    }

    /**
     * @param visitor
     * @param ast
     * @param schema
     * @return
     */
    private boolean updateIllegalCheck(PartitionKeyVisitor visitor, DMLUpdateStatement ast, SchemaConfig schema) {
        logger.debug("updateIllegalCheck");
        // delete more than just one table
        Map<String, Map<String, List<Object>>> columnValue = visitor.getColumnValue();

        illegalCheck(visitor, schema);

        logger.debug("check only one data node");
        /**
         * only one data node then just return true
         */
        for (String table : columnValue.keySet()) {
            TableConfig tableConfig = schema.getTables().get(table);
            if (tableConfig.getDataNodes().length == 1) {
                return true;
            }
        }

        logger.debug("check whether the rule column in set clause");
        /**
         * check whether the rule column in set clause
         *
         * because cannot diff which value belongs to each table
         *
         * so just forbidden all
         */
        Map<String, Set<String>> tableRuleColumnMap = new LinkedHashMap<String, Set<String>>();
        for (String table : columnValue.keySet()) {
            TableConfig tableConfig = schema.getTables().get(table);
            tableRuleColumnMap.put(table, tableConfig.getRouteColumn());
        }
        Map<String, String> alias = visitor.getTableAlias();
        String columnName = null;
        for (Pair<Identifier, Expression> pair : ast.getValues()) {
            // table.name : the id may be short for table name
            String table = pair.getKey().getLevelUnescapeUpName(2);
            table = alias.get(table);
            Set<String> ruleColumns = tableRuleColumnMap.get(table);
            columnName = pair.getKey().getIdTextUpUnescape();
            if (ruleColumns != null && ruleColumns.contains(columnName)) {
                StringBuilder errMsg = new StringBuilder();
                errMsg.append("route column:").append(columnName).append(" cannot be changed");
                logger.debug(errMsg);
                throw new IllegalArgumentException(errMsg.toString());
            }
        }
        return true;
    }

    /**
     * insert sql split into several statement according to shard key value
     *
     * @param visitor
     * @param ast
     * @param schema
     * @param routes
     * @return
     */
    private RouteResult insertRoute(PartitionKeyVisitor visitor, DMLInsertReplaceStatement ast,
                                    SchemaConfig schema, RouteResult routes) {

        logger.debug("insertRoute");
        String errMsg = null;
        String table = ast.getTable().getIdTextUpUnescape();
//        Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> colVals = visitor.getColumnIndex(table);
        TableConfig tableConfig = schema.getTables().get(table);
        // no rule for table then just send to the only datanode
        if (tableConfig == null) {
            errMsg = "table not exist in meta db";
            logger.debug(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        switch (tableConfig.getDataNodes().length) {
            case 0:
                errMsg = "no datanode for table " + table;
                logger.debug(errMsg);
                throw new IllegalArgumentException(errMsg);
            case 1:
                logger.debug("only one datanode for table " + table);
                String datanodeId = tableConfig.getDataNodes()[0];
                routes.getNodeSqls().put(datanodeId, sql);
                return routes;
            default:
                // has rule need to calculate rule
                if (tableConfig.getRules() == null || tableConfig.getRules().size() != 1) {
                    errMsg = "incorrect relation for table " + table;
                    logger.debug(errMsg);
                    throw new IllegalArgumentException(errMsg);
                }
                RuleConfig rule = tableConfig.getRules().get(0);
                //
                Map<String, List<RowExpression>> dnRowMap = new LinkedHashMap<String, List<RowExpression>>();

                Map<String, List<Object>> colValMap = new LinkedHashMap<String, List<Object>>();
                final Map<Object, Object> evaluationParameter = Collections.emptyMap();

                Set<String> ruleColumns = tableConfig.getRouteColumn();
                Map<Integer, String> colIndexMap = new LinkedHashMap<Integer, String>();

                // first need to locate rule column position
                for (int index = 0; index < ast.getColumnNameList().size(); index++) {
                    Identifier column = ast.getColumnNameList().get(index);
                    if (ruleColumns.contains(column.getIdTextUpUnescape())) {
                        colIndexMap.put(index, column.getIdTextUpUnescape());
                    }
                }

                // classify rows
                for (RowExpression row : ast.getRowList()) {
                    // each row has one value for rule column
                    for (Integer index : colIndexMap.keySet()) {
                        List<Object> values = new LinkedList<Object>();
                        values.add(row.getRowExprList().get(index).evaluation(evaluationParameter));
                        colValMap.put(colIndexMap.get(index), values);
                    }
                    // calculate
                    for (Map.Entry<String, List<Object[]>> entry : ruleCalculate(tableConfig, rule, colValMap).entrySet()) {
                        if (dnRowMap.containsKey(entry.getKey())) {
                            dnRowMap.get(entry.getKey()).add(row);
                        } else {
                            List<RowExpression> rows = new LinkedList<RowExpression>();
                            rows.add(row);
                            dnRowMap.put(entry.getKey(), rows);
                        }
                    }
                    // clear column value map
                    for (List<Object> values : colValMap.values()) {
                        values.clear();
                    }
                    colValMap.clear();
                }

                for (Map.Entry<String, List<RowExpression>> entry : dnRowMap.entrySet()) {
                    ast.setReplaceRowList(entry.getValue());
                    routes.getNodeSqls().put(entry.getKey(), genSQL(ast));
                }
                return routes;
        }
    }

    /**
     * insert statement have to carry shard key and not null
     *
     * @param ast
     * @param schema
     * @return
     */
    private boolean insertIllegalCheck(PartitionKeyVisitor visitor, DMLInsertReplaceStatement ast, SchemaConfig schema) {
        logger.debug("insertIllegalCheck");
        String table = ast.getTable().getIdTextUpUnescape();
        TableConfig tableConfig = schema.getTables().get(table);
        if (tableConfig == null) {
            String errMsg = "table " + table + "  not exist int meta info";
            logger.debug(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        // table have no data node relation error for this table insert
        if (tableConfig.getDataNodes() == null || tableConfig.getDataNodes().length == 0) {
            String errMsg = "table " + table + " have no data node relation ";
            logger.debug(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        // check the route column all in insert column and store column index
        Set<String> routeCols = tableConfig.getRouteColumn();

        Map<String, Map<Object, Set<Pair<Expression, ASTNode>>>> colVals = visitor.getColumnIndex(table);
        if (colVals == null || routeCols.size() != colVals.size()) {
            String errMsg = "table " + table + " without carry all shard key in insert column";
            logger.debug(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        // check null value
        for (String colName : routeCols) {
            // if any column value is null
            if (colVals.get(colName).containsKey(null)) {
                String errMsg = "table " + table + " shard column " + colName + " value is null";
                logger.debug(errMsg);
                throw new IllegalArgumentException(errMsg);
            }
        }
        return true;
    }

    private RouteResult showRoute(PartitionKeyVisitor visitor, DALShowStatement ast,
                                  SchemaConfig schema, RouteResult routes) {
        String[] tables = visitor.getMetaReadTable();
        if (tables == null) {
            throw new IllegalArgumentException("route err: tables[] is null for meta readFromChannel table: " + sql);
        }
        String[] datanodes = null;
        switch (tables.length) {
            case 0:
                datanodes = schema.getMetaDataNodes();
                break;
            case 1:
                datanodes = new String[1];
                datanodes[0] = getMetaReadDataNode(schema, tables[0]);
                break;
            default:
                throw new IllegalArgumentException("tables.length : " + tables.length + " for sql " + sql);
        }
        for (String dn : datanodes) {
            routes.getNodeSqls().put(dn, sql);
        }
        return routes;
    }

    private static StringBuilder genSQL(SQLStatement ast) {
        StringBuilder s = new StringBuilder();
        ast.accept(new MySQLOutputASTVisitor(s));
        return s;
    }

    private void error(int errno, String msg) {
        logger.debug("errno");
        conn.writeErrMessage(errno, msg);
    }

    private static String getMetaReadDataNode(SchemaConfig schema, String table) {
        logger.debug("getMetaReadDataNode");
        String dataNode = null;
        Map<String, TableConfig> tables = schema.getTables();
        TableConfig tc;
        if (tables != null && (tc = tables.get(table)) != null) {
            String[] dn = tc.getDataNodes();
            if (dn != null && dn.length > 0) {
                dataNode = dn[0];
            }
        }
        return dataNode;
    }

    private static Map<String, List<Object[]>> ruleCalculate(TableConfig matchedTable, RuleConfig rule,
                                                             Map<String, List<Object>> columnValues) {
        logger.debug("ruleCalculate");
        //<dataNode index => List<router'column'values>>
        Map<String, List<Object[]>> map = new HashMap<String, List<Object[]>>(1, 1);
        RuleAlgorithm algorithm = null;
        List<String> cols = null;

        if (rule != null) {
            algorithm = rule.getRuleAlgorithm();
            //List<column's name upper>
            cols = rule.getColumns();
        } else {
            cols = new ArrayList<String>(matchedTable.getColValNode().keySet());
        }
        // <column's name upper => column's value>
        Map<String, Object> parameter = new HashMap<String, Object>(cols.size(), 1);
        // ArrayList<column'value'iterator>
        ArrayList<Iterator<Object>> colsValIter = new ArrayList<Iterator<Object>>(columnValues.size());

        for (String rc : cols) {
            //List<column's value>
            List<Object> list = columnValues.get(rc);
            if (list == null) {
                String msg = "route err: rule column " + rc + " dosn't exist in extract: " + columnValues;
                throw new IllegalArgumentException(msg);
            }
            colsValIter.add(list.iterator());
        }

        try {
            for (Iterator<Object> mainIter = colsValIter.get(0); mainIter.hasNext(); ) {
                Object[] tuple = new Object[cols.size()];
                for (int i = 0, len = cols.size(); i < len; ++i) {
                    Object value = colsValIter.get(i).next();
                    tuple[i] = value;
                    parameter.put(cols.get(i), value);
                }
                //calc datanode index
                Integer[] dataNodeIndexes = calcDataNodeIndexesByFunction(algorithm, parameter);

                for (int i = 0; i < dataNodeIndexes.length; ++i) {
                    Integer dataNodeIndex = dataNodeIndexes[i];
                    String datanodeid = matchedTable.getDataNodes()[dataNodeIndex];
                    List<Object[]> list = map.get(datanodeid);
                    if (list == null) {
                        list = new LinkedList<Object[]>();
                        map.put(datanodeid, list);
                    }
                    // put columns' value array into list
                    list.add(tuple);
                }
            }
        } catch (NoSuchElementException e) {
            String msg = "route err: different rule columns should have same value number:  " + columnValues;
            throw new IllegalArgumentException(msg, e);
        }

        return map;
    }

    private static Integer[] calcDataNodeIndexesByFunction(RuleAlgorithm algorithm, Map<String, Object> parameter) {
        Integer[] dataNodeIndexes;
        Object calRst = algorithm.calculate(parameter);
        if (calRst instanceof Number) {
            dataNodeIndexes = new Integer[1];
            dataNodeIndexes[0] = ((Number) calRst).intValue();
        } else if (calRst instanceof Integer[]) {
            dataNodeIndexes = (Integer[]) calRst;
        } else if (calRst instanceof int[]) {
            int[] intArray = (int[]) calRst;
            dataNodeIndexes = new Integer[intArray.length];
            for (int i = 0; i < intArray.length; ++i) {
                dataNodeIndexes[i] = intArray[i];
            }
        } else {
            throw new IllegalArgumentException("route err: result of route function is wrong type or null: " + calRst);
        }
        return dataNodeIndexes;
    }
}
