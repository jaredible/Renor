package renor.level.entity;

import java.util.List;
import java.util.Random;

import renor.aabb.AxisAlignedBB;
import renor.level.Level;
import renor.level.block.Block;
import renor.level.material.Material;
import renor.misc.DamageSource;
import renor.util.MathHelper;

public abstract class Entity {
	protected Random rand;
	public Level levelObj;
	public final AxisAlignedBB boundingBox;
	private static int nextEntityId = 0;
	public int entityId;
	public int ticksExisted;
	public int chunkCoordX;
	public int chunkCoordY;
	public int chunkCoordZ;
	public int hurtResistantTime;
	private int nextStepDistance;
	public double posX;
	public double posY;
	public double posZ;
	public double prevPosX;
	public double prevPosY;
	public double prevPosZ;
	public double lastTickPosX;
	public double lastTickPosY;
	public double lastTickPosZ;
	public double motionX;
	public double motionY;
	public double motionZ;
	public double renderDistanceWeight;
	public float rotationYaw;
	public float rotationPitch;
	public float prevRotationYaw;
	public float prevRotationPitch;
	public float width;
	public float height;
	public float distanceWalkedModified;
	public float prevDistanceWalkedModified;
	public float distanceWalkedOnStepModified;
	public float fallDistance;
	public float yOffset;
	public float ySize;
	public float entityCollisionReduction;
	public boolean onGround;
	public boolean isCollided;
	public boolean isCollidedHorizontally;
	public boolean isCollidedVertically;
	public boolean velocityChanged;
	public boolean noClip;
	public boolean isAirBorne;
	public boolean isDead;
	private boolean invulnerable;
	public boolean addedToChunk;
	public boolean ignoreFrustumCheck;
	public boolean preventEntitySpawning;
	private boolean firstUpdate;

	public Entity(Level level) {
		levelObj = level;

		rand = new Random();
		entityId = nextEntityId++;
		hurtResistantTime = 0;
		nextStepDistance = 1;
		renderDistanceWeight = 1.0;
		width = 0.6f;
		height = 1.8f;
		entityCollisionReduction = 0.0f;
		onGround = false;
		velocityChanged = false;
		noClip = false;
		invulnerable = false;
		addedToChunk = false;
		preventEntitySpawning = false;
		firstUpdate = true;
		boundingBox = AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		setPosition(0.0, 0.0, 0.0);
	}

	public void onUpdate() {
		onEntityUpdate();
	}

	public void onEntityUpdate() {
		levelObj.theProfiler.startSection("entityBaseTick");

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		prevRotationYaw = rotationYaw;
		prevRotationPitch = rotationPitch;
		prevDistanceWalkedModified = distanceWalkedModified;

		if (posY < -64.0) kill();

		firstUpdate = false;
		levelObj.theProfiler.endSection();
	}

