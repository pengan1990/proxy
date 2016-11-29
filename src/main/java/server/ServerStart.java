package server;

import java.io.File;

/**
 * Created by pengan on 16-10-17.
 */
public class ServerStart {
    public static void main(String[] args) {
        String config = System.getProperty("config") + File.separator + "config.properties";
        String log4j = System.getProperty("config") + File.separator + "log4j.xml";
        log4j = "/home/admin/idea-projects/nio-client/conf/log4j.xml";
        config = "/home/admin/idea-projects/nio-client/conf/config.properties";
        try {
            Log4jInitializer.configureAndWatch(log4j, 1000);
            ProxyServer server = ProxyServer.getINSTANCE();
            server.start(config);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
