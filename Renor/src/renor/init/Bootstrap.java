package renor.init;

public class Bootstrap {
	private static boolean initialized = false;

	public static void init() {
		if (!initialized) {
			initialized = true;
			// do other stuff here
		}
	}
}
