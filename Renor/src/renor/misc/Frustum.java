package renor.misc;

import renor.aabb.AxisAlignedBB;
import renor.util.ClippingHelper;
import renor.util.ClippingHelperImpl;

public class Frustum implements ICamera {
	private ClippingHelper clippingHelper = ClippingHelperImpl.getInstance();
	private double xPosition;
	private double yPosition;
	private double zPosition;

	public void setPosition(double x, double y, double z) {
		xPosition = x;
		yPosition = y;
		zPosition = z;
	}

	public boolean isBoundingBoxInFrustum(AxisAlignedBB axisAlignedBB) {
		return isBoxInFrustum(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
	}

	public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return clippingHelper.isBoxInFrustrum(minX - xPosition, minY - yPosition, minZ - zPosition, maxX - xPosition, maxY - yPosition, maxZ - zPosition);
	}
}
