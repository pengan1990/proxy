package handler.backend;

import conn.MySQLConnection;
import conn.NonBlockSession;
import handler.SessionHandler;
import mysql.EOFPacket;
import mysql.ErrorPacket;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by pengan on 16-9-29.
 */
public class MySQLConnectionFieldListHandler {
    private static final Logger logger = Logger.getLogger(MySQLConnectionExecuteHandler.class);


    public void write(byte[] data, MySQLConnection conn) {
        logger.debug("handle");
        conn.getExecuteCommand().write(conn);
    }

    public void read(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("read");
        // only error packet and column definition packet with eof end
        switch (data[4]) {
            case ErrorPacket.FIELD_COUNT:
                handleError(data, conn);
                break;
            case EOFPacket.FIELD_COUNT:
                handleFieldEof(data, conn);
                break;
            default:
                handleFieldRow(data, conn);
        }
    }

    private void handleFieldRow(byte[] data, MySQLConnection conn) throws UnsupportedEncodingException {
        logger.debug("handleFieldRow");
        NonBlockSession session = conn.getSession();
        SessionHandler handler = session.getHandler();

        handler.writeField(data, false);
    }

    private void handleError(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleError");
        SessionHandler handler = conn.getSession().getHandler();
        MySQLErrorHandler errorHandler = handler.getErrorHandler();
        errorHandler.handleError(data, conn);
    }

    private void handleFieldEof(byte[] eof, MySQLConnection conn) throws Exception {
        logger.debug("handleFieldEof");
        NonBlockSession session = conn.getSession();
        SessionHandler handler = session.getHandler();
        handler.decrease(eof, conn);
    }
}
