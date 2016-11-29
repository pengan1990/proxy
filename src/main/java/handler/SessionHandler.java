package handler;

import conn.MySQLConnection;
import conn.NonBlockSession;
import conn.ServerConnection;
import handler.backend.*;
import handler.frontend.TransactionHandler;
import mysql.FieldPacket;
import mysql.RowDataPacket;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by pengan on 16-10-13.
 * <p/>
 * one session one session handler
 * <p/>
 * all these handler are operate in one processor
 */
public class SessionHandler {
    private static final Logger logger = Logger.getLogger(SessionHandler.class);

    private volatile boolean isTransaction;
    private byte packetId;
    private volatile boolean isFieldPacketHandled = false;
    private FieldPacket fieldPacket = new FieldPacket();
    private byte[] error; // only reserve one error data info
    private List<FieldPacket> fields;
    private AtomicInteger counter;
    private ByteBuffer buffer;
    private KMergeTree kMTree;
    private List<RowDataPacket>[] backRows; // backend mysql connection row datas

    // share all the handlers below
    private final MySQLConnectionAuthHandler authHandler = new MySQLConnectionAuthHandler();
    private final MySQLConnectionInitHandler initHandler = new MySQLConnectionInitHandler();
    private final MySQLConnectionExecuteHandler executeHandler = new MySQLConnectionExecuteHandler();

    private final MySQLConnectionFieldListHandler fieldListHandler = new MySQLConnectionFieldListHandler();
    private final MySQLOKHandler okHandler = new MySQLOKHandler();
    private final MySQLFieldEOFHandler fieldEOFHandler = new MySQLFieldEOFHandler();
    private final MySQLRowHandler rowHandler = new MySQLRowHandler();
    private final MySQLRowEOFHandler rowEOFHandler = new MySQLRowEOFHandler();
    private final MySQLErrorHandler errorHandler = new MySQLErrorHandler();
    private final MySQLTrxHandler trxHandler = new MySQLTrxHandler();

    private NonBlockSession session;

    public SessionHandler(NonBlockSession session) {
        this.isTransaction = false;
        this.session = session;
        this.buffer = session.getFrontConn().allocate();
        this.counter = new AtomicInteger(0);
        this.fields = new LinkedList<FieldPacket>();
        this.kMTree = new KMergeTree(session);
    }

    /**
     * to write byte
     *
     * @param data
     * @param endWrite
     */
    public void write(byte[] data, boolean endWrite) {
        ByteBuffer writeBuffer = buffer;
        ServerConnection front = session.getFrontConn();
        data[3] = incrementAndGetPacketId();
        writeBuffer = front.writeToBuffer(data, writeBuffer);
        buffer = writeBuffer;
        if (endWrite) {
            front.write(writeBuffer);
        }
    }

    /**
     * to write row data packet
     *
     * @param row
     * @param delete
     * @param endWrite
     */
    public void write(RowDataPacket row, Set<Integer> delete, boolean endWrite) {
        logger.debug("write");
        ByteBuffer writeBuffer = buffer;
        ServerConnection front = session.getFrontConn();
        row.packetId = incrementAndGetPacketId();
        writeBuffer = row.write(writeBuffer, front, delete);
        buffer = writeBuffer;
        if (endWrite) {
            front.write(writeBuffer);
        }
    }

    /**
     * for field list command
     *
     * @param data
     * @param endWrite
     * @throws UnsupportedEncodingException
     */
    public void writeField(byte[] data, boolean endWrite) throws UnsupportedEncodingException {
        fieldPacket.read(data);
        ByteBuffer writeBuffer = buffer;
        ServerConnection front = session.getFrontConn();
        fieldPacket.db = front.getSchema().getBytes(front.getCharset());
        fieldPacket.packetId = incrementAndGetPacketId();
        writeBuffer = fieldPacket.write(writeBuffer, front);
        buffer = writeBuffer;
        if (endWrite) {
            front.write(writeBuffer);
        }
    }

    public void reset(int nodeNumber) {
        logger.debug("reset");
        packetId = 0;
        kMTree.reset();
        buffer.clear();
        fields.clear();
        isFieldPacketHandled = false;
        counter.set(nodeNumber);
        error = null;
        if (backRows != null) {
            for (List<RowDataPacket> rows : backRows) {
                rows.clear();
            }
        }
        backRows = null;
        if (session.getRouteHint().isComplexQuery() && nodeNumber > 0) {
            backRows = new List[nodeNumber];
            for (int index = 0; index < nodeNumber; index++) {
                backRows[index] = new LinkedList<RowDataPacket>();
            }
            kMTree.init(nodeNumber);
        }
    }

    public void saveError(byte[] data) {
        logger.debug("saveError");
        if (error == null) {
            error = data;
        }
    }

    /**
     * terminate all connection execute then return
     * <p/>
     * if io exception then call this
     */
    public void terminate() {
        logger.debug("terminate");
        Map<String, MySQLConnection> nodes = session.getMySQLNodes();
        for (Map.Entry<String, MySQLConnection> entry : nodes.entrySet()) {
            MySQLConnection conn = entry.getValue();
            if (!conn.isUsing()) {
                conn.returnToPool();
                continue;
            }
            logger.debug("close for connection is used");
            conn.close();
        }
        nodes.clear();
    }

