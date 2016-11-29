package handler.backend;

import conn.MySQLConnection;
import conn.NonBlockSession;
import handler.SessionHandler;
import mysql.FieldPacket;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Set;


/**
 * Created by pengan on 16-10-10.
 */
public class MySQLFieldEOFHandler {
    private static final Logger logger = Logger.getLogger(MySQLFieldEOFHandler.class);

    /**
     * wait for all field packet ready then send forward
     * <p/>
     * two point:
     * <p/>
     * 1, only one complement field packet send forward
     * <p/>
     * 2, this process including filter sensitive columns
     * <p/>
     * 3, calculate packet id
     *
     * @param eof
     * @param conn
     */
    public void handleFieldEOF(byte[] eof, MySQLConnection conn) throws UnsupportedEncodingException {
        logger.debug("handleFieldEOF");
        NonBlockSession session = conn.getSession();
        if (session.getRouteHint().isComplexQuery()) {
            // isComplexQuery : reserve field info in session handler
            reserveFieldValue(conn);
        }
        sendFieldPacket(eof, conn);
    }

    private void reserveFieldValue(MySQLConnection conn) {
        logger.debug("reserveFieldValue");
        NonBlockSession session = conn.getSession();
        SessionHandler handler = session.getHandler();
        FieldPacket fieldPacket = null;
        for (byte[] field : conn.getFields()) {
            fieldPacket = new FieldPacket();
            fieldPacket.read(field);
            handler.addFieldPacket(fieldPacket);
        }
    }

    private void sendFieldPacket(byte[] eof, MySQLConnection conn) throws UnsupportedEncodingException {
        logger.debug("sendFieldPacket");
        byte[] header = conn.getHeader();
        NonBlockSession session = conn.getSession();
        SessionHandler handler = session.getHandler();
        Set<Integer> deleteColumns = session.getRouteHint().getDeleteColumnSet();
        // write header
        // recalculate field number
        header[4] = (byte) (conn.getFields().size() - deleteColumns.size());
        handler.write(header, false);
        session.getRouteHint().refresh(conn.getFields().size());
        // write field
        int columnPos = 0;
        for (byte[] field : conn.getFields()) {
            if (deleteColumns.contains(columnPos++)) {
                continue;
            }
            handler.writeField(field, false);
        }
        // write eof
        handler.write(eof, false);
    }

}
