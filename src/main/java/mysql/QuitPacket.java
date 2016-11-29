package mysql;

/**
 * Created by pengan on 16-9-25.
 */
public class QuitPacket extends MySQLPacket {
    public static final byte[] QUIT = new byte[]{1, 0, 0, 0, 1};

    @Override
    public int calcPacketSize() {
        return 1;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Quit Packet";
    }

}
