package conn;

import handler.Handler;
import mysql.ErrorPacket;
import mysql.OKPacket;
import nio.Processor;
import org.apache.log4j.Logger;
import util.CharsetUtil;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pengan on 16-9-23.
 */
public abstract class AbstractConnection {
    private static final Logger logger = Logger.getLogger(AbstractConnection.class);

    public static final int PACKET_HEAD_SIZE = 4;
    private static final int MAX_PACKET_SIZE = 16 * 1024 * 1024; // 16M
    private static final int SOCKET_RECV_BUFFER_SIZE = 128 * 1024;
    private static final int SOCKET_SEND_BUFFER_SIZE = 64 * 1024;
    protected static final int DEFAULT_WRITE_QUEUE_SIZE = 16;

    protected static final String CHARSET = "utf8";

    protected volatile boolean isUsing; // 这个用来判断是否放回连接池 kill 的时候
    private volatile int readBufferOffset;
    protected final AtomicBoolean isClosed;
    protected String charset = CHARSET;
    protected int charsetIndex;
    private Lock lock;
    protected ByteBuffer readBuffer;
    protected ByteBuffer remainBuffer;
    protected Queue<ByteBuffer> writeBufferQueue;
    protected SocketChannel client;
    protected SelectionKey clientKey;
    protected Processor processor;
    protected NonBlockSession session;
    protected Handler handler;

    public AbstractConnection(SocketChannel client, Processor processor) throws IOException {
        this.processor = processor;
        this.writeBufferQueue = new LinkedList<ByteBuffer>();
        this.client = client;
        this.lock = new ReentrantLock();
        setSocketOpts();
        this.isClosed = new AtomicBoolean(false);
        this.isUsing = true;
    }

    public void register(Selector selector) throws IOException {
        logger.debug("register");
        this.isUsing = true;
        this.clientKey = client.register(selector, SelectionKey.OP_READ, this);
    }

    public void unregister() {
        logger.debug("unregister");
        if (clientKey != null && clientKey.isValid()) {
            clientKey.attach(null);
            clientKey.cancel();
        }
    }

    /**
     * 不需要担心 interrupt之后 再次来读取数据 引发socket io exception
     * 因为 socket isUsing then close or else we put into connection pool
     * <p/>
     * here may be another problem that the read buffer have not clear then put it into connection pool
     * <p/>
     * is is impossible to appear @<reset> is to clear read buffer
     * and if it is unregistered then will not keep working
     *
     * @throws IOException
     */
    public void read() throws IOException {
        logger.debug("read");
        if (client == null || !client.isRegistered() || client.socket().isClosed()) {
            return;
        }
        ByteBuffer buffer = this.readBuffer;
        int got = client.read(buffer);
        if (got < 0) {
            logger.error("got == -1");
            throw new EOFException("got == -1");
        }

        if (got == 0) {
            logger.warn("readFromChannel, got == 0");
        }

        // 处理数据
        int offset = readBufferOffset, length = 0, position = buffer.position();
        while (isUsing) {
            length = getPacketLength(buffer, offset);
            if (length == -1) {// 未达到可计算数据包长度的数据
                if (!buffer.hasRemaining()) {
                    checkReadBuffer(buffer, offset, position);
                }
                break;
            }
            if (position >= offset + length) {
                // 提取一个数据包的数据进行处理
                buffer.position(offset);
                byte[] data = new byte[length];
                buffer.get(data, 0, length);

                handle(data);

                // 设置偏移量
                offset += length;
                if (position == offset) {// 数据正好全部处理完毕
                    if (readBufferOffset != 0) {
                        readBufferOffset = 0;
                    }
                    buffer.clear();
                    break;
                } else {// 还有剩余数据未处理
                    readBufferOffset = offset;
                    buffer.position(position);
                    continue;
                }
            } else {// 未到达一个数据包的数据
                if (!buffer.hasRemaining()) {
                    checkReadBuffer(buffer, offset, position);
                }
                break;
            }
        }
    }

