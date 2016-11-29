package nio;

import conn.ServerConnection;
import org.apache.log4j.Logger;
import server.ProxyServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Created by pengan on 16-9-23.
 */
public class NIOAcceptor extends Thread {
    private static final Logger logger = Logger.getLogger(NIOAcceptor.class);
    private long acceptIndex;
    private ServerSocketChannel server;
    private Selector selector;

    public NIOAcceptor(int port) throws IOException {
        setName("NIOAcceptor");
        this.acceptIndex = 0;
        this.server = ServerSocketChannel.open();
        this.server.socket().bind(new InetSocketAddress(port));
        this.server.configureBlocking(false);
        this.selector = Selector.open();
        this.server.register(selector, SelectionKey.OP_ACCEPT);
    }

    // start up supply server
    @Override
    public void run() {
        final Selector selector = this.selector;
        while (true) {
            try {
                selector.select(1000L);
                Set<SelectionKey> keys = selector.selectedKeys();
                try {
                    for (SelectionKey key : keys) {
                        if (key.isValid() && key.isAcceptable()) {
                            accept();
                        } else {
                            logger.warn("key is not validate");
                            key.cancel();
                        }
                    }
                } finally {
                    keys.clear();
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    private void accept() throws IOException {
        long connectionId = (++this.acceptIndex & Long.MAX_VALUE);
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        Processor processor = nextProcessor(connectionId);
        ServerConnection serverConn = new ServerConnection(client, processor);
        serverConn.setConnectionId(connectionId);
        processor.frontendRegister(serverConn);
    }

    private Processor nextProcessor(long connectionId) {
        Processor[] processors = ProxyServer.getINSTANCE().getProcessors();
        int threads = ProxyServer.getINSTANCE().getSystemConfig().getProcessors();
        return processors[(int) ((connectionId & (threads - 1)) & Integer.MAX_VALUE)];
    }
}
