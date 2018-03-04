package renor.misc;

public enum EnumChatFormatting {
	BLACK('0'), DARK_BLUE('1'), DARK_GREEN('2'), DARK_AQUA('3'), DARK_RED('4'), DARK_PURPLE('5'), GOLD('6'), GRAY('7'), DARK_GRAY('8'), BLUE('9'), GREEN('a'), AQUA('b'), RED('c'), LIGHT_PURPLE('d'), YELLOW('e'), WHITE('f'), OBFUSCATED('k', true), BOLD('l', true), ITALIC('m', true), UNDERLINE('n', true), STRIKETHROUGH('o', true), RESET('r');

	private final String a;
	private final char c;
	private final boolean b;

	private EnumChatFormatting(char c) {
		this(c, false);
	}

	private EnumChatFormatting(char c, boolean a) {
		this.c = c;
		this.b = a;

		this.a = "\u00A7" + c;
	}

	public String toString() {
		return a;
	}
}
