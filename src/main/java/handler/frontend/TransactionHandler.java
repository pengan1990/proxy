package handler.frontend;

import conn.MySQLConnection;
import conn.NonBlockSession;
import conn.ServerConnection;
import org.apache.log4j.Logger;

/**
 * Created by pengan on 16-11-22.
 */
public class TransactionHandler {
    private static final Logger logger = Logger.getLogger(TransactionHandler.class);

    public static void begin(ServerConnection conn) {
        logger.debug("begin");
        conn.setTransaction(true);
        conn.writeOkPacket();
    }

    /**
     * return true : 表示需要拦截
     * return false： 表示需要往后端发送
     *
     * @param peerConn
     * @return
     */
    public static boolean rollback(ServerConnection peerConn, StringBuilder sql) {
        logger.debug("rollback");
        NonBlockSession session = peerConn.getSession();
        // 判断 如果前端 不是事务 直接返回ok包
        if (!peerConn.isTransaction() ||
                session.getMySQLNodes().size() == 0) {
            peerConn.writeOkPacket();
            return true;
        }
        // if it is transaction
        peerConn.setTransaction(false);
        session.getRouteHint().reset();
        /**
         * backConnections are holding current using connections
         */
        session.getBackConns().clear();
        session.getBackConns().addAll(session.getMySQLNodes().values());

        /**
         * reset handler to clear all buffer
         */
        session.getHandler().reset(session.getMySQLNodes().size());
        for (MySQLConnection conn : session.getMySQLNodes().values()) {
            conn.rollback();
        }
        return true;
    }

    /**
     * return true : 表示需要拦截
     * return false： 表示需要往后端发送
     *
     * @param peerConn
     * @return
     */
    public static boolean commit(ServerConnection peerConn, StringBuilder sql) {
        logger.debug("commit");
        NonBlockSession session = peerConn.getSession();
        // 判断 如果前端 不是事务 直接返回ok包
        if (!peerConn.isTransaction() ||
                session.getMySQLNodes().size() == 0) {
            peerConn.writeOkPacket();
            return true;
        }
        // if it is transaction: commit all back connections
        peerConn.setTransaction(false);
        session.getRouteHint().reset();
        /**
         * backConnections are holding current using connections
         */
        session.getBackConns().clear();
        session.getBackConns().addAll(session.getMySQLNodes().values());

        /**
         * reset handler to clear all buffer
         */
        session.getHandler().reset(session.getMySQLNodes().size());
        for (MySQLConnection conn : session.getMySQLNodes().values()) {
            conn.commit();
        }
        return true;
    }

    public static final void commit(NonBlockSession session) {
        logger.debug("commit current using back connections ");
        if (!session.getHandler().isTransaction()) {
            // if back connections are not in transaction just return
            return;
        }
        session.getHandler().setTransaction(false);
        session.getHandler().reset(session.getBackConns().size());
        for (MySQLConnection conn : session.getBackConns()) {
            conn.commit();
        }
    }

    public static final void rollback(NonBlockSession session) {
        logger.debug("commit current using back connections ");
        if (!session.getHandler().isTransaction()) {
            // if back connections are not in transaction just return
            return;
        }
        session.getHandler().setTransaction(false);
        session.getHandler().reset(session.getBackConns().size());
        for (MySQLConnection conn : session.getBackConns()) {
            conn.rollback();
        }
    }
}
