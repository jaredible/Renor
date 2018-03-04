package renor.aabb;

import renor.misc.MovingObjectPosition;
import renor.vector.Vec3;

public class AxisAlignedBB {
	private static final ThreadLocal<AABBPool> theAABBLocalPool = new ThreadLocal<AABBPool>() {
		protected AABBPool initialValue() {
			return new AABBPool(300, 2000);
		}
	};
	public double minX;
	public double minY;
	public double minZ;
	public double maxX;
	public double maxY;
	public double maxZ;

	public static AxisAlignedBB getBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static AABBPool getAABBPool() {
		return theAABBLocalPool.get();
	}

	protected AxisAlignedBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public AxisAlignedBB setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		return this;
	}

	public AxisAlignedBB addCoord(double x, double y, double z) {
		double minX = this.minX;
		double minY = this.minY;
		double minZ = this.minZ;
		double maxX = this.maxX;
		double maxY = this.maxY;
		double maxZ = this.maxZ;

		if (x < 0.0) minX += x;
		if (x > 0.0) maxX += x;
		if (y < 0.0) minY += y;
		if (y > 0.0) maxY += y;
		if (z < 0.0) minZ += z;
		if (z > 0.0) maxZ += z;

		return getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public AxisAlignedBB expand(double x, double y, double z) {
		double minX = this.minX - x;
		double minY = this.minY - y;
		double minZ = this.minZ - z;
		double maxX = this.maxX + x;
		double maxY = this.maxY + y;
		double maxZ = this.maxZ + z;
		return getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public AxisAlignedBB getOffsetBoundingBox(double x, double y, double z) {
		return getAABBPool().getAABB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
	}

	public double calculateXOffset(AxisAlignedBB axisAlignedBB, double x) {
		if (axisAlignedBB.maxY > minY && axisAlignedBB.minY < maxY) {
			if (axisAlignedBB.maxZ > minZ && axisAlignedBB.minZ < maxZ) {
				double dx;

				if (x > 0.0 && axisAlignedBB.maxX <= minX) {
					dx = minX - axisAlignedBB.maxX;

					if (dx < x) x = dx;
				}

				if (x < 0.0 && axisAlignedBB.minX >= maxX) {
					dx = maxX - axisAlignedBB.minX;

					if (dx > x) x = dx;
				}

				return x;
			} else return x;
		} else return x;
	}

	public double calculateYOffset(AxisAlignedBB axisAlignedBB, double y) {
		if (axisAlignedBB.maxX > minX && axisAlignedBB.minX < maxX) {
			if (axisAlignedBB.maxZ > minZ && axisAlignedBB.minZ < maxZ) {
				double dy;

				if (y > 0.0 && axisAlignedBB.maxY <= minY) {
					dy = minY - axisAlignedBB.maxY;

					if (dy < y) y = dy;
				}

				if (y < 0.0 && axisAlignedBB.minY >= maxY) {
					dy = maxY - axisAlignedBB.minY;

					if (dy > y) y = dy;
				}

				return y;
			} else return y;
		} else return y;
	}

	public double calculateZOffset(AxisAlignedBB axisAlignedBB, double z) {
		if (axisAlignedBB.maxX > minX && axisAlignedBB.minX < maxX) {
			if (axisAlignedBB.maxY > minY && axisAlignedBB.minY < maxY) {
				double dz;

				if (z > 0.0 && axisAlignedBB.maxZ <= minZ) {
					dz = minZ - axisAlignedBB.maxZ;

					if (dz < z) z = dz;
				}

				if (z < 0.0 && axisAlignedBB.minZ >= maxZ) {
					dz = maxZ - axisAlignedBB.minZ;

					if (dz > z) z = dz;
				}

				return z;
			} else return z;
		} else return z;
	}

	public boolean intersectsWith(AxisAlignedBB axisAlignedBB) {
		return axisAlignedBB.maxX > minX && axisAlignedBB.minX < maxX ? (axisAlignedBB.maxY > minY && axisAlignedBB.minY < maxY ? axisAlignedBB.maxZ > minZ && axisAlignedBB.minZ < maxZ : false) : false;
	}

	public AxisAlignedBB offset(double x, double y, double z) {
		minX += x;
		minY += y;
		minZ += z;
		maxX += x;
		maxY += y;
		maxZ += z;
		return this;
	}

	public boolean isVecInside(Vec3 vec3) {
		return vec3.xCoord > minX && vec3.xCoord < maxX ? (vec3.yCoord > minY && vec3.yCoord < maxY ? vec3.zCoord > minZ && vec3.zCoord < maxZ : false) : false;
	}

	public double getAverageEdgeLength() {
		double dx = maxX - minX;
		double dy = maxY - minY;
		double dz = maxZ - minZ;
		return (dx + dy + dz) / 3.0;
	}

	public AxisAlignedBB contract(double x, double y, double z) {
		double minX = this.minX + x;
		double minY = this.minY + y;
		double minZ = this.minZ + z;
		double maxX = this.maxX - x;
		double maxY = this.maxY - y;
		double maxZ = this.maxZ - z;
		return getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public AxisAlignedBB copy() {
		return getAABBPool().getAABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public MovingObjectPosition calculateIntercept(Vec3 startVec, Vec3 endVec) {
		Vec3 v0 = startVec.getIntermediateWithXValue(endVec, minX);
		Vec3 v1 = startVec.getIntermediateWithXValue(endVec, maxX);
		Vec3 v2 = startVec.getIntermediateWithYValue(endVec, minY);
		Vec3 v3 = startVec.getIntermediateWithYValue(endVec, maxY);
		Vec3 v4 = startVec.getIntermediateWithZValue(endVec, minZ);
		Vec3 v5 = startVec.getIntermediateWithZValue(endVec, maxZ);

		if (!isVecInYZ(v0)) v0 = null;

		if (!isVecInYZ(v1)) v1 = null;

		if (!isVecInXZ(v2)) v2 = null;

		if (!isVecInXZ(v3)) v3 = null;

		if (!isVecInXY(v4)) v4 = null;

		if (!isVecInXY(v5)) v5 = null;

		Vec3 hitVec = null;

		if (v0 != null && (hitVec == null || startVec.squareDistanceTo(v0) < startVec.squareDistanceTo(hitVec))) hitVec = v0;

		if (v1 != null && (hitVec == null || startVec.squareDistanceTo(v1) < startVec.squareDistanceTo(hitVec))) hitVec = v1;

		if (v2 != null && (hitVec == null || startVec.squareDistanceTo(v2) < startVec.squareDistanceTo(hitVec))) hitVec = v2;

		if (v3 != null && (hitVec == null || startVec.squareDistanceTo(v3) < startVec.squareDistanceTo(hitVec))) hitVec = v3;

		if (v4 != null && (hitVec == null || startVec.squareDistanceTo(v4) < startVec.squareDistanceTo(hitVec))) hitVec = v4;

		if (v5 != null && (hitVec == null || startVec.squareDistanceTo(v5) < startVec.squareDistanceTo(hitVec))) hitVec = v5;

		if (hitVec == null) return null;
		else {
			byte side = -1;

			if (hitVec == v0) side = 4;

			if (hitVec == v1) side = 5;

			if (hitVec == v2) side = 0;

			if (hitVec == v3) side = 1;

			if (hitVec == v4) side = 2;

			if (hitVec == v5) side = 3;

			return new MovingObjectPosition(0, 0, 0, side, hitVec);
		}
	}

	private boolean isVecInYZ(Vec3 vec3) {
		return vec3 == null ? false : vec3.yCoord >= minY && vec3.yCoord <= maxY && vec3.zCoord >= minZ && vec3.zCoord <= maxZ;
	}

	private boolean isVecInXZ(Vec3 vec3) {
		return vec3 == null ? false : vec3.xCoord >= minX && vec3.xCoord <= maxX && vec3.zCoord >= minZ && vec3.zCoord <= maxZ;
	}

	private boolean isVecInXY(Vec3 vec3) {
		return vec3 == null ? false : vec3.xCoord >= minX && vec3.xCoord <= maxX && vec3.yCoord >= minY && vec3.yCoord <= maxY;
	}

	public void setBB(AxisAlignedBB axisAlignedBB) {
		minX = axisAlignedBB.minX;
		minY = axisAlignedBB.minY;
		minZ = axisAlignedBB.minZ;
		maxX = axisAlignedBB.maxX;
		maxY = axisAlignedBB.maxY;
		maxZ = axisAlignedBB.maxZ;
	}

	public String toString() {
		return "box[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", " + maxZ + "]";
	}
}
