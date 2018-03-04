package renor.misc;

import renor.aabb.AxisAlignedBB;

public abstract interface ICamera {
	public abstract void setPosition(double x, double y, double z);

	public abstract boolean isBoundingBoxInFrustum(AxisAlignedBB axisAlignedBB);
}
