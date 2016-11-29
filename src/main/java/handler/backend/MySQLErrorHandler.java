package handler.backend;

import conn.MySQLConnection;
import conn.ServerConnection;
import handler.SessionHandler;
import mysql.ErrorPacket;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by pengan on 16-10-11.
 */
public class MySQLErrorHandler {
    private static final Logger logger = Logger.getLogger(MySQLErrorHandler.class);

    /**
     * 主要处理 sql语句执行异常
     * <p/>
     * 放心 这个是单线程 没有用多线程来处理
     * <p/>
     * if one connection return error then terminate other connections
     * <p/>
     * here is clear that only sql execute error no io exception
     * <p/>
     * so just return to pool while not used and registered
     *
     * @param data
     * @param conn
     */
    public void handleError(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("handleError");
        ErrorPacket error = new ErrorPacket();
        error.read(data);
        logger.error(new String(error.message));

        SessionHandler handler = conn.getSession().getHandler();
        handler.saveError(data);
        handler.decrease(data, conn);
    }

    /**
     * 主要处理 channel 异常
     *
     * @param errno
     * @param exp
     * @param conn
     */
    public void handleException(int errno, Throwable exp, MySQLConnection conn) {
        exp.printStackTrace();
        logger.debug("handleException : " + conn.getSchema() + conn);
        // TODO: 16-11-20 here exp.getLoallizedMessage may be null
        StringBuilder errMsg = new StringBuilder(exp.getLocalizedMessage() == null ?
                "handle error" : exp.getLocalizedMessage());
        logger.error(errMsg);
        ServerConnection front = conn.getSession().getFrontConn();
        SessionHandler handler = conn.getSession().getHandler();
        // send error packet to front
        front.writeErrMessage(errno, errMsg.toString());
        handler.interrupt();
    }
}
