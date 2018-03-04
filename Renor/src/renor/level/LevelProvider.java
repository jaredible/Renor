package renor.level;

import renor.util.MathHelper;
import renor.vector.Vec3;

public class LevelProvider {
	public Level levelObj;
	public float[] lightBrightnessTable = new float[16];
	private float[] colorsSunriseSunset = new float[4];

	public final void registerLevel(Level level) {
		levelObj = level;
		generateLightBrightnessTable();
	}

	private void generateLightBrightnessTable() {
		float v0 = 0.0f;

		for (int i = 0; i <= 15; ++i) {
			float v1 = 1.0f - (float) i / 15.0f;
			lightBrightnessTable[i] = (1.0f - v1) / (v1 * 1.0f + 1.0f) * (1.0f - v0) + v0;
		}
	}

	public Vec3 getFogColor(float angle, float partialTickTime) {
		float n = MathHelper.cos(angle * (float) Math.PI * 2.0f) * 2.0f + 0.5f;

		if (n < 0.0f) n = 0.0f;

		if (n > 1.0f) n = 1.0f;

		float r = 0.7529412f;
		float g = 0.84705883f;
		float b = 1.0f;
		r *= n * 0.94f + 0.06f;
		g *= n * 0.94f + 0.06f;
		b *= n * 0.91f + 0.09f;
		return levelObj.getLevelVec3Pool().getVecFromPool((double) r, (double) g, (double) b);
	}

	public float calculateCelestialAngle(long time, float partialTickTime) {
		int v0 = (int) (time % 24000);
		float v1 = ((float) v0 + partialTickTime) / 24000.0f - 0.25f;

		if (v1 < 0.0f) ++v1;

		if (v1 > 1.0f) --v1;

		float v2 = v1;
		v1 = 1.0f - (float) ((Math.cos((double) v1 * Math.PI) + 1.0) / 2.0);
		v1 = v2 + (v1 - v2) / 3.0f;
		return v1;
	}

	public float[] calcSunriseSunsetColors(float angle, float partialTickTime) {
		float v0 = 0.4f;
		float v1 = MathHelper.cos(angle * (float) Math.PI * 2.0f) - 0.0f;
		float v2 = -0.0f;

		if (v1 >= v2 - v0 && v1 <= v2 + v0) {
			float v3 = (v1 - v2) / v0 * 0.5f + 0.5f;
			float v4 = 1.0f - (1.0f - MathHelper.sin(v3 * (float) Math.PI)) * 0.99f;
			v4 *= v4;
			colorsSunriseSunset[0] = v3 * 0.3f + 0.7f;
			colorsSunriseSunset[1] = v3 * v3 * 0.7f + 0.2f;
			colorsSunriseSunset[2] = v3 * v3 * 0.0f + 0.2f;
			colorsSunriseSunset[3] = v4;
			return colorsSunriseSunset;
		} else return null;
	}

	public boolean isSkyColored() {
		return true;
	}
}
