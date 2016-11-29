package server;

import com.sun.net.httpserver.HttpServer;
import handler.http.ReloadMetaHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by pengan on 16-11-28.
 * <p/>
 * this is a meta data server to handle meta data Using http server
 * <p/>
 * rather than tcp
 * <p/>
 * this HttpServer from jdk1.6 are on a serial manner
 *
 * forbidden to tcp keep alive
 */
public class MetaDataServer extends Thread {
    private final int port;
    private final String name = "MetaDataServer";
    private HttpServer httpServer;
    private volatile boolean isClosed;

    public MetaDataServer(int port) throws IOException {
        this.port = port;
        this.isClosed = false;
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
    }

    private void startServer() {
        httpServer.createContext("/proxy/reload", new ReloadMetaHandler());//用MyHandler类内处理到/chinajash的请求
        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();
    }

    @Override
    public void run() {
        startServer();
    }

    public void terminate() {
        if (!this.isClosed) {
            this.isClosed = true;
            this.httpServer.stop(0);
        }
    }

    public void restart() throws IOException {
        if (this.isClosed) { // TODO only restart when the previous server is closed
            this.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
            this.isClosed = false;
            startServer();
        }
    }
}
