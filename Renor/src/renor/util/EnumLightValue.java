package renor.util;

public enum EnumLightValue {
	Sky(15), Block(0);

	public final int defaultLightValue;

	private EnumLightValue(int lightValue) {
		defaultLightValue = lightValue;
	}
}
