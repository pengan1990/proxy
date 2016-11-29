package conn;

import handler.Handler;
import handler.SessionHandler;
import handler.backend.MySQLTrxHandler;
import mysql.Capabilities;
import mysql.CommandPacket;
import mysql.ErrorCode;
import mysql.MySQLPacket;
import nio.Processor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by pengan on 16-9-23.
 *
 * @MySQLConnection
 */
public class MySQLConnection extends AbstractConnection {

    private static final Logger logger = Logger.getLogger(MySQLConnection.class);
    private static final long MAX_BUFFER_SIZE = 16;
    public static final long MAX_PACKET_SIZE = 1024 * 1024 * 16;
    public static final long CLIENT_FLAGS = initClientFlags();
    // handler state
    private static final int HANDLER_STATE_AUTH_START = 0;
    private static final int HANDLER_STATE_AUTH_END = 1;
    private static final int HANDLER_STATE_INIT = 2;
    private static final int HANDLER_STATE_EXECUTE = 3;

    // packet state
    public static final int PACKET_STATE_FIELD_HEADER = 0;
    public static final int PACKET_STATE_FIELD = 1;
    public static final int PACKET_STATE_ROW = 2;
    public static final int PACKET_STATE_ROW_EOF = 3;

    private int index;
    private long threadId;
    private int handlerState;
    private int packetState; // for query command
    private boolean isSchemaChanged;
    private String schema;
    private boolean isFieldListCommand;
    private boolean isNewlyCreated; // 新创建的连接 或者是老的连接
    private byte[] header;
    private List<byte[]> fields;
    private List<CommandPacket> initCommands;
    private MySQLConnectionPool pool;
    private CommandPacket executeCommand;

    public MySQLConnection(MySQLConnectionPool pool, Processor processor) throws IOException {
        // 需要创建socket 然后开始挂在到 selector
        super(SocketChannel.open(new InetSocketAddress(pool.getInstance().getHost(),
                pool.getInstance().getPort())), processor);
        this.readBuffer = processor.getBufferPool().allocate();
        this.pool = pool;
        this.handlerState = HANDLER_STATE_AUTH_START;
        this.packetState = PACKET_STATE_FIELD_HEADER;
        this.isFieldListCommand = false;
        this.isNewlyCreated = true; //
        this.isSchemaChanged = false;
        this.initCommands = new LinkedList<CommandPacket>();
        this.fields = new LinkedList<byte[]>();
    }

    /**
     * if is newly created then
     * from writeToChannel to writeToChannel sql
     * else
     *
     * @param data
     * @throws IOException
     */
    @Override
    protected void handle(byte[] data) throws IOException {
        logger.debug("handle");
        SessionHandler handler = getSession().getHandler();
        try {
            switch (this.handlerState) {
                case HANDLER_STATE_AUTH_START:
                    handler.getAuthHandler().write(data, this); // write encrypt password to server
                    this.handlerState = HANDLER_STATE_AUTH_END;
                    break;
                case HANDLER_STATE_AUTH_END:
                    handler.getAuthHandler().read(data, this); // an ok packet returned
                    this.handlerState = HANDLER_STATE_INIT;
                    handler.getInitHandler().write(null, this);
                    break;
                case HANDLER_STATE_INIT:
                    handler.getInitHandler().read(data, this);
                    if (initCommands.size() > 0) {
                        // keep write init commands to MySQL server
                        handler.getInitHandler().write(null, this);
                        break;
                    }

                    if (isFieldListCommand) {
                        // if field list command
                        handler.getFieldListHandler().write(null, this);
                    } else {
                        // sql command
                        handler.getExecuteHandler().write(null, this);
                    }
                    this.handlerState = HANDLER_STATE_EXECUTE;
                    break;
                case HANDLER_STATE_EXECUTE:
                    if (isFieldListCommand) {
                        handler.getFieldListHandler().read(data, this);
                    } else {
                        handler.getExecuteHandler().read(data, this);
                    }
                    break;
                default:
                    logger.error("MySQLConnection handlerState error " + handlerState);
                    handler.getErrorHandler().handleError(data, this);
                    break;
            }
        } catch (Throwable exp) {
            // if any exception occur let the front know and start to clean
            handler.getErrorHandler().handleException(ErrorCode.ER_YES, exp, this);
        }
    }

    public void setExecuteSQL(StringBuilder sql) {
        logger.debug("setExecuteSQL");
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = sql.toString().getBytes();
        this.executeCommand = cmd;
        this.isFieldListCommand = false;
    }

    public void setExecutePacket(byte[] data) {
        logger.debug("setExecutePacket");
        CommandPacket cmd = new CommandPacket();
        cmd.read(data);
        this.executeCommand = cmd;
        this.isFieldListCommand = true;
    }

    public void returnToPool() {
        logger.debug("returnToPool");
        // return to connection pool
        reset();
        pool.returnObject(this);
    }

    /**
     * no need to set handlerState = HANDLER_STATE_EXECUTE
     * if previous execute is ok
     */
    public void commit() {
        logger.debug("commit");
        this.executeCommand = MySQLTrxHandler.COMMIT;
        execute();
    }

