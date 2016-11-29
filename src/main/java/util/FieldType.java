package util;

//参考
//http://mysql.timesoft.cc/doc/internals/en/field-packet.html

public class FieldType {
	
	public static final int FIELD_TYPE_DECIMAL = 0x00;
	public static final int FIELD_TYPE_TINY = 0x01;
	public static final int FIELD_TYPE_SHORT = 0x02;
	public static final int FIELD_TYPE_LONG = 0x3;
	public static final int FIELD_TYPE_FLOAT = 0x04;
	public static final int FIELD_TYPE_DOUBLE = 0x05;
	public static final int FIELD_TYPE_NULL = 0x06;
	
	public static final int FIELD_TYPE_TIMESTAMP = 0x07;
	public static final int FIELD_TYPE_LONGLONG = 0x08;
	public static final int FIELD_TYPE_INT24 = 0x09;
	
	public static final int FIELD_TYPE_DATE = 0x0a;
	public static final int FIELD_TYPE_TIME = 0x0b;
	public static final int FIELD_TYPE_DATETIME = 0x0c;
	
	public static final int FIELD_TYPE_YEAR = 0x0d;
	public static final int FIELD_TYPE_NEWDATE = 0x0e;
	
	public static final int FIELD_TYPE_VARCHAR = 0x0f;
	
	public static final int FIELD_TYPE_BIT = 0x010;
	
	public static final int FIELD_TYPE_NEWDECIMAL = 0xf6;
	
	public static final int FIELD_TYPE_ENUM = 0xf7;
	
	public static final int FIELD_TYPE_SET = 0xf8;
	
	public static final int FIELD_TYPE_TINEY_BLOB = 0xf9;
	public static final int FIELD_TYPE_MEDIUM_BLOB = 0xfa;
	public static final int FIELD_TYPE_LONG_BLOB = 0xfb;
	public static final int FIELD_TYPE_BLOB = 0xfc;
	public static final int FIELD_TYPE_VAR_STRING = 0xfd;
	public static final int FIELD_TYPE_STRING = 0xfe;
	public static final int FIELD_TYPE_GEOMETRY = 0xff;

}

