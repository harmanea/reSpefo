package cz.cuni.mff.respefo;

public class Version {
	public static final int MAJOR = 0;
	public static final int MINOR = 8;
	public static final int RELEASE = 5;
	public static final String BUILD = "unix64";
	
	public static String toFullString() {
		return String.format("v%d.%d.%d %s", MAJOR, MINOR, RELEASE, BUILD);
	}
	
	public static String toSimpleString() {
		return String.format("%d.%d.%d", MAJOR, MINOR, RELEASE);
	}
}
