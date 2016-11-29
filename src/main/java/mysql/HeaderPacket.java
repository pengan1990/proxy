package mysql;

import conn.AbstractConnection;
import util.BufferUtil;
import util.MySQLMessage;

import java.nio.ByteBuffer;

/**
 * Created by pengan on 16-11-4.
 */
public class HeaderPacket extends MySQLPacket {

    public int fieldCount;
    public long extra;

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        this.packetLength = mm.readUB3();
        this.packetId = mm.read();
        this.fieldCount = (int) mm.readLength();
        if (mm.hasRemaining()) {
            this.extra = mm.readLength();
        }
    }

    @Override
    public ByteBuffer write(ByteBuffer buffer, AbstractConnection c) {
        int size = calcPacketSize();
        buffer = c.checkWriteBuffer(buffer, AbstractConnection.PACKET_HEAD_SIZE + size);
        BufferUtil.writeUB3(buffer, size);
        buffer.put(packetId);
        BufferUtil.writeLength(buffer, fieldCount);
        if (extra > 0) {
            BufferUtil.writeLength(buffer, extra);
        }
        return buffer;
    }

    @Override
    public int calcPacketSize() {
        int size = BufferUtil.getLength(fieldCount);
        if (extra > 0) {
            size += BufferUtil.getLength(extra);
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL ResultSetHeader Packet";
    }

}