	public void moveEntity(double mx, double my, double mz) {
		if (noClip) {
			boundingBox.offset(mx, my, mz);
			posX = (boundingBox.minX + boundingBox.maxX) / 2.0;
			posY = boundingBox.minY + (double) yOffset - (double) ySize;
			posZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;
		} else {
			levelObj.theProfiler.startSection("move");
			ySize *= 0.4f;
			double x = posX;
			double y = posY;
			double z = posZ;

			double mxx = mx;
			double myy = my;
			double mzz = mz;
			boolean var0 = onGround && isSneaking() && this instanceof EntityPlayer;
			List<AxisAlignedBB> aabbs = levelObj.getCollidingBoundingBoxes(this, boundingBox.addCoord(mx, my, mz));

			for (int i = 0; i < aabbs.size(); ++i)
				my = aabbs.get(i).calculateYOffset(boundingBox, my);

			boundingBox.offset(0.0, my, 0.0);

			int i;

			for (i = 0; i < aabbs.size(); ++i)
				mx = aabbs.get(i).calculateXOffset(boundingBox, mx);

			boundingBox.offset(mx, 0.0, 0.0);

			for (i = 0; i < aabbs.size(); ++i)
				mz = aabbs.get(i).calculateZOffset(boundingBox, mz);

			boundingBox.offset(0.0, 0.0, mz);

			double mxxx;
			double myyy;
			int n;
			double mzzz;

			levelObj.theProfiler.endStartSection("rest");
			posX = (boundingBox.minX + boundingBox.maxX) / 2.0;
			posY = boundingBox.minY + (double) yOffset - (double) ySize;
			posZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;
			isCollidedHorizontally = mxx != mx || mzz != mz;
			isCollidedVertically = myy != my;
			isCollided = isCollidedHorizontally || isCollidedVertically;
			onGround = myy != my && myy < 0.0;

			if (mxx != mx) motionX = 0.0;

			if (myy != my) motionY = 0.0;

			if (mzz != mz) motionZ = 0.0;

			mxxx = posX - x;
			myyy = posY - y;
			mzzz = posZ - z;

			if (canTriggerWalking() && !var0) {
				int xx = MathHelper.floor_double(posX);
				n = MathHelper.floor_double(posY - 0.20000000298023224 - (double) yOffset);
				int zz = MathHelper.floor_double(posZ);
				// blockId
				int id = levelObj.getBlockId(xx, n, zz);

				myyy = 0.0;

				distanceWalkedModified = (float) ((double) distanceWalkedModified + (double) MathHelper.sqrt_double(mxxx * mxxx + mzzz * mzzz) * 0.6);
				distanceWalkedOnStepModified = (float) ((double) distanceWalkedOnStepModified + (double) MathHelper.sqrt_double(mxxx * mxxx + myyy * myyy + mzzz * mzzz) * 0.6);

				if (distanceWalkedOnStepModified > (float) nextStepDistance && id > 0) {
					nextStepDistance = (int) distanceWalkedOnStepModified + 1;

					// play step sound here
					Block.blocksList[id].onEntityWalking(levelObj, xx, n, zz, this);
				}
			}

			doBlockCollisions();

			levelObj.theProfiler.endSection();
		}
	}

	protected void doBlockCollisions() {
		int minX = MathHelper.floor_double(boundingBox.minX + 0.001);
		int minY = MathHelper.floor_double(boundingBox.minY + 0.001);
		int minZ = MathHelper.floor_double(boundingBox.minZ + 0.001);
		int maxX = MathHelper.floor_double(boundingBox.maxX - 0.001);
		int maxY = MathHelper.floor_double(boundingBox.maxY - 0.001);
		int maxZ = MathHelper.floor_double(boundingBox.maxZ - 0.001);

		if (levelObj.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
			for (int x = minX; x <= maxX; ++x) {
				for (int y = minY; y <= maxY; ++y) {
					for (int z = minZ; z <= maxZ; ++z) {
						// blockId
						int id = levelObj.getBlockId(x, y, z);

						if (id > 0) Block.blocksList[id].onEntityCollidedWithBlock(levelObj, x, y, z, this);
					}
				}
			}
		}
	}

	public void moveFlying(float strafe, float forward, float p0) {
		float d = strafe * strafe + forward * forward;

		if (d >= 1.0e-4f) {
			d = MathHelper.sqrt_float(d);

			if (d < 1.0f) d = 1.0f;

			d = p0 / d;
			strafe *= d;
			forward *= d;
			float v0 = MathHelper.sin(rotationYaw * (float) Math.PI / 180.0f);
			float v1 = MathHelper.cos(rotationYaw * (float) Math.PI / 180.0f);
			motionX += (double) (strafe * v1 - forward * v0);
			motionZ += (double) (forward * v1 + strafe * v0);
		}
	}

	public void setPosition(double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
		float w = width / 2.0f;
		float h = height;
		boundingBox.setBounds(x - (double) w, y - (double) yOffset + (double) ySize, z - (double) w, x + (double) w, y - (double) yOffset + (double) ySize + (double) h, z + (double) w);
	}

