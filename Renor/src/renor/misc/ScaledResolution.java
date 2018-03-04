package renor.misc;

import renor.util.MathHelper;

public class ScaledResolution {
	private int scaledWidth;
	private int scaledHeight;
	private int scaleFactor;
	private double scaledWidthD;
	private double scaledHeightD;

	public ScaledResolution(GameSettings gameSettings, int width, int height) {
		scaledWidth = width;
		scaledHeight = height;
		scaleFactor = 1;
		int n = gameSettings.guiScale;

		if (n == 0) n = 1000;

		while (scaleFactor < n && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240)
			++scaleFactor;

		scaledWidthD = (double) scaledWidth / (double) scaleFactor;
		scaledHeightD = (double) scaledHeight / (double) scaleFactor;
		scaledWidth = MathHelper.ceiling_double_int(scaledWidthD);
		scaledHeight = MathHelper.ceiling_double_int(scaledHeightD);
	}

	public int getScaledWidth() {
		return scaledWidth;
	}

	public int getScaledHeight() {
		return scaledHeight;
	}

	public double getScaledWidth_double() {
		return scaledWidthD;
	}

	public double getScaledHeight_double() {
		return scaledHeightD;
	}

	public int getScaleFactor() {
		return scaleFactor;
	}
}
