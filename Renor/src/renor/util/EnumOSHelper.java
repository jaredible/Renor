package renor.util;

public class EnumOSHelper {
	public static final int[] osIds = new int[EnumOS.values().length];

	static {
		try {
			osIds[EnumOS.LINUX.ordinal()] = 1;
		} catch (NoSuchFieldError e) {
		}

		try {
			osIds[EnumOS.SOLARIS.ordinal()] = 2;
		} catch (NoSuchFieldError e) {
		}

		try {
			osIds[EnumOS.WINDOWS.ordinal()] = 3;
		} catch (NoSuchFieldError e) {
		}

		try {
			osIds[EnumOS.MACOS.ordinal()] = 4;
		} catch (NoSuchFieldError e) {
		}
	}
}
