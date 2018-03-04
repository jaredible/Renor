package renor.misc;

public class StringTranslate {
	private static StringTranslate instance = new StringTranslate("en_US");

	public StringTranslate(String language) {
	}

	public static StringTranslate getInstance() {
		return instance;
	}
}
