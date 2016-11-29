package handler.backend;

import conn.MySQLConnection;
import handler.SessionHandler;
import org.apache.log4j.Logger;

/**
 * Created by pengan on 16-10-11.
 */
public class MySQLRowEOFHandler {

    private static final Logger logger = Logger.getLogger(MySQLRowEOFHandler.class);

    public void handleRowEOF(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleRowEOF");
        SessionHandler handler = conn.getSession().getHandler();
        handler.decrease(data, conn);
    }
}
