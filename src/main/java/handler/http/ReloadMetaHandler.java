package handler.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.loader.MetaConfig;
import exception.ConfigException;
import org.apache.log4j.Logger;
import server.ProxyServer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by pengan on 16-11-28.
 */
public class ReloadMetaHandler implements HttpHandler {
    private static final Logger logger = Logger.getLogger(ReloadMetaHandler.class);

    /**
     * make sure that all processors get the coordinate meta data
     * <p/>
     * nginx way : producing newly processor
     * <p/>
     * rather then using the old worker just waiting previous worker have no connections
     * <p/>
     * then replace all here we just replace the old
     *
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        logger.debug("handle");
        MetaConfig metaConfig = ProxyServer.getINSTANCE().getMetaConfig();
        int retCode = 200;
        StringBuilder response = new StringBuilder("reload success");
        try {
            metaConfig.reload();
        } catch (ConfigException e) {
            retCode = 400;
            response.setLength(0);
            response.append(e.getLocalizedMessage());
        }
        httpExchange.sendResponseHeaders(retCode, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        httpExchange.close();
    }
}
