package renor.misc;

public class NibbleArray {
	public final byte[] data;
	private final int depthBits;
	private final int depthBitsPlusFour;

	public NibbleArray(int p0, int p1) {
		data = new byte[p0 >> 1];
		depthBits = p1;
		depthBitsPlusFour = p1 + 4;
	}

	public NibbleArray(byte[] p0, int p1) {
		data = p0;
		depthBits = p1;
		depthBitsPlusFour = p1 + 4;
	}

	public int get(int x, int y, int z) {
		int v0 = y << depthBitsPlusFour | z << depthBits | x;
		int v1 = v0 >> 1;
		int v2 = v0 & 1;
		return v2 == 0 ? data[v1] & 15 : data[v1] >> 4 & 15;
	}

	public void set(int x, int y, int z, int value) {
		int v0 = y << depthBitsPlusFour | z << depthBits | x;
		int v1 = v0 >> 1;
		int v2 = v0 & 1;

		if (v2 == 0) data[v1] = (byte) (data[v1] & 240 | value & 15);
		else data[v1] = (byte) (data[v1] & 15 | (value & 15) << 4);
	}
}
