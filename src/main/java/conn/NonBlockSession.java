package conn;

import config.model.DataNodeConfig;
import handler.Handler;
import handler.RouteHandler;
import handler.RouteHintHandler;
import handler.SessionHandler;
import handler.frontend.CommandHandler;
import mysql.ErrorCode;
import nio.Processor;
import org.apache.log4j.Logger;
import route.RouteResult;
import server.ProxyServer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by pengan on 16-9-23.
 * <p/>
 * non block session a container to hold frontend connection and mysql conn
 * <p/>
 * routes are bright from front end connection to mysql conn
 * <p/>
 * all event was triggered by front end connection
 */
public class NonBlockSession {
    private static final Logger logger = Logger.getLogger(NonBlockSession.class);

    private RouteResult routes;
    private RouteHintHandler routeHint;
    private ServerConnection frontConn;
    private List<MySQLConnection> backConns; // save current sql related mysql connections

    // <schema, mysqlConnection>
    private final Map<String, MySQLConnection> mySQLNodes; // store all used mysql connections
    private Handler commandHandler;
    private Handler routeHandler;
    private SessionHandler handler;

    public NonBlockSession(ServerConnection frontendConn) {
        this.frontConn = frontendConn;
        this.commandHandler = new CommandHandler(frontendConn);
        this.routeHandler = new RouteHandler(frontendConn);
        this.handler = new SessionHandler(this);
        this.mySQLNodes = new LinkedHashMap<String, MySQLConnection>();
        this.routes = new RouteResult();
        this.routeHint = new RouteHintHandler(this);
        this.backConns = new LinkedList<MySQLConnection>();
    }

    /**
     * init mysql connections
     * <p/>
     * frontendRegister all to processor
     *
     * @throws IOException
     */
    public void execute() {
        logger.debug("execute");
        if (routes == null || routes.getNodeSqls().size() == 0) {
//            String errMsg = (routes == null ? "" : routes.getOriginalSql());
//            frontConn.error(ErrorCode.ERR_NO_ROUTE, "no route for sql: " + errMsg);
            return;
        }
        backConns.clear();
        handler.reset(routes.getNodeSqls().size());

        Map<String, DataNodeConfig> nodes = ProxyServer.getINSTANCE().getMetaConfig().getDataNodes();
        int index = 0;

        try {
            MySQLConnection conn = null;
            for (Map.Entry<String, StringBuilder> entry : routes.getNodeSqls().entrySet()) {

                String dataNodeId = entry.getKey();
                DataNodeConfig nodeConfig = nodes.get(dataNodeId);
                // mysql connection may get from mySQLNodes it means it have transaction and not ended
                if (mySQLNodes.containsKey(nodeConfig.getDatabase())) {
                    conn = mySQLNodes.get(nodeConfig.getDatabase()); //
                    conn.setExecuteSQL(entry.getValue());
                    conn.setIndex(index);

                    // connection is already registered
                    conn.execute();
                } else {
                    conn = getConnectionFromPool(nodeConfig);
                    setMySQLConnOpts(conn, nodeConfig.getDatabase(), entry.getValue(), index);
                    // just frontendRegister in present processor selector
                    frontConn.getProcessor().backendRegister(conn);
                }
                backConns.add(conn);
                index++;
            }
        } catch (IOException exp) {
            // if error then no new connection created
            frontConn.error(ErrorCode.ER_NET_FCNTL_ERROR,
                    "get backend connection error " + exp.getLocalizedMessage());
            releaseCurrBackConns();
            routeHint.reset();
            handler.reset(0);
            // 此时前端连接并不断开
        } finally {
            routes.reset();
        }
    }

    private MySQLConnection getConnectionFromPool(DataNodeConfig nodeConfig) throws IOException {
        logger.debug("getConnectionFromPool");
        Processor processor = frontConn.getProcessor();
        Map<String, MySQLConnectionPool> instancePools = processor.getInstancePools();
        MySQLConnectionPool pool = instancePools.get(nodeConfig.getInstanceId());
        MySQLConnection conn = pool.borrowObject(nodeConfig.getDatabase());
        if (conn == null) {
            conn = pool.create(processor);
        } else {
            conn.setNewlyCreated(false); // get from pool
        }
        mySQLNodes.put(nodeConfig.getDatabase(), conn);
        return conn;
    }

    private void setMySQLConnOpts(MySQLConnection conn, String schema,
                                  StringBuilder nodeSql, int index) {
        logger.debug("setMySQLConnOpts");
        // init mysql
        conn.setSchema(schema);
        conn.setExecuteSQL(nodeSql);
        conn.setSession(this);
        conn.setIndex(index);
    }

    public ServerConnection getFrontConn() {
        return frontConn;
    }

    public Handler getRouteHandler() {
        return routeHandler;
    }

    public Handler getCommandHandler() {
        return commandHandler;
    }

    /**
     * session close
     * <p/>
     * kill by sbd.
     */
    public void close() throws IOException {
        logger.debug("front close");
        frontConn.close();
        releaseTotalBackConns();
    }

    /**
     * release current using back connection which are in this.backConns
     */
    private void releaseCurrBackConns() {
        logger.debug("releaseCurrBackConns");
        // already using connection have to close
        List<MySQLConnection> backConns = this.backConns;
        Map<String, MySQLConnection> nodes = this.mySQLNodes;
        for (MySQLConnection conn : backConns) {
            if (conn == null) {
                continue;
            }
            try {
                if (conn.isUsing()) {
                    logger.debug("close mysql connection " + conn.getSchema());
                    conn.close();
                    continue;
                }
                logger.debug("return mysql connection " + conn.getSchema() + " to pool");
                conn.returnToPool();
            } finally {
                nodes.remove(conn.getSchema());
            }
        }
    }

    /**
     * release total back connections which are in
     */
    private void releaseTotalBackConns() {
        logger.debug("releaseTotalBackConns");
        Map<String, MySQLConnection> nodes = mySQLNodes;
        for (Map.Entry<String, MySQLConnection> entry : nodes.entrySet()) {
            MySQLConnection conn = entry.getValue();
            if (!conn.isUsing()) {
                conn.returnToPool();
                continue;
            }
            logger.debug("reset mysql connection close");
            conn.close();
        }
        nodes.clear();
        List<MySQLConnection> backConns = this.backConns;
        backConns.clear();
    }

    public SessionHandler getHandler() {
        return handler;
    }

    public void executePacket(DataNodeConfig nodeConfig, byte[] data) throws IOException {
        logger.debug("executePacket");
        backConns.clear();
        handler.reset(1);

        Map<String, MySQLConnection> nodes = mySQLNodes;
        MySQLConnection conn = null;
        if (nodes.containsKey(nodeConfig.getId())) {
            conn = nodes.get(nodeConfig.getId());
        } else {
            try {
                conn = getConnectionFromPool(nodeConfig);
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
                frontConn.error(ErrorCode.ER_NET_FCNTL_ERROR,
                        "get backend connection error " + e.getLocalizedMessage());
                return;
            }
        }

        conn.setSchema(nodeConfig.getDatabase());
        conn.setExecutePacket(data);
        conn.setSession(this);
        conn.setIndex(0);
        backConns.add(conn);
        frontConn.getProcessor().backendRegister(conn);
    }

    public Map<String, MySQLConnection> getMySQLNodes() {
        return mySQLNodes;
    }

    public List<MySQLConnection> getBackConns() {
        return backConns;
    }

    public RouteResult getRoutes() {
        return routes;
    }

    public RouteHintHandler getRouteHint() {
        return routeHint;
    }

}
