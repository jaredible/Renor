package renor.util;

public enum EnumRarity {
	rareWhite(0xffffff), rareGreen(0x00ff00), rareOrange(0xff7f00), rareDarkGray(0x3f3f3f);

	public final int rarityColor;

	// white, green, blue, purple, yellow, orange, dark gray
	private EnumRarity(int rarityColor) {
		this.rarityColor = rarityColor;
	}
}