    /**
     * notify all mysql connection to stop
     * <p/>
     * if sql exception then call this
     * <p/>
     * all connection have to return to pool rather than to hold
     * <p/>
     * <p/>
     * problem: 多个data packet to handle but here just handle one packet
     *
     * @throws Exception： no need to worry about transaction if is in transaction
     *                    <p/>
     *                    then connection cannot be return to pool to to be close
     *                    <p/>
     *                    it means field variable @isUsing == true always true
     */
    public void interrupt() {
        logger.debug("interrupt");
        List<MySQLConnection> conns = session.getBackConns();
        Map<String, MySQLConnection> nodes = session.getMySQLNodes();
        for (MySQLConnection conn : conns) {
            try {
                if (!conn.isUsing()) {
                    logger.debug("connection " + conn.getSchema() + " return to pool");
                    conn.returnToPool();
                    continue;
                }
                logger.debug("connection " + conn.getSchema() + " close");
                conn.close();
            } finally {
                nodes.remove(conn.getSchema());
            }
        }
        isTransaction = false; // 事务处理异常 直接释放连接 不进行rollback 因为有链接已经出错
        session.getFrontConn().setTransaction(false);
        reset(0);
    }

    /**
     * normal : connection return to pool, counter --
     * <p/>
     *
     * @param data eof or error or ok packet
     * @param conn mysql connection
     * @throws IOException
     */
    public void decrease(byte[] data, MySQLConnection conn) throws Exception {
        // all error or ok have to call this
        logger.debug("decrease");
        /**
         * @question: may other connection have no begin yet?
         *
         * three way to solve this :
         * hold the connection not to recycle or move data to another place
         * if not calling rows.clear() in @MySQLConnection
         * while this connection used for other Session then error occurred
         *
         * finally: decide to move data to another place because a task for this @para conn is over
         */
        int connIndex = conn.getIndex();
        recycleBackConnection(conn);
        RouteHintHandler routeHint = session.getRouteHint();
        if (!haveError() && routeHint.isComplexQuery() &&
                (routeHint.getGroupByColumnSet().size() != 0 ||
                routeHint.getOrderByColumns().size() != 0 ||
                routeHint.getLimit()[0] != routeHint.getLimit()[1])) {
            // group by & order by limit using kMerge
            finish(connIndex);
        }
        if (counter.decrementAndGet() == 0) {
            // all route are returned, data must be eof or ok packet
            if (isTransaction && !session.getFrontConn().isTransaction()) {
                if (error != null) {
                    // error occur and backend is transaction and front end is not transactions
                    TransactionHandler.rollback(session);
                    return;
                } else {
                    // error is null back connection is transaction
                    TransactionHandler.commit(session);
                    return;
                }
            }
            if (error != null) {
                data = error;
            }
            write(data, true);
        }
    }

    private void recycleBackConnection(MySQLConnection conn) {
        logger.debug("recycleBackConnection:" + conn.getSchema());
        if (session.getFrontConn().isTransaction() || isTransaction) {
            logger.debug("operation is in transaction all connection to hold");
            return;
        }
        Map<String, MySQLConnection> nodes = session.getMySQLNodes();
        List<MySQLConnection> backConns = session.getBackConns();
        nodes.remove(conn.getSchema());
        backConns.remove(conn); // only to remove object but not index
        conn.returnToPool();
    }

    public void finish(int index) throws Exception {
        backRows[index].add(KMergeTree.LOSER);
        if (backConnReadyCheck()) {
            kMTree.refresh();
            kMTree.kMerge(); // 这里可以不排序 可以放到最后去排
        }
    }

    /**
     * check back mysql connection is ready for sort
     * : at least one row in row list
     *
     * @return
     */
    public boolean backConnReadyCheck() {
        logger.debug("backConnReadyCheck");
        for (List<RowDataPacket> rows : backRows) {
            if (rows.size() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * have error  => error != null => return true
     * no error => error == null =>  return false
     * @return
     */
    public boolean haveError() {
        logger.debug("haveError");
        return error != null;
    }

    public List<RowDataPacket>[] getBackRows() {
        return backRows;
    }

    public boolean addFieldPacket(FieldPacket field) {
        return fields.add(field);
    }

    public List<FieldPacket> getFields() {
        return fields;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public byte incrementAndGetPacketId() {
        return ++packetId;
    }

    public byte getPacketId() {
        return packetId;
    }

    public boolean isFieldPacketHandled() {
        return isFieldPacketHandled;
    }

    public void setFieldPacketHandled(boolean fieldPacketHandled) {
        isFieldPacketHandled = fieldPacketHandled;
    }

    public MySQLConnectionFieldListHandler getFieldListHandler() {
        return fieldListHandler;
    }

    public MySQLFieldEOFHandler getFieldEOFHandler() {
        return fieldEOFHandler;
    }

    public MySQLRowHandler getRowHandler() {
        return rowHandler;
    }

    public MySQLRowEOFHandler getRowEOFHandler() {
        return rowEOFHandler;
    }

    public MySQLOKHandler getOkHandler() {
        return okHandler;
    }

    public MySQLErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public MySQLConnectionAuthHandler getAuthHandler() {
        return authHandler;
    }

    public MySQLConnectionInitHandler getInitHandler() {
        return initHandler;
    }

    public MySQLConnectionExecuteHandler getExecuteHandler() {
        return executeHandler;
    }

    public MySQLTrxHandler getTrxHandler() {
        return trxHandler;
    }

    public KMergeTree getkMTree() {
        return kMTree;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public void setTransaction(boolean transaction) {
        isTransaction = transaction;
    }
}
