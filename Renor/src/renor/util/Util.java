package renor.util;

public class Util {
	public static Util.EnumOS getOSType() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("win") ? Util.EnumOS.WINDOWS : (osName.contains("mac") ? Util.EnumOS.MACOS : (osName.contains("solaris") ? Util.EnumOS.SOLARIS : (osName.contains("sunos") ? Util.EnumOS.SOLARIS : (osName.contains("linux") ? Util.EnumOS.LINUX : (osName.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN)))));
	}

	public static enum EnumOS {
		LINUX, SOLARIS, WINDOWS, MACOS, UNKNOWN;
	}
}
