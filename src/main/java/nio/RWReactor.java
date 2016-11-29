package nio;

import conn.AbstractConnection;
import conn.MySQLConnection;
import conn.ServerConnection;
import mysql.ErrorCode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by pengan on 16-9-23.
 */
public class RWReactor {
    private static final Logger logger = Logger.getLogger(RWReactor.class);
    private ReadReactor read;
    private WriteReactor write;

    public RWReactor(String name) throws IOException {
        this.read = new ReadReactor(name + "-R");
        this.write = new WriteReactor(name + "-W");
    }

    public void start() {
        logger.debug("start");
        this.read.start();
        this.write.start();
    }

    public void register(AbstractConnection conn) {
        logger.debug("handleQuery");
        read.readQueue.offer(conn);
        read.selector.wakeup();
    }

    /**
     * just in the present thread so no need lock
     *
     * @param conn
     * @throws ClosedChannelException
     */
    public void backendRegister(MySQLConnection conn) throws IOException {
        logger.debug("backendRegister");
        conn.register(read.selector);
        read.selector.wakeup();
    }

    class ReadReactor extends Thread {
        private BlockingQueue<AbstractConnection> readQueue;
        private Selector selector;

        public ReadReactor(String name) throws IOException {
            setName(name);
            selector = Selector.open();
            readQueue = new LinkedBlockingQueue<AbstractConnection>();
        }

        @Override
        public void run() {
            logger.debug("run");
            Selector selector = this.selector;
            while (true) {
                try {
                    selector.select(1000L);
                    register();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    try {
                        for (SelectionKey key : keys) {
                            Object attach = key.attachment();
                            if (attach != null && key.isValid()) {
                                if (key.isReadable()) {
                                    read(key);
                                } else if (key.isWritable()) {
                                    writeByEvent(key);
                                } else {
                                    logger.warn("cancel key");
                                    key.cancel();
                                }
                            } else {
                                logger.warn("cancel key");
                                key.cancel();
                            }
                        }
                    } finally {
                        keys.clear();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error(e.getLocalizedMessage());
                }
            }
        }
    }

    private void register() {
        AbstractConnection conn = null;
        while ((conn = read.readQueue.poll()) != null) {
            try {
                conn.register(read.selector);
            } catch (IOException e) {
                error(ErrorCode.ER_NET_FCNTL_ERROR, e.getLocalizedMessage(), conn);
            }
        }
    }

    private void writeByEvent(SelectionKey key) {
        logger.debug("writeByEvent");
        AbstractConnection conn = (AbstractConnection) key.attachment();
        try {
            conn.writeByEvent();
        } catch (IOException e) {
            error(ErrorCode.ER_NET_ERROR_ON_WRITE, "writeToChannel by event error " + e.getLocalizedMessage(), conn);
        }
    }

    private void read(SelectionKey key) {
        logger.debug("read");
        AbstractConnection conn = (AbstractConnection) key.attachment();
        try {
            conn.read();
        } catch (IOException e) {
            error(ErrorCode.ER_NET_READ_ERROR, e.getLocalizedMessage(), conn);
        }
    }

    class WriteReactor extends Thread {
        BlockingQueue<AbstractConnection> writeQueue;

        public WriteReactor(String name) {
            setName(name);
            this.writeQueue = new LinkedBlockingQueue<AbstractConnection>();
        }

        @Override
        public void run() {
            logger.debug("run");
            AbstractConnection conn = null;
            while (true) {
                try {
                    if ((conn = writeQueue.take()) != null) {
                        try {
                            writeByQueue(conn);
                        } catch (IOException e) {
                            String errMsg = "write by queue error " + e.getLocalizedMessage();
                            error(ErrorCode.ER_NET_ERROR_ON_WRITE, errMsg, conn);
                        } catch (Throwable exp) {
                            exp.printStackTrace();
                            System.err.println(exp.getLocalizedMessage());
                            error(ErrorCode.ER_ERROR_ON_WRITE, exp.getLocalizedMessage(), conn);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error(e.getLocalizedMessage());
                }
            }
        }
    }

    public void offerWrite(AbstractConnection conn) {
        logger.debug("offerWrite");
        write.writeQueue.offer(conn);
    }

    private void writeByQueue(AbstractConnection conn) throws IOException {
        logger.debug("writeByQueue");
        conn.writeByQueue();
    }

    private void error(int errCode, String errMsg, AbstractConnection conn) {
        logger.error(errMsg);
        try {
            if (!conn.isClosed()) {
                // connection is not closed
                if (conn instanceof ServerConnection) {
                    conn.writeErrMessage(errCode, errMsg);
                    conn.close();
                } else {
                    conn.close();
                    // 通知前段连接后端写入异常, 注意所有连接都必须先注册后开始读写 异常直接关闭
                }
            }
        } catch (Throwable exp) {
            logger.warn("close connection error " +
                    exp.getLocalizedMessage());
        }
    }
}
