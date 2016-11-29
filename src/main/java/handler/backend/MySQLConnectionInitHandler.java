package handler.backend;

import conn.MySQLConnection;
import exception.ErrorPacketException;
import handler.SessionHandler;
import mysql.CommandPacket;
import mysql.ErrorPacket;
import mysql.OKPacket;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by pengan on 16-10-9.
 */
public class MySQLConnectionInitHandler {
    private static final Logger logger = Logger.getLogger(MySQLConnectionInitHandler.class);

    public void write(byte[] data, MySQLConnection conn) {
        logger.debug("write");
        CommandPacket command = conn.getInitCommands().remove(0);
        command.write(conn);
    }

    public void read(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("read");
        switch (data[4]) {
            case OKPacket.FIELD_COUNT:
                handleOK(data, conn);
                break;
            default:
                handleError(data, conn);
                ErrorPacket err = new ErrorPacket();
                err.read(data);
                throw new ErrorPacketException(new String(err.message, conn.getCharset()));
        }
    }

    private void handleOK(byte[] data, MySQLConnection conn) throws IOException {
        logger.debug("handleOK");
        // OK do nothing
    }

    private void handleError(byte[] data, MySQLConnection conn) throws Exception {
        SessionHandler handler = conn.getSession().getHandler();
        MySQLErrorHandler errorHandler = handler.getErrorHandler();
        errorHandler.handleError(data, conn);
    }


}
