package mysql;

import conn.AbstractConnection;
import util.BufferUtil;
import util.MySQLMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by pengan on 16-11-4.
 */
public class RowDataPacket extends MySQLPacket {
    public static final byte NULL_MARK = (byte) 0xfb;
    public static final byte EMPTY_MARK = (byte) 0x00;

    public int fieldCount;
    public List<byte[]> fieldValues;

    public RowDataPacket(int fieldCount) {
        this.fieldCount = fieldCount;
        this.fieldValues = new ArrayList<byte[]>(fieldCount);
    }

    public void add(byte[] value) {
        fieldValues.add(value);
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        for (int i = 0; i < fieldCount; i++) {
            fieldValues.add(mm.readBytesWithLength());
        }
    }


    @Override
    public ByteBuffer write(ByteBuffer bb, AbstractConnection c) {
        bb = c.checkWriteBuffer(bb, AbstractConnection.PACKET_HEAD_SIZE);
        BufferUtil.writeUB3(bb, calcPacketSize());
        bb.put(packetId);
        for (int i = 0; i < fieldCount; i++) {
            byte[] fv = fieldValues.get(i);

            if (fv == null) {
                bb = c.checkWriteBuffer(bb, 1);
                bb.put(RowDataPacket.NULL_MARK);//fv.lenght == 0的时候，发送EMPTY_MARK, fv = null的时候，发送NULL_MARK
                continue;
            }

            if (fv.length == 0) {
                bb = c.checkWriteBuffer(bb, 1);
                bb.put(RowDataPacket.EMPTY_MARK);//fv.lenght == 0的时候，发送EMPTY_MARK, fv = null的时候，发送NULL_MARK
            } else {
                bb = c.checkWriteBuffer(bb, BufferUtil.getLength(fv.length));
                BufferUtil.writeLength(bb, fv.length);
                bb = c.writeToBuffer(fv, bb);
            }
        }
        return bb;
    }

    public ByteBuffer write(ByteBuffer bb, AbstractConnection c, Set<Integer> delete) {
        bb = c.checkWriteBuffer(bb, AbstractConnection.PACKET_HEAD_SIZE);
        BufferUtil.writeUB3(bb, calcPacketSize(delete));
        bb.put(packetId);

        for (int i = 0; i < fieldCount; i++) {
            if (delete.contains(i))
                continue;

            byte[] fv = fieldValues.get(i);

            if (fv == null || fv.length == 0) {
                bb = c.checkWriteBuffer(bb, 1);
                bb.put(RowDataPacket.NULL_MARK);
            } else {
                bb = c.checkWriteBuffer(bb, BufferUtil.getLength(fv.length));
                BufferUtil.writeLength(bb, fv.length);
                bb = c.writeToBuffer(fv, bb);
            }
        }

        return bb;
    }

    @Override
    public int calcPacketSize() {//TODO 这里可能需要重新计算一下，因为可能会涉及到有些字段需要剔除
        int size = 0;
        for (int i = 0; i < fieldCount; i++) {
            byte[] v = fieldValues.get(i);
            size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }
        return size;
    }


    public int calcPacketSize(Set<Integer> delete) {
        int size = 0;
        //TODO
        for (int i = 0; i < fieldCount; i++) {
            if (delete.contains(i))
                continue;

            byte[] v = fieldValues.get(i);
            size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }

        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL RowData Packet";
    }

}
