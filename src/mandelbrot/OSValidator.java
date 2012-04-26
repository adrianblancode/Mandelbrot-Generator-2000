package mandelbrot;

public class OSValidator {
	public enum OperativeSystem {
		Windows,
		Mac,
		Linux,
		Unix;
	}
	/**
	 * Returns what kind of operativesystem is in use. Will return null if system is unsupported.
	 * @return current use of operativesystem.
	 */
	public static OperativeSystem getOS() {
		if(isWindows()) {
			return OperativeSystem.Windows;
		}
		if(isMac()) {
			return OperativeSystem.Mac;
		}
		if(isLinux()) {
			return OperativeSystem.Linux;
		}
		if(isUnix()) {
			return OperativeSystem.Unix;
		}
		return null;
	}
	
	/**
	 * Checks wheter the current Operativesystem is Windows.
	 * @return true if Windows, otherwise false.
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().indexOf("windows") >=0;
	}
	/**
	 * Checks wheter the current Operativesystem is Mac.
	 * @return true if Mac, otherwise false.
	 */
	public static boolean isMac() {
		return System.getProperty("os.name").toLowerCase().indexOf("mac") >=0;
	}
	/**
	 * Checks wheter the current Operativesystem is Linux.
	 * @return true if Linux, otherwise false.
	 */
	public static boolean isLinux() {
		return System.getProperty("os.name").toLowerCase().indexOf("nux") >=0;
	}
	/**
	 * Checks wheter the current Operativesystem is Unix.
	 * @return true if Unix, otherwise false.
	 */
	public static boolean isUnix() {
		return System.getProperty("os.name").toLowerCase().indexOf("nix") >=0;
	}
}
