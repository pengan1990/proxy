package conn;

import handler.Handler;
import handler.frontend.AuthHandler;
import mysql.Capabilities;
import mysql.ErrorCode;
import mysql.HandshakePacket;
import mysql.Versions;
import nio.Processor;
import org.apache.log4j.Logger;
import util.CharsetUtil;
import util.RandomUtil;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by pengan on 16-9-23.
 */
public class ServerConnection extends AbstractConnection {
    private static final Logger logger = Logger.getLogger(ServerConnection.class);
    private volatile boolean isTransaction;
    private byte[] seed;
    private int charsetIndex = CharsetUtil.getIndex(CHARSET);
    private long connectionId;
    private int port;
    private String schema;
    private String user;
    private Handler handler;

    public ServerConnection(SocketChannel client, Processor processor) throws IOException {
        super(client, processor);
        this.isTransaction = false;
        this.port = client.socket().getLocalPort();
        this.handler = new AuthHandler(this);
        this.readBuffer = allocate();
    }

    @Override
    public void register(Selector selector) throws IOException {
        logger.debug("register");
        super.register(selector);
        logger.debug("send handshake package");
        byte[] rand1 = RandomUtil.randomBytes(8);
        byte[] rand2 = RandomUtil.randomBytes(12);

        // 保存认证数据
        byte[] seed = new byte[rand1.length + rand2.length];
        System.arraycopy(rand1, 0, seed, 0, rand1.length);
        System.arraycopy(rand2, 0, seed, rand1.length, rand2.length);
        this.seed = seed;

        // 发送握手数据包
        HandshakePacket hs = new HandshakePacket();
        hs.packetId = 0;
        hs.protocolVersion = Versions.PROTOCOL_VERSION;
        hs.serverVersion = Versions.SERVER_VERSION;
        hs.threadId = connectionId;
        hs.seed = rand1;
        hs.serverCapabilities = getServerCapabilities();
        hs.serverCharsetIndex = (byte) (charsetIndex & 0xff);
        hs.serverStatus = 2;
        hs.restOfScrambleBuff = rand2;
        hs.write(this);
    }

    @Override
    protected void handle(byte[] data) throws IOException {
        try {
            if (session == null) {
                // session not create yet
                handler.handle(data);
            } else {
                session.getCommandHandler().handle(data);
            }
        } catch (Throwable exp) {
            exp.printStackTrace();
            logger.error(exp.getLocalizedMessage());
            writeErrMessage(ErrorCode.ER_YES, exp.getLocalizedMessage());
        }
    }

    protected int getServerCapabilities() {
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
        // flag |= ServerDefs.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        return flag;
    }

    public void error(int errorCode, String msg) {
        logger.error(errorCode + ":" + msg);
        writeErrMessage((byte) 1, errorCode, msg);
        switch (errorCode) {
            // no need to kill connection just table not exist
            case ErrorCode.ER_TABLE_EXISTS_ERROR:
            case ErrorCode.ER_BAD_DB_ERROR:
                break;
            default:
                logger.warn("close in error");
                close();
        }
    }

    /**
     * check write buffer
     * reason: much write stored in buffer not send forward
     *
     * @return
     */
    public boolean checkWriteBuffer() {
        logger.debug("checkWriteBuffer");
        return writeBufferQueue.size() > DEFAULT_WRITE_QUEUE_SIZE;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public int getCharsetIndex() {
        return charsetIndex;
    }

    public void setCharsetIndex(int charsetIndex) {
        this.charsetIndex = charsetIndex;
        this.charset = CharsetUtil.getCharset(charsetIndex);
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    public int getPort() {
        return port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public void setTransaction(boolean transaction) {
        isTransaction = transaction;
    }
}
