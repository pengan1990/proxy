package handler.frontend;


import config.loader.MetaConfig;
import config.model.UserConfig;
import conn.ServerConnection;
import mysql.ErrorCode;
import org.apache.log4j.Logger;
import server.ProxyServer;
import util.StringUtil;

/**
 * Created by pengan on 16-11-4.
 */
public class UseHandler {
    private static final Logger logger = Logger.getLogger(UseHandler.class);
    private static final MetaConfig META_CONFIG = ProxyServer.getINSTANCE().getMetaConfig();

    public static void response(ServerConnection conn, String schema) {
        if (!META_CONFIG.getSchemas().containsKey(schema)) {
            conn.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + schema + "'");
            return;
        }
        UserConfig userConfig = META_CONFIG.getUsers().get(conn.getUser());
        if (!userConfig.getSchemas().contains(schema)) {
            conn.writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, "Access denied for user '" + conn.getUser()
                    + "' to database '" + schema + "'");
            return;
        }

        if (!StringUtil.equalsIgnoreCase(conn.getSchema(), schema)) {
            conn.setSchema(schema);
        }
        conn.writeOkPacket();
    }
}
