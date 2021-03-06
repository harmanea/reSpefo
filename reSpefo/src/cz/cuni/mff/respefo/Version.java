package cz.cuni.mff.respefo;

public class Version {
	public static final int MAJOR = 1;
	public static final int MINOR = 1;
	public static final int RELEASE = 0;
	public static final String BUILD = "unix64";
	public static final String OS;
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		String bitness = System.getProperty("sun.arch.data.model");
		if (osName.contains("win")) {
			OS = "win" + bitness;
		} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
			OS = "unix" + bitness;
		} else if (osName.contains("mac")) {
			OS = "mac" + bitness;
		} else {
			OS = "unknown";
		}
	}
	public static final String PUBLISHED = "2020-10-04";
	
	/**
	 * @return version in the format vMAJOR.MINOR.RELEASE BUILD
	 */
	public static String toFullString() {
		return String.format("v%d.%d.%d %s %s", MAJOR, MINOR, RELEASE, PUBLISHED, BUILD);
	}
	
	/**
	 * @return version in the format MAJOR.MINOR.RELEASE
	 */
	public static String toSimpleString() {
		return String.format("%d.%d.%d", MAJOR, MINOR, RELEASE);
	}
}
