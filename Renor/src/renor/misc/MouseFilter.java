package renor.misc;

public class MouseFilter {
	private float a;
	private float b;
	private float c;

	public float smooth(float n, float inc) {
		a += n;
		n = (a - b) * inc;
		c += (n - c) * 0.5f;

		if ((n > 0.0f && n > c) || (n < 0.0f && n < c)) n = c;

		b += n;
		return n;
	}
}
