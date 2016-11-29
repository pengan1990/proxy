package handler.frontend;

import conn.ServerConnection;
import org.apache.log4j.Logger;

/**
 * Created by pengan on 16-11-4.
 */
public class KillHandler {
    private static final Logger logger = Logger.getLogger(KillHandler.class);

    public static void response(ServerConnection conn, long id) {
        logger.debug("response");
        logger.debug("try to kill connection " + id);
        conn.writeOkPacket();
    }
}
