package handler.backend;

import conn.MySQLConnection;
import conn.NonBlockSession;
import handler.SessionHandler;
import mysql.RowDataPacket;
import org.apache.log4j.Logger;

/**
 * Created by pengan on 16-10-10.
 */
public class MySQLRowHandler {
    private static final Logger logger = Logger.getLogger(MySQLRowHandler.class);

    /**
     * addRow row data
     *
     * @param data
     * @param conn
     */
    public void handleRow(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleRow");
        filter(data, conn);

        // write to buffer
        NonBlockSession session = conn.getSession();
        SessionHandler handler = session.getHandler();
        if (handler.haveError()) {
            // just abandon this packet
            return;
        }
        if (session.getRouteHint().isComplexQuery()) {
            // if valid complex sql require proxy addRow
            addRow(data, conn);
            if (handler.backConnReadyCheck()) {
                // if all back connections are ready
                handler.getkMTree().refresh();
                handler.getkMTree().kMerge();
            }
            return;
        }
        handler.write(data, false);
    }

    private void addRow(byte[] data, MySQLConnection conn) {
        int fieldCount = conn.getFields().size();
        SessionHandler handler = conn.getSession().getHandler();
        RowDataPacket row = new RowDataPacket(fieldCount);
        row.read(data);
        handler.getBackRows()[conn.getIndex()].add(row);
    }


    private void filter(byte[] data, MySQLConnection conn) {
        logger.debug("filter");
    }
}
