package mysql;

/**
 * Created by pengan on 16-9-25.
 */
public interface Versions {

    /** 协议版本 */
    byte PROTOCOL_VERSION = 10;

    /** 服务器版本 */
    byte[] SERVER_VERSION = "5.6.16-debug-log".getBytes();
}

