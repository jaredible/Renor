package renor.util;

public class ClippingHelper {
	public float[][] frustum = new float[16][16];
	public float[] projectionMatrix = new float[16];
	public float[] modelviewMatrix = new float[16];
	public float[] clippingMatrix = new float[16];

	public boolean isBoxInFrustrum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		for (int i = 0; i < 6; ++i) {
			if ((double) frustum[i][0] * minX + (double) frustum[i][1] * minY + (double) frustum[i][2] * minZ + (double) frustum[i][3] <= 0.0 && (double) frustum[i][0] * maxX + (double) frustum[i][1] * minY + (double) frustum[i][2] * minZ + (double) frustum[i][3] <= 0.0 && (double) frustum[i][0] * minX + (double) frustum[i][1] * maxY + (double) frustum[i][2] * minZ + (double) frustum[i][3] <= 0.0
					&& (double) frustum[i][0] * maxX + (double) frustum[i][1] * maxY + (double) frustum[i][2] * minZ + (double) frustum[i][3] <= 0.0 && (double) frustum[i][0] * minX + (double) frustum[i][1] * minY + (double) frustum[i][2] * maxZ + (double) frustum[i][3] <= 0.0 && (double) frustum[i][0] * maxX + (double) frustum[i][1] * minY + (double) frustum[i][2] * maxZ + (double) frustum[i][3] <= 0.0
					&& (double) frustum[i][0] * minX + (double) frustum[i][1] * maxY + (double) frustum[i][2] * maxZ + (double) frustum[i][3] <= 0.0 && (double) frustum[i][0] * maxX + (double) frustum[i][1] * maxY + (double) frustum[i][2] * maxZ + (double) frustum[i][3] <= 0.0) return false;
		}

		return true;
	}
}