	public void setAngles(float yaw, float pitch) {
		float oldYaw = rotationYaw;
		float oldPitch = rotationPitch;
		rotationYaw = (float) ((double) rotationYaw + (double) yaw * 0.15);
		rotationPitch = (float) ((double) rotationPitch - (double) pitch * 0.15);

		if (rotationPitch < -90.0f) rotationPitch = -90.0f;
		if (rotationPitch > 90.0f) rotationPitch = 90.0f;

		prevRotationYaw += rotationYaw - oldYaw;
		prevRotationPitch += rotationPitch - oldPitch;
	}

	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
		lastTickPosX = prevPosX = posX = x;
		lastTickPosY = prevPosY = posY = y + (double) yOffset;
		lastTickPosZ = prevPosZ = posZ = z;
		rotationYaw = yaw;
		rotationPitch = pitch;
		setPosition(posX, posY, posZ);
	}

	public void copyLocationAndAnglesFrom(Entity entity) {
		setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
	}

	public float getDistanceToEntity(Entity entity) {
		float dx = (float) (posX - entity.posX);
		float dy = (float) (posY - entity.posY);
		float dz = (float) (posZ - entity.posZ);
		return MathHelper.sqrt_float(dx * dx + dy * dy + dz * dz);
	}

	public double getDistanceSq(double x, double y, double z) {
		double dx = posX - x;
		double dy = posY - y;
		double dz = posZ - z;
		return dx * dx + dy * dy + dz * dz;
	}

	public double getDistance(double x, double y, double z) {
		double dx = posX - x;
		double dy = posY - y;
		double dz = posZ - z;
		return (double) MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz);
	}

	public double getDistanceSqToEntity(Entity entity) {
		double dx = posX - entity.posX;
		double dy = posY - entity.posY;
		double dz = posZ - entity.posZ;
		return dx * dx + dy * dy + dz * dz;
	}

	protected void setSize(float width, float height) {
		float w;

		if (width != this.width || height != this.height) {
			w = this.width;
			this.width = width;
			this.height = height;
			boundingBox.maxX = boundingBox.minX + (double) this.width;
			boundingBox.maxY = boundingBox.minY + (double) this.height;
			boundingBox.maxZ = boundingBox.minZ + (double) this.width;

			if (this.width > w && !firstUpdate && !levelObj.isClient) moveEntity((double) (w - this.width), 0.0, (double) (w - this.width));
		}
	}

	public boolean isInRangeToRender3D(double x, double y, double z) {
		double dx = posX - x;
		double dy = posY - y;
		double dz = posZ - z;
		double l = dx * dx + dy * dy + dz * dz;
		return isInRangeToRenderDist(l);
	}

	public boolean isInRangeToRenderDist(double length) {
		double n = boundingBox.getAverageEdgeLength();
		n *= 64.0 * renderDistanceWeight;
		return length < n * n;
	}

	public int getBrightnessForRender() {
		int x = MathHelper.floor_double(posX);
		int z = MathHelper.floor_double(posZ);

		if (levelObj.blockExists(x, 0, z)) {
			double v0 = (boundingBox.maxY - boundingBox.minY) * 0.66;
			int v1 = MathHelper.floor_double(posY - (double) yOffset + v0);
			return levelObj.getLightBrightnessForSkyBlocks(x, v1, z, 0);
		} else return 0;
	}

	public float getBrightness() {
		int x = MathHelper.floor_double(posX);
		int z = MathHelper.floor_double(posZ);

		if (levelObj.blockExists(x, 0, z)) {
			double v0 = (boundingBox.maxY - boundingBox.minY) * 0.66;
			int v1 = MathHelper.floor_double(posY - (double) yOffset + v0);
			return levelObj.getLightBrightness(x, v1, z);
		} else return 0.0f;
	}

	public void setDead() {
		isDead = true;
	}

	protected void kill() {
		setDead();
	}

	protected boolean pushOutOfBlocks(double x, double y, double z) {
		int xInt = MathHelper.floor_double(x);
		int yInt = MathHelper.floor_double(y);
		int zInt = MathHelper.floor_double(z);
		double xx = x - (double) xInt;
		double yy = y - (double) yInt;
		double zz = z - (double) zInt;
		List<AxisAlignedBB> aabbs = levelObj.getCollidingBlockBounds(boundingBox);

		if (aabbs.isEmpty() && !levelObj.a(xInt, yInt, zInt)) return false;
		else {
			boolean v0 = !levelObj.a(xInt - 1, yInt, zInt);
			boolean v1 = !levelObj.a(xInt + 1, yInt, zInt);
			boolean v2 = !levelObj.a(xInt, yInt - 1, zInt);
			boolean v3 = !levelObj.a(xInt, yInt + 1, zInt);
			boolean v4 = !levelObj.a(xInt, yInt, zInt - 1);
			boolean v5 = !levelObj.a(xInt, yInt, zInt + 1);
			byte side = 3;
			double v6 = 9999.0;

			if (v0 && xx < v6) {
				v6 = xx;
				side = 0;
			}

			if (v1 && 1.0 - xx < v6) {
				v6 = 1.0 - xx;
				side = 1;
			}

			if (v3 && 1.0 - yy < v6) {
				v6 = 1.0 - yy;
				side = 3;
			}

			if (v4 && zz < v6) {
				v6 = zz;
				side = 4;
			}

			if (v5 && 1.0 - zz < v6) {
				v6 = 1.0 - zz;
				side = 5;
			}

			float n = rand.nextFloat() * 0.2f + 0.1f;

			if (side == 0) motionX = (double) (-n);

			if (side == 1) motionX = (double) n;

			if (side == 2) motionY = (double) (-n);

			if (side == 3) motionY = (double) n;

			if (side == 4) motionZ = (double) (-n);

			if (side == 5) motionZ = (double) n;

			return true;
		}
	}

	public void applyEntityCollision(Entity entity) {
		double dx = entity.posX - posX;
		double dz = entity.posZ - posZ;
		double d = MathHelper.abs_max(dx, dz);

		if (d >= 0.009999999776482582) {
			d = (double) MathHelper.sqrt_double(d);
			dx /= d;
			dz /= d;
			double n = 1.0 / d;

			if (n > 1.0) n = 1.0;

			dx *= n;
			dz *= n;
			dx *= 0.05000000074505806;
			dz *= 0.05000000074505806;
			dx *= (double) (1.0f - entityCollisionReduction);
			dz *= (double) (1.0f - entityCollisionReduction);
			addVelocity(-dx, 0.0, -dz);
			entity.addVelocity(dx, 0.0, dz);
		}
	}

	public void addVelocity(double mx, double my, double mz) {
		motionX += mx;
		motionY += my;
		motionZ += mz;
	}

	public boolean canBePushed() {
		return false;
	}

	protected boolean canTriggerWalking() {
		return true;
	}

	public boolean isEntityAlive() {
		return !isDead;
	}

	public float getEyeHeight() {
		return 0.0f;
	}

	public void playSound(String name, float volume, float pitch) {
	}

	public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		if (isEntityInvulnerable()) return false;
		else {
			setBeenAttacked();
			return false;
		}
	}

	protected void setBeenAttacked() {
		velocityChanged = true;
	}

	public boolean isEntityInvulnerable() {
		return invulnerable;
	}

	public void onKillEntity(EntityLiving living) {
	}

	public boolean isSneaking() {
		return false;
	}

	public boolean isInvisible() {
		return false;
	}

	public boolean isInsideOfMaterial(Material material) {
		return false;
	}

	public float getCollisionBorderSize() {
		return 0.1f;
	}

	public boolean canBeCollidedWith() {
		return false;
	}

	public boolean canAttackWithItem() {
		return true;
	}

	public boolean isEntityInsideOpaqueBlock() {
		for (int i = 0; i < 8; ++i) {
			float xo = ((float) ((i >> 0) % 2) - 0.5f) * width * 0.8f;
			float yo = ((float) ((i >> 1) % 2) - 0.5f) * 0.1f;
			float zo = ((float) ((i >> 2) % 2) - 0.5f) * width * 0.8f;
			int x = MathHelper.floor_double(posX + (double) xo);
			int y = MathHelper.floor_double(posY + (double) getEyeHeight() + (double) yo);
			int z = MathHelper.floor_double(posZ + (double) zo);

			if (levelObj.isBlockNormalCube(x, y, z)) return true;
		}

		return false;
	}

	public void handleHealthUpdate(byte status) {
	}

	public void addToPlayerScore(Entity entity, int score) {
	}

	public void onCollideWithPlayer(EntityPlayer player) {
	}
}