    // 开始处理数据包
    protected abstract void handle(byte[] data) throws IOException;

    // writeToChannel 先写到队列当中 对外部开发就这个接口
    public void write(ByteBuffer buffer) {
        writeBufferQueue.offer(buffer);
        // 这里需要唤醒 reactor线程 放到reactor 的写队列当中
        processor.getReactor().offerWrite(this);
    }


    /**
     * @return true 需要打开事件写 false 不需要打开
     * @throws IOException
     */
    private boolean write() throws IOException {
        logger.debug("write");
        if (isClosed.get()) {
            // if closed then just return
            return false;
        }
        if (this.remainBuffer != null) {
            // 上次没写完 现在继续写
            client.write(remainBuffer);
            if (remainBuffer.hasRemaining()) {
                // 这次还没写完
                return true;
            }
            // 已经写完 置空
            processor.getBufferPool().recycle(remainBuffer);
            remainBuffer = null;
        }

        if (this.writeBufferQueue.isEmpty()) {
            // 队列当中没有任何东西要写
            return false;
        }
        ByteBuffer buffer = this.writeBufferQueue.poll();
        buffer.flip(); // 注意这里必须要flip 否则写出的数据都是0

        client.write(buffer);
        if (buffer.hasRemaining()) {
            // 显然没有写完
            remainBuffer = buffer;
            return true;
        }
        processor.getBufferPool().recycle(buffer);
        // 当前buffer 已经写完
        if (this.writeBufferQueue.isEmpty()) {
            // 已经写完了
            return false;
        }
        // 队列当中还有 需要打开事件 写
        return true;
    }

    // writeToChannel method one buffer for once
    public void writeByQueue() throws IOException {
        if (client == null || !client.isRegistered() || client.socket().isClosed() || !isUsing) {
            // 这里并不需要操作 因为close 已经帮忙处理
            return;
        }

        // 前提是没有打开event写
        if ((clientKey.interestOps() & SelectionKey.OP_WRITE) != 0) {
            logger.debug("write by event rather than write by queue");
            return;
        }
        // 还没有写完 打开事件写
        if (write()) {
            enableWrite();
        }
    }

    /**
     * 写得考虑多线程问题 因为最开始是直接写到buffer当中 然后调用writeByQueue
     * <p/>
     * writeByQueue 和 writeByEvent 不在同一个线程当中
     * <p/>
     * writeByQueue 只会打开写事件
     * <p/>
     * writeByEvent 关闭写事件
     */
    public void writeByEvent() throws IOException {
        if (write()) {
            // 如果需要打开事件写 那么什么也不用做
            return;
        }
        // 关闭写事件
        disableWrite();
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    public void close() {
        // if not isClosed yet
        logger.debug("close");
        this.isClosed.getAndSet(true);
        this.isUsing = false;
        this.readBufferOffset = 0;
//        processor.getBufferPool().recycle(readBuffer); // 这个不应该回收 搞不好里面还有东西
//        processor.getBufferPool().recycle(remainBuffer);
        this.readBuffer = null;
        this.remainBuffer = null;
        this.writeBufferQueue.clear();
        this.writeBufferQueue = null;
        this.session = null;
        this.handler = null;
        this.processor = null;

        unregister();
        try {
            this.client.socket().close();
        } catch (Throwable exp) {
            // make sure all close
        }

        try {
            this.client.close();
        } catch (Throwable exp) {
            // make sure all close
        }
    }

    protected void reset() {
        logger.debug("reset");
        this.isUsing = false;
        this.isClosed.getAndSet(false);
        this.charset = CHARSET;
        this.charsetIndex = CharsetUtil.getIndex(this.charset);
        this.readBufferOffset = 0;
        this.readBuffer.clear();
        this.session = null;
        this.handler = null;
        this.processor = null;
        unregister();
    }

    protected void enableWrite() {
        logger.debug("enableWrite");
        Lock lock = this.lock;
        lock.lock();
        try {
            clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_WRITE);
        } finally {
            lock.unlock();
        }
    }

