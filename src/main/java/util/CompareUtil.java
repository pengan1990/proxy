package util;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CompareUtil {
	private static final Logger LOGGER = Logger.getLogger(CompareUtil.class);
	
	//private static final DateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
	//private static final DateFormat fmtDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//private static final DateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
	
	/*
	 * TODO 
	 * FIELD_TYPE_SHORT smallint
	 * FIELD_TYPE_INT24 mediumint
	 * FIELD_TYPE_NEWDECIMAL decimal, numeric
	 * FIELD_TYPE_DATETIME datetime
	 */
	public static int compare(byte[] left, byte[] right, int type) throws Exception {
		switch(type) {
		case FieldType.FIELD_TYPE_TINY://tinyint
		case FieldType.FIELD_TYPE_LONG://int, int signed  
		case FieldType.FIELD_TYPE_LONGLONG://bigint
		case FieldType.FIELD_TYPE_SHORT://smallint
		case FieldType.FIELD_TYPE_INT24://mediumint
			return CompareUtil.compareLong(left, right);
			
		case FieldType.FIELD_TYPE_NEWDECIMAL://decimal numeric
		case FieldType.FIELD_TYPE_DOUBLE://real, double precision
		case FieldType.FIELD_TYPE_FLOAT://float
			return CompareUtil.compareNumber(left, right);
			
		case FieldType.FIELD_TYPE_VAR_STRING://varchar
		case FieldType.FIELD_TYPE_STRING://char, 
			return CompareUtil.compareString(left, right);
			
		case FieldType.FIELD_TYPE_DATE://date
			return CompareUtil.compareDate(left, right);
		
		case FieldType.FIELD_TYPE_DATETIME:
		case FieldType.FIELD_TYPE_TIMESTAMP://timestamp
			return CompareUtil.compareDateTime(left, right);
			
		case FieldType.FIELD_TYPE_TIME://time
			return CompareUtil.compareTime(left, right);
		
		default:
			LOGGER.error("do not support type: " + type);
			throw new Exception("compare error, do not support type: " + type);
		}
	}
	
	
	public static int compareNumber(byte[] left, byte[] right) {
		if (left == null) {
			return -1;
		}
		
		if (right == null) {
			return 1;
		}
		
		Double leftDouble = Double.parseDouble(new String(left));
		Double rightDouble = Double.parseDouble(new String(right));
		
		int result = leftDouble.compareTo(rightDouble);
//		LOGGER.debug("leftDouble: " + leftDouble + ", rightDouble: " + rightDouble + ", result: " + result);
		return result;
	}
	
	public static int compareLong(byte[] left, byte[] right) {
		if (left == null) {
			return -1;
		}
		
		if (right == null) {
			return 1;
		}
		
		Long leftLong = Long.parseLong(new String(left));
		Long rightLong = Long.parseLong(new String(right));
		return leftLong.compareTo(rightLong);
	}
	
	
	public static int compareString(byte[] left, byte[] right) {
		if (left == null) {
			return -1;
		}
		
		if (right == null) {
			return 1;
		}
		
		String leftStr1 = new String(left).toLowerCase();
		String rightStr2 = new String(right).toLowerCase();

		int result = leftStr1.compareTo(rightStr2);
		
//		LOGGER.debug("leftStr1: " + leftStr1 + ", rightStr2: " + rightStr2 + ", result: " + result);
		return result;
	}
	
	public static int compareBit(byte[] left, byte[] right) {
		//TODO
		LOGGER.debug("compareBit");
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}
		if (left.length > right.length) {
			return -1;
		}
		if (left.length < right.length) {
			return 1;
		}

		for (int idx = 0; idx < left.length; idx ++) {
			if(left[idx] > right[idx]) {
				return 1;
			} else if (left[idx] < right[idx]) {
			return -1;
			}
		}
		return 0;
	}
	
	public static int compareDate(byte[] left, byte[] right) throws ParseException {
		LOGGER.debug("compareDate");
		
		if (left == null) {
			return -1;
		}
		
		if (right == null) {
			return 1;
		}
		
		String leftStr = new String(left);
		String rightStr = new String(right);

        DateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
		Date leftDate = fmtDate.parse(leftStr);
		Date rightDate = fmtDate.parse(rightStr);
		
		return leftDate.compareTo(rightDate);
	}
	
	public static int compareDateTime(byte[] left, byte[] right) throws ParseException {
		//TODO
		LOGGER.debug("compareDateTime");
		
		if (left == null) {
			return -1;
		}
		
		if (right == null) {
			return 1;
		}
		
		String leftStr = new String(left);
		String rightStr = new String(right);

        DateFormat fmtDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date leftDate = fmtDateTime.parse(leftStr);
		Date rightDate = fmtDateTime.parse(rightStr);
		
		return leftDate.compareTo(rightDate);
	}
	
	public static int compareTime(byte[] left, byte[] right) throws ParseException {
		//TODO
		
		LOGGER.debug("compareTime");
		
		if (left == null) {
			return -1;
		}
		
		if (right == null) {
			return 1;
		}
		
		String leftStr = new String(left);
		String rightStr = new String(right);
		
//		LOGGER.debug("left: " + leftStr + ", right: " + rightStr);

        DateFormat fmtTime = new SimpleDateFormat("HH:mm:ss");
		Date leftDate = fmtTime.parse(leftStr);
		Date rightDate = fmtTime.parse(rightStr);
		
		return leftDate.compareTo(rightDate);
	}
}
