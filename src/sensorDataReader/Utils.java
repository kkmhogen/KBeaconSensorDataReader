package sensorDataReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Utils {
   	
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	
	public static String getCurrentTime() {
		long nCurrentTime = System.currentTimeMillis();

		Date date = new Date(nCurrentTime);
		return DATE_FORMAT.format(date) + " ";
	}
	
	private static final char[] HEX_ARRAY = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	
	
	public static String bytesToHex(final byte[] bytes, final int start, final int length, final boolean add0x)
	{
		if (bytes == null || bytes.length <= start || length <= 0) {
			return "";
		}
		final int maxLength = Math.min(length, bytes.length - start);
		final char[] hexChars = new char[maxLength * 2];
		for (int j = 0; j < maxLength; j++) {
			final int v = bytes[start + j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		if (!add0x)
			return new String(hexChars);
		return "0x" + new String(hexChars);
	}
	
	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		char[] hexCharacter = hexString.toCharArray();
		for (int i = 0; i < hexCharacter.length; i++) {
			if (-1 == charToByte(hexCharacter[i])) {
				return null;
			}
		}

		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

		}
		return d;
	}

	public static String bytesToHex(final byte[] bytes, final boolean add0x) {
		if (bytes == null)
			return "";
		return bytesToHex(bytes, 0, bytes.length, add0x);
	}
	
	public static boolean isHexString(String hexString)
    {
        String pattern = "([0-9A-Fa-f]{2})+";
        String pattern2 = "^0x([0-9A-Fa-f]{2})+";
        String pattern3 = "^0X([0-9A-Fa-f]{2})+";
        if (!Pattern.matches(pattern, hexString))
        {
            if (!Pattern.matches(pattern2, hexString))
            {
            	if (!Pattern.matches(pattern3, hexString))
                {
                    return false;
                }
            }
        }

        return true;
    }
	
	
	public static boolean isMacAddressValid(String strMacAddr) {
		if (strMacAddr == null || strMacAddr.length() != 12) {
			return false;
		}

		return isHexString(strMacAddr);
	}
	
	public static boolean isNumber(String string) {
        if (string == null)
            return false;
        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
        return pattern.matcher(string).matches();
    }
}
