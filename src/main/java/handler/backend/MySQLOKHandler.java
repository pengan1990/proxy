package handler.backend;


import conn.MySQLConnection;
import handler.SessionHandler;
import mysql.OKPacket;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by pengan on 16-10-11.
 */
public class MySQLOKHandler {
    private static final Logger logger = Logger.getLogger(MySQLOKHandler.class);

    public void handleOK(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleOK");
        // 1, wait for all node response
        // 2, reset mysql connection
        SessionHandler handler = conn.getSession().getHandler();
        handler.decrease(data, conn);
    }
}
