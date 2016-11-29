package mysql;

import conn.AbstractConnection;
import util.BufferUtil;
import util.MySQLMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by pengan on 16-10-6.
 */
public class CommandPacket extends MySQLPacket {

    public byte command;
    public byte[] arg;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        command = mm.read();
        arg = mm.readBytes();
    }

    @Override
    public void write(AbstractConnection c) {
        ByteBuffer buffer = c.allocate();
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.put(packetId);
        buffer.put(command);
        buffer = c.writeToBuffer(arg, buffer);
        c.write(buffer);
    }

    @Override
    public int calcPacketSize() {
        return 1 + arg.length;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Command Packet";
    }

    @Override
    public String toString() {
        try {
            return new String(arg, "utf8");
        } catch (UnsupportedEncodingException e) {
            return new String(arg);
        }
    }
}
