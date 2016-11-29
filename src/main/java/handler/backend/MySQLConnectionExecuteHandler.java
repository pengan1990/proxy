package handler.backend;

import conn.MySQLConnection;
import handler.SessionHandler;
import mysql.EOFPacket;
import mysql.ErrorPacket;
import mysql.OKPacket;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by pengan on 16-10-6.
 * <p/>
 * <p/>
 * for all mysql connection to share this handler
 */
public class MySQLConnectionExecuteHandler {

    private static final Logger logger = Logger.getLogger(MySQLConnectionExecuteHandler.class);

    public void write(byte[] data, MySQLConnection conn) {
        logger.debug("write");
        logger.debug(conn.getSchema() + " execute command is " + conn.getExecuteCommand());
        conn.getExecuteCommand().write(conn);
    }

    public void read(byte[] data, MySQLConnection conn) throws Exception {
        // select return field eof row eof update/insert/delete return ok packet
        logger.debug(conn.getSchema() + " read");
        switch (data[4]) {
            case OKPacket.FIELD_COUNT:
                // ok packet;
                handleOK(data, conn);
                break;
            case EOFPacket.FIELD_COUNT:
                // 这里是分隔row packet 和 field packet
                switch (conn.getPacketState()) {
                    case MySQLConnection.PACKET_STATE_FIELD:
                        conn.setPacketState(MySQLConnection.PACKET_STATE_ROW);

                        // field packet are ready
                        handleFieldEof(data, conn);
                        break;
                    case MySQLConnection.PACKET_STATE_ROW:
                        // row eof
                        conn.setPacketState(MySQLConnection.PACKET_STATE_ROW_EOF);
                        handleRowEof(data, conn);
                        break;
                    default:
                        // here just abandon data
                        logger.error("packet state error");
                        handleError(data, conn);
                }
                break;
            case ErrorPacket.FIELD_COUNT:
                logger.error("read error packet");
                handleError(data, conn);
                break;
            default:
                switch (conn.getPacketState()) {
                    case MySQLConnection.PACKET_STATE_FIELD_HEADER:
                        conn.setHeader(data);
                        conn.setPacketState(MySQLConnection.PACKET_STATE_FIELD);
                        break;
                    case MySQLConnection.PACKET_STATE_FIELD:
                        conn.addField(data);
                        break;
                    case MySQLConnection.PACKET_STATE_ROW:
                        handleRow(data, conn);
                        break;
                    default:
                        // here just abandon data
                        logger.error("packet state error");
                        handleError(data, conn);
                }
        }
    }

    private void handleOK(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleOK");
        SessionHandler handler = conn.getSession().getHandler();
        MySQLOKHandler okHandler = handler.getOkHandler();
        okHandler.handleOK(data, conn);
    }

    private void handleError(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleError");
        SessionHandler handler = conn.getSession().getHandler();
        MySQLErrorHandler errorHandler = handler.getErrorHandler();
        errorHandler.handleError(data, conn);
    }

    private void handleRowEof(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleRowEof");
        SessionHandler handler = conn.getSession().getHandler();
        MySQLRowEOFHandler rowEOFHandler = handler.getRowEOFHandler();
        rowEOFHandler.handleRowEOF(data, conn);
    }

    private void handleRow(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleRow");
        SessionHandler handler = conn.getSession().getHandler();
        handler.getRowHandler().handleRow(data, conn);
    }

    /**
     * first send field packet to client first
     *
     * @param data
     * @param conn
     */
    private void handleFieldEof(byte[] data, MySQLConnection conn) throws UnsupportedEncodingException {
        logger.debug("handleFieldEof");
        SessionHandler handler = conn.getSession().getHandler();
        if (handler.isFieldPacketHandled()) {
            // only one connection field packet can send forward
            return;
        }
        handler.setFieldPacketHandled(true);
        MySQLFieldEOFHandler fieldHandler = handler.getFieldEOFHandler();
        fieldHandler.handleFieldEOF(data, conn);
    }


}
