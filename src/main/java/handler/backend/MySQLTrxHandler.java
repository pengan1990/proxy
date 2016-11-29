package handler.backend;

import conn.MySQLConnection;
import mysql.CommandPacket;
import mysql.MySQLPacket;
import org.apache.log4j.Logger;

/**
 * Created by pengan on 16-11-22.
 */
public class MySQLTrxHandler {
    private static final Logger logger = Logger.getLogger(MySQLTrxHandler.class);
    public static final CommandPacket BEGIN;
    public static final CommandPacket COMMIT;
    public static final CommandPacket ROLLBACK;

    static {
        BEGIN = new CommandPacket();
        BEGIN.packetId = 0;
        BEGIN.command = MySQLPacket.COM_QUERY;
        BEGIN.arg = "BEGIN".getBytes();

        COMMIT = new CommandPacket();
        COMMIT.packetId = 0;
        COMMIT.command = MySQLPacket.COM_QUERY;
        COMMIT.arg = "COMMIT".getBytes();

        ROLLBACK = new CommandPacket();
        ROLLBACK.packetId = 0;
        ROLLBACK.command = MySQLPacket.COM_QUERY;
        ROLLBACK.arg = "ROLLBACK".getBytes();
    }

    public void commit(MySQLConnection conn) {
        logger.debug("commit:" + conn);
        COMMIT.write(conn);
    }

    public void rollback(MySQLConnection conn) {
        logger.debug("rollback" + conn);
        ROLLBACK.write(conn);
    }
}
