package renor.util;

public class ColorizerGrass {
	private static int[] grassBuffer = new int[65536];

	public static void setGrassBiomeColorizer(int[] data) {
		grassBuffer = data;
	}

	public static int getGrassColor(double temperature, double humidity) {
		temperature *= temperature;
		int x = (int) ((1.0 - temperature) * 255.0);
		int y = (int) ((1.0 - humidity) * 255.0);
		return grassBuffer[y << 8 | x];
	}
}
