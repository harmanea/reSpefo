package cz.cuni.mff.respefo.util;

public class StringUtils {
	
	public static String paddedString(String str, int targetLength) {
		String result = str;
		for (int i = str.length(); i < targetLength; i++) {
			result = " " + result;
		}
		return result;
	}
	
}
