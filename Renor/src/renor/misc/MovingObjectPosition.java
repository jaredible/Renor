package renor.misc;

import renor.level.entity.Entity;
import renor.vector.Vec3;

public class MovingObjectPosition {
	public MovingObjectPosition.MovingObjectType typeOfHit;
	public Entity entityHit;
	public Vec3 hitVec;
	public int blockX;
	public int blockY;
	public int blockZ;
	public int sideHit;

	public MovingObjectPosition(int x, int y, int z, int side, Vec3 vec3, boolean flag) {
		blockX = x;
		blockY = y;
		blockZ = z;
		sideHit = side;
		hitVec = vec3.myVec3LocalPool.getVecFromPool(vec3.xCoord, vec3.yCoord, vec3.zCoord);

		typeOfHit = flag ? MovingObjectPosition.MovingObjectType.BLOCK : MovingObjectPosition.MovingObjectType.MISS;
	}

	public MovingObjectPosition(int x, int y, int z, int side, Vec3 vec3) {
		this(x, y, z, side, vec3, true);
	}

	public MovingObjectPosition(Entity entity, Vec3 vec3) {
		entityHit = entity;
		hitVec = vec3;

		typeOfHit = MovingObjectPosition.MovingObjectType.ENTITY;
	}

	public MovingObjectPosition(Entity entity) {
		this(entity, entity.levelObj.getLevelVec3Pool().getVecFromPool(entity.posX, entity.posY, entity.posZ));
	}

	public String toString() {
		return "HitResult{type=" + typeOfHit + ", x=" + blockX + ", y=" + blockY + ", z=" + blockZ + ", f=" + sideHit + ", pos=" + hitVec + ", entity=" + entityHit + '}';
	}

	public static enum MovingObjectType {
		MISS, BLOCK, ENTITY;
	}
}