    protected void disableWrite() {
        logger.debug("disableWrite");
        Lock lock = this.lock;
        lock.lock();
        try {
            clientKey.interestOps(clientKey.interestOps() & ~SelectionKey.OP_WRITE);
        } finally {
            lock.unlock();
        }
    }

    private void setSocketOpts() throws IOException {
        client.configureBlocking(false);
        client.socket().setReuseAddress(true);
        client.socket().setKeepAlive(true);
        client.socket().setTcpNoDelay(true);
        client.socket().setReceiveBufferSize(SOCKET_RECV_BUFFER_SIZE);
        client.socket().setSendBufferSize(SOCKET_SEND_BUFFER_SIZE);
    }

    protected int getPacketLength(ByteBuffer buffer, int offset) {
        if (buffer.position() < offset + PACKET_HEAD_SIZE) {
            return -1;
        } else {
            int length = buffer.get(offset) & 0xff;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 16;
            return length + PACKET_HEAD_SIZE;
        }
    }

    protected ByteBuffer checkReadBuffer(ByteBuffer buffer, int offset, int position) {
        if (offset == 0) {
            if (buffer.capacity() >= MAX_PACKET_SIZE) {
                throw new IllegalArgumentException("Packet size over the limit.");
            }
            int size = buffer.capacity() << 1;
            size = (size > MAX_PACKET_SIZE) ? MAX_PACKET_SIZE : size;
            ByteBuffer newBuffer = ByteBuffer.allocate(size);
            buffer.position(offset);
            newBuffer.put(buffer);

            // recycle current buffer
            processor.getBufferPool().recycle(buffer);

            readBuffer = newBuffer;
            return newBuffer;
        } else {
            buffer.position(offset);
            buffer.compact();
            readBufferOffset = 0;
            return buffer;
        }
    }

    public ByteBuffer checkWriteBuffer(ByteBuffer buffer, int capacity) {
        if (capacity > buffer.remaining()) {
            write(buffer);
            return processor.getBufferPool().allocate();
        } else {
            return buffer;
        }
    }

    public void writeOkPacket() {
        ByteBuffer buffer = writeToBuffer(OKPacket.OK_PACKET, allocate());
        write(buffer);
    }

    // send data directly to writeToChannel buffer
    public void write(byte[] data) {
        ByteBuffer buffer = writeToBuffer(data, allocate());
        write(buffer);
    }

    public ByteBuffer writeToBuffer(byte[] src, ByteBuffer buffer) {
        int offset = 0;
        int length = src.length;
        int remaining = buffer.remaining();
        while (length > 0) {
            if (remaining >= length) {
                buffer.put(src, offset, length);
                break;
            } else {
                buffer.put(src, offset, remaining);
                write(buffer);
                buffer = allocate();
                offset += remaining;
                length -= remaining;
                remaining = buffer.remaining();
                continue;
            }
        }
        return buffer;
    }

    public void writeErrMessage(int errno, String msg) {
        writeErrMessage((byte) 1, errno, msg);
    }

    public void writeErrMessage(byte packetId, int errno, String msg) {
        ErrorPacket err = new ErrorPacket();
        err.packetId = packetId;
        err.errno = errno;
        err.message = encodeString(msg, CHARSET);
        err.write(this);
    }

    private final static byte[] encodeString(String src, String charset) {
        if (src == null) {
            return null;
        }
        if (charset == null) {
            return src.getBytes();
        }
        try {
            return src.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return src.getBytes();
        }
    }

    public ByteBuffer allocate() {
        return processor.getBufferPool().allocate();
    }

    public Processor getProcessor() {
        return processor;
    }

    public String getCharset() {
        return charset;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setSession(NonBlockSession session) {
        this.session = session;
    }

    public NonBlockSession getSession() {
        return session;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setCharsetIndex(int charsetIndex) {
        this.charsetIndex = charsetIndex;
    }

    public int getCharsetIndex() {
        return charsetIndex;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public boolean isUsing() {
        return isUsing;
    }

    public void setUsing(boolean using) {
        isUsing = using;
    }
}
