package cz.cuni.mff.respefo.util;

public class StringUtils {
	
	public static String trimmedOrPaddedString(String str, int targetLength) {
		String result = str.substring(0, Math.min(targetLength, str.length()));
		for (int i = str.length(); i < targetLength; i++) {
			result = " " + result;
		}
		return result;
	}
	
}
