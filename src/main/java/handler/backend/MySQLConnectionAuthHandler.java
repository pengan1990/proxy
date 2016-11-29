package handler.backend;

import conn.MySQLConnection;
import exception.UnknownPacketException;
import handler.SessionHandler;
import mysql.AuthPacket;
import mysql.ErrorPacket;
import mysql.HandshakePacket;
import mysql.OKPacket;
import org.apache.log4j.Logger;
import util.CharsetUtil;
import util.SecurityUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pengan on 16-10-6.
 */
public class MySQLConnectionAuthHandler {
    private static final Logger logger = Logger.getLogger(MySQLConnectionAuthHandler.class);

    /**
     * send writeToChannel 411 : writeToChannel 323 是版本4.1之前的 不考虑
     *
     * @param hsp
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private void sendAuth411(HandshakePacket hsp, MySQLConnection conn) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        logger.debug("sendAuth411");

        AuthPacket ap = new AuthPacket();
        ap.packetId = 1;
        ap.clientFlags = MySQLConnection.CLIENT_FLAGS;
        ap.maxPacketSize = MySQLConnection.MAX_PACKET_SIZE;
        ap.charsetIndex = conn.getCharsetIndex();
        ap.user = conn.getPool().getInstance().getUser();
        String passwd = conn.getPool().getInstance().getPassword();
        if (passwd != null && passwd.length() > 0) {
            byte[] password = passwd.getBytes(conn.getCharset());
            byte[] seed = hsp.seed;
            byte[] restOfScramble = hsp.restOfScrambleBuff;
            byte[] authSeed = new byte[seed.length + restOfScramble.length];
            System.arraycopy(seed, 0, authSeed, 0, seed.length);
            System.arraycopy(restOfScramble, 0, authSeed, seed.length,
                    restOfScramble.length);
            ap.password = SecurityUtil.scramble411(password, authSeed);
        }
        ap.write(conn);
    }

    public void write(byte[] data, MySQLConnection conn) throws IOException {
        logger.debug("write");
        HandshakePacket hsp = new HandshakePacket();
        hsp.read(data);
        conn.setThreadId(hsp.threadId);

        int ci = (hsp.serverCharsetIndex & 0xff);
        if (CharsetUtil.getCharset(ci) != null) {
            conn.setCharset(CharsetUtil.getCharset(ci));
            conn.setCharsetIndex(ci);
        } else {
            throw new IllegalArgumentException("unknown charset:" + ci);
        }
        try {
            sendAuth411(hsp, conn);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void read(byte[] data, MySQLConnection conn) throws Exception {
        logger.debug("read");
        SessionHandler handler = conn.getSession().getHandler();
        switch (data[4]) {
            case OKPacket.FIELD_COUNT:
                logger.debug("write success");
                break;
            case ErrorPacket.FIELD_COUNT:
            default:
                handler.getErrorHandler().handleError(data, conn);
        }
    }
}
