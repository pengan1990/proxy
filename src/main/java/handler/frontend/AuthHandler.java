package handler.frontend;

import check.Privilege;
import conn.NonBlockSession;
import conn.ServerConnection;
import handler.Handler;
import mysql.AuthPacket;
import mysql.ErrorCode;
import mysql.MySQLPacket;
import mysql.QuitPacket;
import org.apache.log4j.Logger;
import util.SecurityUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pengan on 16-9-26.
 */
public class AuthHandler implements Handler {
    private static final Logger logger = Logger.getLogger(AuthHandler.class);

    private static final byte[] AUTH_OK = new byte[] { 7, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0 };
    private ServerConnection conn;

    public AuthHandler(ServerConnection conn) {
        this.conn = conn;
    }

    public void handle(byte[] data) throws IOException {
        logger.debug("handle");
        if (data.length == QuitPacket.QUIT.length &&
                data[4] == MySQLPacket.COM_QUIT) {
            logger.debug("com quit");
            conn.close();
            return;
        }
        AuthPacket auth = new AuthPacket();
        auth.read(data);

        if (validate(auth)) {
            NonBlockSession session = new NonBlockSession(conn);
            conn.setSession(session);
            // wait for front end command
            return;
        }
        // 验证不通过 关闭连接
        logger.debug("auth failure close ");
//        conn.close();
    }

    private boolean validate(AuthPacket auth) {
        logger.debug("validate");
        if (Privilege.userExist(auth.user) && passwordCheck(auth)) {
            logger.debug("validate success");
            conn.setCharsetIndex(auth.charsetIndex);
            conn.setUser(auth.user);
            conn.setSchema(auth.database);
            conn.write(AUTH_OK);
            return true;
        }
        conn.writeErrMessage((byte) 2, ErrorCode.ER_ACCESS_DENIED_ERROR, auth.user + " or password is not correct");
        return false;
    }

    private boolean passwordCheck(AuthPacket auth) {
        String dbPassword = Privilege.getPassword(auth.user);
        String hexPass = dbPassword.substring(1);
        byte[] hexPassByte = SecurityUtil.hexToBytes(hexPass);

        // check null
        if (dbPassword == null || dbPassword.length() == 0) {
            if (auth.password == null || auth.password.length == 0) {
                return true;
            } else {
                return false;
            }
        }

        if (auth.password == null || auth.password.length == 0) {
            return false;
        }
        // encrypt
        try {
            return SecurityUtil.checkPass(auth.password, hexPassByte,
                    conn.getSeed());
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

}
