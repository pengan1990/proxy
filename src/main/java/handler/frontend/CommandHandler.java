package handler.frontend;

import check.Privilege;
import config.model.DataNodeConfig;
import conn.NonBlockSession;
import conn.ServerConnection;
import handler.Handler;
import handler.RouteHandler;
import mysql.ErrorCode;
import mysql.MySQLPacket;
import org.apache.log4j.Logger;
import util.MySQLMessage;
import util.StringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by pengan on 16-9-26.
 */
public class CommandHandler implements Handler {
    private static final Logger logger = Logger.getLogger(CommandHandler.class);

    private ServerConnection conn;

    public CommandHandler(ServerConnection conn) {
        this.conn = conn;
    }

    @Override
    public void handle(byte[] data) throws IOException {
        logger.debug("handle");
        switch (data[4] & 0xff) {
            case MySQLPacket.COM_FIELD_LIST: // need to send back
                fieldList(data);
                break;
            case MySQLPacket.COM_QUERY: // need to send back
                query(data);
                break;
            case MySQLPacket.COM_QUIT:
                close();
                break;
            case MySQLPacket.COM_INIT_DB: // need to catch
                initDb(data);
                break;
            default:
                conn.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unknown command");
        }
    }

    private void close() throws IOException {
        logger.debug("close");
        if (conn.getSession() != null) {
            conn.getSession().close();
        }
    }

    private void query(byte[] data) throws IOException {
        // generate route for mysql data node
        logger.debug("query");
        NonBlockSession session = conn.getSession();
        session.getRouteHint().reset();
        session.getRoutes().reset();

        session.getRouteHandler().handle(data);
        session.execute();
    }

    private void fieldList(byte[] data) throws IOException {
        logger.debug("fieldList");
        if (conn.getSchema() == null) {
            conn.error(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }
        MySQLMessage message = new MySQLMessage(data);
        message.position(5);

        String table = null;
        try {
            table = message.readString(conn.getCharset());
        } catch (UnsupportedEncodingException e) {
            conn.error(ErrorCode.ER_UNKNOWN_CHARACTER_SET,
                    "Unknown charset '" + conn.getCharset() + "'");
            return;
        }
        logger.debug("show field for table " + table);
        if (table == null) {
            conn.error(ErrorCode.ER_BAD_TABLE_ERROR, "Unknown table null");
            return;
        }
        DataNodeConfig nodeConfig = RouteHandler.handleFieldCommand(conn.getSchema(), table.toUpperCase());
        conn.getSession().executePacket(nodeConfig, data);
    }

    private void initDb(byte[] data) {
        logger.debug("initDb");
        MySQLMessage message = new MySQLMessage(data);
        message.position(5);
        String db = message.readString(); // character
        if (db == null) {
            conn.error(ErrorCode.ER_BAD_DB_ERROR, " database is null");
            return;
        }
        if (conn.getSchema() != null && StringUtil.equalsIgnoreCase(conn.getSchema(), db)) {
            conn.writeOkPacket();
            return;
        }

        // schema no equal then check schema if exist
        if (!Privilege.schemaExist(conn.getUser(), db)) {
            conn.error(ErrorCode.ER_BAD_DB_ERROR, " Unknown database " + conn.getUser());
            return;
        }
        conn.setSchema(db);
        conn.writeOkPacket();
    }
}
