package util;

import mysql.ErrorCode;
import mysql.ErrorPacket;
import mysql.FieldPacket;
import mysql.HeaderPacket;

import java.io.UnsupportedEncodingException;

/**
 * @author xianmao.hexm
 */
public class PacketUtil {
    //private static final String CODE_PAGE_1252 = "Cp1252";
    public static final String CODE_PAGE_1252 = "Cp1252";

    
    public static final HeaderPacket getHeader(int fieldCount) {
        HeaderPacket packet = new HeaderPacket();
        packet.packetId = 1;
        packet.fieldCount = fieldCount;
        return packet;
    }

    public static byte[] encode(String src, String charset) {
        if (src == null) {
            return null;
        }
        try {
            return src.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return src.getBytes();
        }
    }

    public static final FieldPacket getField(String name, String orgName, int type) {
        FieldPacket packet = new FieldPacket();
        packet.charsetIndex = CharsetUtil.getIndex(CODE_PAGE_1252);
        packet.name = encode(name, CODE_PAGE_1252);
        packet.orgName = encode(orgName, CODE_PAGE_1252);
        packet.type = (byte) type;
        return packet;
    }

    public static final FieldPacket getField(String name, int type) {
        FieldPacket packet = new FieldPacket();
        packet.charsetIndex = CharsetUtil.getIndex(CODE_PAGE_1252);
        packet.name = encode(name, CODE_PAGE_1252);
        packet.type = (byte) type;
        return packet;
    }
}
