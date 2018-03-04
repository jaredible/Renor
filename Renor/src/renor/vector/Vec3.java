package renor.vector;

import renor.util.MathHelper;

public class Vec3 {
	public static final Vec3Pool fakePool = new Vec3Pool(-1, -1);
	public final Vec3Pool myVec3LocalPool;
	public double xCoord;
	public double yCoord;
	public double zCoord;

	public static Vec3 createVectorHelper(double xCoord, double yCoord, double zCoord) {
		return new Vec3(fakePool, xCoord, yCoord, zCoord);
	}

	protected Vec3(Vec3Pool vec3Pool, double xCoord, double yCoord, double zCoord) {
		myVec3LocalPool = vec3Pool;
		if (xCoord == -0.0) xCoord = 0.0;
		if (yCoord == -0.0) yCoord = 0.0;
		if (zCoord == -0.0) zCoord = 0.0;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	protected Vec3 setComponents(double xCoord, double yCoord, double zCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		return this;
	}

	public Vec3 subtract(Vec3 vec3) {
		return myVec3LocalPool.getVecFromPool(vec3.xCoord - xCoord, vec3.yCoord - yCoord, vec3.zCoord - zCoord);
	}

	public Vec3 normalize() {
		double l = (double) MathHelper.sqrt_double(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
		return l < 1.0e-4 ? myVec3LocalPool.getVecFromPool(0.0, 0.0, 0.0) : myVec3LocalPool.getVecFromPool(xCoord / l, yCoord / l, zCoord / l);
	}

	public double dotProduct(Vec3 vec3) {
		return xCoord * vec3.xCoord + yCoord * vec3.yCoord + zCoord * vec3.zCoord;
	}

	public Vec3 crossProduct(Vec3 vec3) {
		return myVec3LocalPool.getVecFromPool(yCoord * vec3.zCoord - zCoord * vec3.yCoord, zCoord * vec3.xCoord - xCoord * vec3.zCoord, xCoord * vec3.yCoord - yCoord * vec3.xCoord);
	}

	public Vec3 addVector(double x, double y, double z) {
		return myVec3LocalPool.getVecFromPool(xCoord + x, yCoord + y, zCoord + z);
	}

	public double distanceTo(Vec3 vec3) {
		double dx = vec3.xCoord - xCoord;
		double dy = vec3.yCoord - yCoord;
		double dz = vec3.zCoord - zCoord;
		return (double) MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
	}

	public double squareDistanceTo(Vec3 vec3) {
		double dx = vec3.xCoord - xCoord;
		double dy = vec3.yCoord - yCoord;
		double dz = vec3.zCoord - zCoord;
		return dx * dx + dy * dy + dz * dz;
	}

	public double squareDistanceTo(double x, double y, double z) {
		double dx = x - xCoord;
		double dy = y - yCoord;
		double dz = z - zCoord;
		return dx * dx + dy * dy + dz * dz;
	}

	public double lengthVector() {
		return (double) MathHelper.sqrt_double(xCoord * xCoord + yCoord * yCoord + zCoord * zCoord);
	}

	public Vec3 getIntermediateWithXValue(Vec3 par1Vec3, double x) {
		double dx = par1Vec3.xCoord - xCoord;
		double dy = par1Vec3.yCoord - yCoord;
		double dz = par1Vec3.zCoord - zCoord;

		if (dx * dx < 1.0000000116860974e-7) return null;
		else {
			double n = (x - xCoord) / dx;
			return n >= 0.0 && n <= 1.0 ? myVec3LocalPool.getVecFromPool(xCoord + dx * n, yCoord + dy * n, zCoord + dz * n) : null;
		}
	}

	public Vec3 getIntermediateWithYValue(Vec3 par1Vec3, double y) {
		double dx = par1Vec3.xCoord - xCoord;
		double dy = par1Vec3.yCoord - yCoord;
		double dz = par1Vec3.zCoord - zCoord;

		if (dy * dy < 1.0000000116860974e-7) return null;
		else {
			double n = (y - yCoord) / dy;
			return n >= 0.0 && n <= 1.0 ? myVec3LocalPool.getVecFromPool(xCoord + dx * n, yCoord + dy * n, zCoord + dz * n) : null;
		}
	}

	public Vec3 getIntermediateWithZValue(Vec3 par1Vec3, double z) {
		double dx = par1Vec3.xCoord - xCoord;
		double dy = par1Vec3.yCoord - yCoord;
		double dz = par1Vec3.zCoord - zCoord;

		if (dz * dz < 1.0000000116860974e-7) return null;
		else {
			double n = (z - zCoord) / dz;
			return n >= 0.0 && n <= 1.0 ? myVec3LocalPool.getVecFromPool(xCoord + dx * n, yCoord + dy * n, zCoord + dz * n) : null;
		}
	}

	public void rotateAroundX(float degree) {
		float v0 = MathHelper.cos(degree);
		float v1 = MathHelper.sin(degree);
		double v2 = xCoord;
		double v3 = yCoord * (double) v0 + zCoord * (double) v1;
		double v4 = zCoord * (double) v0 - yCoord * (double) v1;
		xCoord = v2;
		yCoord = v3;
		zCoord = v4;
	}

	public void rotateAroundY(float degree) {
		float v0 = MathHelper.cos(degree);
		float v1 = MathHelper.sin(degree);
		double v2 = xCoord * (double) v0 + zCoord * (double) v1;
		double v3 = yCoord;
		double v4 = zCoord * (double) v0 - xCoord * (double) v1;
		xCoord = v2;
		yCoord = v3;
		zCoord = v4;
	}

	public void rotateAroundZ(float degree) {
		float v0 = MathHelper.cos(degree);
		float v1 = MathHelper.sin(degree);
		double v2 = xCoord * (double) v0 + yCoord * (double) v1;
		double v3 = yCoord * (double) v0 - xCoord * (double) v1;
		double v4 = zCoord;
		xCoord = v2;
		yCoord = v3;
		zCoord = v4;
	}

	public String toString() {
		return "(" + xCoord + ", " + yCoord + ", " + zCoord + ")";
	}
}
