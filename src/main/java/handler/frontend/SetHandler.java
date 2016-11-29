package handler.frontend;

import conn.ServerConnection;
import org.apache.log4j.Logger;
import parse.ast.stmt.SQLStatement;
import parse.ast.stmt.dal.DALSetAutocommitStatement;

/**
 * Created by pengan on 16-11-4.
 */
public class SetHandler {
    private static final Logger logger = Logger.getLogger(SetHandler.class);

    public static void response(ServerConnection conn, SQLStatement set) {
        logger.debug("response");
        // here to save set sqls in session
        boolean autocommit = false;
        if (set instanceof DALSetAutocommitStatement) {
            // set autocommit = true or false
            autocommit = ((DALSetAutocommitStatement)set).isAutocommit();
            if (autocommit && conn.isTransaction()) {// autocommit is true
                TransactionHandler.commit(conn, null);
                return;
            }
            conn.setTransaction(autocommit);
        }
        conn.writeOkPacket();
    }
}
