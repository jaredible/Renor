package renor.util;

public class MathHelper {
	private static float[] SIN_TABLE = new float[65536];

	public static final float sin(float n) {
		return SIN_TABLE[(int) (n * 10430.378f) & 65535];
	}

	public static final float cos(float n) {
		return SIN_TABLE[(int) (n * 10430.378f + 16384.0f) & 65535];
	}

	public static final float sqrt_float(float n) {
		return (float) Math.sqrt(n);
	}

	public static final float sqrt_double(double n) {
		return (float) Math.sqrt(n);
	}

	public static int floor_double(double n) {
		int result = (int) n;
		return n < (double) result ? result - 1 : result;
	}

	public static int ceiling_double_int(double n) {
		int result = (int) n;
		return n > (double) result ? result + 1 : result;
	}

	public static int abs_int(int n) {
		return n >= 0 ? n : -n;
	}

	public static double abs_max(double n1, double n2) {
		if (n1 < 0.0) n1 = -n1;

		if (n2 < 0.0) n2 = -n2;

		return n1 > n2 ? n1 : n2;
	}

	public static int bucketInt(int i, int bucketSize) {
		return i < 0 ? -((-i - 1) / bucketSize) - 1 : i / bucketSize;
	}

	public static float wrapAngleTo180_float(float angle) {
		angle %= 360.0f;

		if (angle >= 180.0f) angle -= 360.0f;

		if (angle < -180.0f) angle += 360.0f;

		return angle;
	}

	static {
		for (int i = 0; i < 65536; ++i)
			SIN_TABLE[i] = (float) Math.sin((double) i * Math.PI * 2.0 / 65536.0);
	}
}