    /**
     * no need to set handlerState = HANDLER_STATE_EXECUTE
     * if previous execute is ok
     */
    public void rollback() {
        logger.debug("rollback");
        this.executeCommand = MySQLTrxHandler.ROLLBACK;
        execute();
    }

    public void execute() {
        logger.debug("execute");
        CommandPacket exeCmd = this.executeCommand;
        int idx = this.index;
        clear();
        executeCommand = exeCmd;
        index = idx;
        handlerState = HANDLER_STATE_EXECUTE;
        session.getHandler().getExecuteHandler().write(null, this);
    }

    @Override
    public void register(Selector selector) throws IOException {
        // here register then send command to mysql server
        super.register(selector);
        logger.debug(this + " schema is " + getSchema());
        if (!isNewlyCreated) {
            logger.debug("connection is not newly created");
            // TODO: 16-11-5 each time get from pool then clear previous buffer
            remainBuffer = null;
            writeBufferQueue.clear();
        }

        if (initCommands.size() == 0) {
            // if not init commands initiated
            initCommands();
        }
        if (!isNewlyCreated) {
            // not newly created
            if (initCommands.size() > 0) {
                // have init commands to send
                session.getHandler().getInitHandler().write(null, this);
                return;
            }
            // send command to backend
            handlerState = HANDLER_STATE_EXECUTE;
            // handler state change from HANDLER_STATE_INIT to HANDLER_STATE_EXECUTE
            if (isFieldListCommand) {
                // if field list command
                session.getHandler().getFieldListHandler().write(null, this);
            } else {
                // execute sql command
                execute();
            }
        }
    }

    /**
     */
    @Override
    public void close() {
        logger.debug("close");
        pool.decrementActiveNumAndGet();
        clear();
        super.close();
    }

    /**
     * 放回连接池操作
     */
    @Override
    public void reset() {
        logger.debug("reset");
        clear();
        super.reset();
    }

    @Override
    public void read() throws IOException {
        logger.debug("schema is " + schema);
        if (session.getFrontConn().checkWriteBuffer() ||
                (session.getRouteHint().isComplexQuery() &&
                        session.getHandler().getBackRows()[index].size() > MAX_BUFFER_SIZE)) {
            logger.warn("read for the next time connection @" + this + ":" + schema);
            return;
        }
        super.read();
    }

    private void clear() {
        this.index = 0;
        this.packetState = PACKET_STATE_FIELD_HEADER;
        this.handlerState = HANDLER_STATE_INIT;
        this.isFieldListCommand = false;
        this.isNewlyCreated = false;
        this.header = null;
        this.fields.clear();
        this.initCommands.clear();
        this.executeCommand = null;
    }

    private void initCommands() {
        logger.debug("initCommands");
        // if connection is newly created then add init commands
        // else just check schema change
        if (isNewlyCreated) {
            // newly create then get command
            initCommands.add(getSqlModeCommand());
            initCommands.add(getCharsetCommand());
        }
        if (isSchemaChanged) {
            // schema changed then use db
            initCommands.add(getUseDbCommand()); // 如果从连接池当中拿出来 那么不需要重新发送
        }

        if (getSession().getFrontConn().isTransaction()) {
            // front is in transaction
            initCommands.add(MySQLTrxHandler.BEGIN);
        }
    }

    private CommandPacket getUseDbCommand() {
        StringBuilder command = new StringBuilder();
        command.append(schema);
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_INIT_DB;
        cmd.arg = command.toString().getBytes();
        return cmd;
    }

    private CommandPacket getCharsetCommand() {
        String charset = getCharset();
        StringBuilder s = new StringBuilder();
        s.append("SET names ").append(charset);
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    private CommandPacket getSqlModeCommand() {
        StringBuilder s = new StringBuilder();
        s.append("SET sql_mode=\"").append(pool.getInstance().getSqlMode()).append('"');
        CommandPacket cmd = new CommandPacket();
        cmd.packetId = 0;
        cmd.command = MySQLPacket.COM_QUERY;
        cmd.arg = s.toString().getBytes();
        return cmd;
    }

    private static long initClientFlags() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= Capabilities.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        // client extension
        // flag |= Capabilities.CLIENT_MULTI_STATEMENTS;
        // flag |= Capabilities.CLIENT_MULTI_RESULTS;
        return flag;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public List<byte[]> getFields() {
        return fields;
    }

    public void addField(byte[] field) {
        this.fields.add(field);
    }

    public List<CommandPacket> getInitCommands() {
        return initCommands;
    }

    public int getPacketState() {
        return packetState;
    }

    public void setPacketState(int packetState) {
        this.packetState = packetState;
    }

    public void setNewlyCreated(boolean newlyCreated) {
        logger.debug("setNewlyCreated");
        isNewlyCreated = newlyCreated;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        if (this.schema == null || !this.schema.equals(schema)) {
            this.isSchemaChanged = true;
        }
        this.schema = schema;
    }

    public MySQLConnectionPool getPool() {
        return pool;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getThreadId() {
        return threadId;
    }

    public Handler getHandler() {
        return handler;
    }

    public CommandPacket getExecuteCommand() {
        return executeCommand;
    }
}
