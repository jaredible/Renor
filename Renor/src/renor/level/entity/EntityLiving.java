package renor.level.entity;

import java.util.List;

import renor.level.Level;
import renor.level.block.Block;
import renor.misc.DamageSource;
import renor.misc.MovingObjectPosition;
import renor.util.MathHelper;
import renor.vector.Vec3;

public abstract class EntityLiving extends Entity {
	private EntityLiving entityLivingToAttack = null;
	private EntityLiving lastAttacker = null;
	protected EntityPlayer attackingPlayer = null;
	private int jumpTicks = 0;
	public int deathTime = 0;
	protected int entityAge = 0;
	private int aiTalkInterval = 80;
	public int livingSoundTime;
	public int maxHurtResistantTime = 20;
	public int hurtTime;
	public int maxHurtTime;
	private int revengeTimer;
	protected int recentlyHit = 0;
	protected int scoreValue = 0;
	protected float health;
	public float prevHealth;
	public float cameraPitch;
	public float prevCameraPitch;
	public float landMovementFactor = 0.1f;
	public float jumpMovementFactor = 0.02f;
	protected float moveForward;
	protected float moveStrafe;
	private float aiMoveSpeed;
	public float attackedAtYaw = 0.0f;
	public float limbSwingAmount;
	public float prevLimbSwingAmount;
	public float limbSwing;
	protected float lastDamage;
	protected boolean isJumping = false;
	protected boolean isSprinting = false;

	public EntityLiving(Level level) {
		super(level);

		rotationYaw = (float) (Math.random() * Math.PI * 2.0);
		preventEntitySpawning = true;
		setPosition(posX, posY, posZ);
		setHealth(getMaxHealth());
	}

	public void onUpdate() {
		super.onUpdate();

		onLivingUpdate();

		levelObj.theProfiler.startSection("rangeChecks");

		while (rotationYaw - prevRotationYaw < -180.0f)
			prevRotationYaw -= 360.0f;

		while (rotationYaw - prevRotationYaw >= 180.0f)
			prevRotationYaw += 360.0f;

		while (rotationPitch - prevRotationPitch < -180.0f)
			prevRotationPitch -= 360.0f;

		while (rotationPitch - prevRotationPitch >= 180.0f)
			prevRotationPitch += 360.0f;

		levelObj.theProfiler.endSection();
	}

	public void onEntityUpdate() {
		super.onEntityUpdate();
		levelObj.theProfiler.startSection("livingEntityBaseTick");

		if (isEntityAlive() && rand.nextInt(1000) < livingSoundTime++) {
			livingSoundTime = -getTalkInterval();
			playLivingSound();
		}

		if (isEntityAlive() && isEntityInsideOpaqueBlock()) attackEntityFrom(DamageSource.inWall, 1.0f);

		prevCameraPitch = cameraPitch;

		if (hurtTime > 0) --hurtTime;

		if (hurtResistantTime > 0) --hurtResistantTime;

		if (getHealth() <= 0.0f) onDeathUpdate();

		if (recentlyHit > 0) --recentlyHit;
		else attackingPlayer = null;

		if (lastAttacker != null && !lastAttacker.isEntityAlive()) lastAttacker = null;

		if (entityLivingToAttack != null) {
			if (!entityLivingToAttack.isEntityAlive()) setRevengeTarget(null);
			else if (revengeTimer > 0) --revengeTimer;
			else setRevengeTarget(null);
		}

		prevRotationYaw = rotationYaw;
		prevRotationPitch = rotationPitch;
		levelObj.theProfiler.endSection();
	}

	protected void kill() {
		attackEntityFrom(DamageSource.outOfLevel, 4.0f);
	}

	public boolean canBePushed() {
		return !isDead;
	}

	public boolean isEntityAlive() {
		return !isDead && getHealth() > 0.0f;
	}

	public void onLivingUpdate() {
		if (jumpTicks > 0) --jumpTicks;

		if (!isClientLevel()) {
			motionX *= 0.98;
			motionY *= 0.98;
			motionZ *= 0.98;
		}

		if (Math.abs(motionX) < 0.005) motionX = 0.0;
		if (Math.abs(motionY) < 0.005) motionY = 0.0;
		if (Math.abs(motionZ) < 0.005) motionZ = 0.0;

		levelObj.theProfiler.startSection("ai");

		if (isMovementBlocked()) {
			moveForward = 0.0f;
			moveStrafe = 0.0f;
			isJumping = false;
		} else if (isClientLevel()) {
			if (isAIEnabled()) {
				levelObj.theProfiler.startSection("newAi");
				updateAITasks();
				levelObj.theProfiler.endSection();
			} else {
				levelObj.theProfiler.startSection("oldAi");
				updateEntityActionState();
				levelObj.theProfiler.endSection();
			}
		}

		levelObj.theProfiler.endStartSection("jump");

		if (isJumping) {
			if (onGround && jumpTicks == 0) {
				jump();
				jumpTicks = 10;
			}
		} else jumpTicks = 0;

		levelObj.theProfiler.endStartSection("travel");
		moveForward *= 0.98f;
		moveStrafe *= 0.98f;
		float v0 = landMovementFactor;
		landMovementFactor *= getSpeedModifier();
		moveEntityWithHeading(moveStrafe, moveForward);
		landMovementFactor = v0;
		levelObj.theProfiler.endStartSection("push");
		collideWithNearbyEntities();
		levelObj.theProfiler.endSection();
	}

	protected void onDeathUpdate() {
		++deathTime;

		if (deathTime == 20) {
			int i;

			setDead();

			for (i = 0; i < 20; ++i) {
			}
		}
	}

	protected void updateEntityActionState() {
		++entityAge;
		moveForward = 0.0f;
		moveStrafe = 0.0f;
	}

	public void moveEntityWithHeading(float strafe, float forward) {
		float v0 = 0.91f;

		if (onGround) {
			v0 = 0.54600006f;
			// blockId
			int id = levelObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));

			if (id > 0) v0 = Block.blocksList[id].slipperiness * 0.91f;
		}

		float v1 = 0.16277136f / (v0 * v0 * v0);
		float v2;

		if (onGround) {
			if (isAIEnabled()) v2 = getAIMoveSpeed();
			else v2 = landMovementFactor;

			v2 *= v1;
		} else v2 = jumpMovementFactor;

		moveFlying(strafe, forward, v2);
		v0 = 0.91f;

		if (onGround) {
			v0 = 0.54600006f;
			// blockId
			int id = levelObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));

			if (id > 0) v0 = Block.blocksList[id].slipperiness * 0.91f;
		}

		moveEntity(motionX, motionY, motionZ);

		if (levelObj.isClient && (!levelObj.blockExists((int) posX, 0, (int) posZ) || !levelObj.getChunkFromBlockCoords((int) posX, (int) posZ).isChunkLoaded)) {
			if (posY > 0.0) motionY = -0.1;
			else motionY = 0.0;
		} else motionY -= 0.08;

		motionX *= (double) v0;
		motionY *= 0.9800000190734863;
		motionZ *= (double) v0;

		prevLimbSwingAmount = limbSwingAmount;
		double dx = posX - prevPosX;
		double dz = posZ - prevPosZ;
		float d = MathHelper.sqrt_double(dx * dx + dz * dz) * 4.0f;

		if (d > 1.0f) d = 1.0f;

		limbSwingAmount += (d - limbSwingAmount) * 0.4f;
		limbSwing += limbSwingAmount;
	}

	public abstract float getMaxHealth();

	public float getHealth() {
		return health;
	}

	public void setHealth(float n) {
		health = n;

		if (n > getMaxHealth()) n = getMaxHealth();
	}

	protected void jump() {
		motionY = 0.41999998688697815;

		if (isSprinting()) {
			float n = rotationYaw * 0.017453292f;
			motionX -= (double) (MathHelper.sin(n) * 0.2f);
			motionZ += (double) (MathHelper.cos(n) * 0.2f);
		}
	}

	public boolean isSprinting() {
		return isSprinting;
	}

	public void setSprinting(boolean flag) {
		isSprinting = flag;
	}

	public float getSpeedModifier() {
		float n = 1.0f;

		if (n < 0.0f) n = 0.0f;

		return n;
	}

	protected boolean isMovementBlocked() {
		return getHealth() <= 0.0f;
	}

	public float getEyeHeight() {
		return height * 0.85f;
	}

	protected void collideWithNearbyEntities() {
		List<Entity> entities = levelObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.20000000298023224, 0.0, 0.20000000298023224));

		if (entities != null && !entities.isEmpty()) {
			for (int i = 0; i < entities.size(); ++i) {
				Entity e = entities.get(i);

				if (e.canBePushed()) collideWithEntity(e);
			}
		}
	}

	protected void collideWithEntity(Entity entity) {
		entity.applyEntityCollision(this);
	}

	protected boolean isAIEnabled() {
		return false;
	}

	public float getAIMoveSpeed() {
		return aiMoveSpeed;
	}

	public void setAIMoveSpeed(float n) {
		aiMoveSpeed = n;
		setMoveForward(n);
	}

	public void setMoveForward(float n) {
		moveForward = n;
	}

	protected void updateAITasks() {
		++entityAge;
		levelObj.theProfiler.startSection("mob tick");
		updateAITick();
		levelObj.theProfiler.endSection();
	}

	protected void updateAITick() {
	}

	public int getTalkInterval() {
		return aiTalkInterval;
	}

	public void setTalkInterval(int n) {
		if (n != aiTalkInterval) {
			int var0 = aiTalkInterval;
			aiTalkInterval = n;
			livingSoundTime -= (n - var0);
		}
	}

	public void playLivingSound() {
		String livingSound = getLivingSound();

		if (livingSound != null) playSound(livingSound, getSoundVolume(), getSoundPitch());
	}

	protected String getLivingSound() {
		return null;
	}

	protected float getSoundVolume() {
		return 1.0f;
	}

	protected float getSoundPitch() {
		return (rand.nextFloat() - rand.nextFloat()) * 0.2f + 1.0f;
	}

	protected String getHurtSound() {
		return "damage.hit";
	}

	protected String getDeathSound() {
		return "damage.hit";
	}

	public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		if (isEntityInvulnerable()) return false;
		else if (levelObj.isClient) return false;
		else {
			entityAge = 0;

			if (getHealth() <= 0.0f) return false;
			else {
				limbSwingAmount = 1.5f;
				boolean hurt = true;

				if ((float) hurtResistantTime > (float) maxHurtResistantTime / 2.0f) {
					if (damage <= lastDamage) return false;

					damageEntity(damageSource, damage - lastDamage);
					lastDamage = damage;
					hurt = false;
				} else {
					lastDamage = damage;
					prevHealth = getHealth();
					hurtResistantTime = maxHurtResistantTime;
					damageEntity(damageSource, damage);
					hurtTime = maxHurtTime = 10;
				}

				attackedAtYaw = 0.0f;
				Entity source = damageSource.getEntity();

				if (source != null) {
					if (source instanceof EntityLiving) setRevengeTarget((EntityLiving) source);

					if (source instanceof EntityPlayer) {
						recentlyHit = 100;
						attackingPlayer = (EntityPlayer) source;
					}
				}

				if (hurt) {
					levelObj.setEntityState(this, (byte) 2);

					setBeenAttacked();

					if (source != null) {
						double dx = source.posX - posX;
						double dz;

						for (dz = source.posZ - posZ; dx * dx + dz * dz < 1.0e-4; dz = (Math.random() - Math.random()) * 0.01)
							dx = (Math.random() - Math.random()) * 0.01;

						attackedAtYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - rotationYaw;
						knockBack(source, damage, dx, dz);
					} else attackedAtYaw = (float) ((int) (Math.random() * 2.0) * 180);
				}

				String sound;

				if (getHealth() <= 0.0f) {
					sound = getDeathSound();
					if (hurt && sound != null) playSound(sound, getSoundVolume(), getSoundPitch());

					onDeath(damageSource);
				} else {
					sound = getHurtSound();
					if (hurt && sound != null) playSound(sound, getSoundVolume(), getSoundPitch());
				}

				return true;
			}
		}
	}

	public boolean canBeCollidedWith() {
		return !isDead;
	}

	// TODO onDeath() not done!
	public void onDeath(DamageSource damageSource) {
		Entity entity = damageSource.getEntity();
		EntityLiving var0 = null;

		if (scoreValue >= 0 && var0 != null) var0.addToPlayerScore(this, scoreValue);

		if (entity != null) entity.onKillEntity(this);

		if (!levelObj.isClient) {
			int n = 0;

			if (levelObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) {
				// do stuff in here

				if (recentlyHit > 0) {
					// do stuff in here
				}
			}
		}

		levelObj.setEntityState(this, (byte) 3);
	}

	public Vec3 getPosition(float partialTickTime) {
		if (partialTickTime == 1.0f) return levelObj.getLevelVec3Pool().getVecFromPool(posX, posY, posZ);

		double x = prevPosX + (posX - prevPosX) * (double) partialTickTime;
		double y = prevPosY + (posY - prevPosY) * (double) partialTickTime;
		double z = prevPosZ + (posZ - prevPosZ) * (double) partialTickTime;
		return levelObj.getLevelVec3Pool().getVecFromPool(x, y, z);
	}

	public Vec3 getLook(float partialTickTime) {
		float v0;
		float v1;
		float v2;
		float v3;

		if (partialTickTime == 1.0f) {
			v0 = MathHelper.cos(-rotationYaw * 0.017453292f - (float) Math.PI);
			v1 = MathHelper.sin(-rotationYaw * 0.017453292f - (float) Math.PI);
			v2 = -MathHelper.cos(-rotationPitch * 0.017453292f);
			v3 = MathHelper.sin(-rotationPitch * 0.017453292f);
			return levelObj.getLevelVec3Pool().getVecFromPool((double) (v1 * v2), (double) v3, (double) (v0 * v2));
		} else {
			v0 = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTickTime;
			v1 = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTickTime;
			v2 = MathHelper.cos(-v1 * 0.017453292f - (float) Math.PI);
			v3 = MathHelper.sin(-v1 * 0.017453292f - (float) Math.PI);
			float v4 = -MathHelper.cos(-v0 * 0.017453292f);
			float v5 = MathHelper.sin(-v0 * 0.017453292f);
			return levelObj.getLevelVec3Pool().getVecFromPool((double) (v3 * v4), (double) v5, (double) (v2 * v4));
		}
	}

	public MovingObjectPosition rayTrace(double reachDistance, float partialTickTime) {
		// position
		Vec3 p = getPosition(partialTickTime);
		Vec3 look = getLook(partialTickTime);
		// vector
		Vec3 v = p.addVector(look.xCoord * reachDistance, look.yCoord * reachDistance, look.zCoord * reachDistance);
		return levelObj.rayTraceBlocks(p, v);
	}

	public void swingItem() {
	}

	protected void damageEntity(DamageSource damageSource, float damage) {
		if (!isEntityInvulnerable()) {
			if (damage != 0.0f) {
				float h = getHealth();
				setHealth(h - damage);
			}
		}
	}

	public void setRevengeTarget(EntityLiving living) {
		entityLivingToAttack = living;
		revengeTimer = entityLivingToAttack != null ? 100 : 0;
	}

	public void knockBack(Entity entity, float damage, double x, double z) {
		isAirBorne = true;
		float d = MathHelper.sqrt_double(x * x + z * z);
		float n = 0.4f;
		motionX /= 2.0;
		motionY /= 2.0;
		motionZ /= 2.0;
		motionX -= x / (double) d * (double) n;
		motionY += (double) n;
		motionZ -= z / (double) d * (double) n;

		if (motionY > 0.4000000059604645) motionY = 0.4000000059604645;
	}

	public void setLastAttackingEntity(Entity entity) {
		if (entity instanceof EntityLiving) lastAttacker = (EntityLiving) entity;
	}

	protected boolean isClientLevel() {
		return !levelObj.isClient;
	}

	public void handleHealthUpdate(byte status) {
		if (status == 2) {
			limbSwingAmount = 1.5f;
			hurtResistantTime = maxHurtResistantTime;
			hurtTime = maxHurtTime = 10;
			attackedAtYaw = 0.0f;
			playSound(getHurtSound(), getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2f + 1.0f);
			attackEntityFrom(DamageSource.generic, 0.0f);
		} else if (status == 3) {
			playSound(getDeathSound(), getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2f + 1.0f);
			setHealth(0.0f);
			onDeath(DamageSource.generic);
		} else super.handleHealthUpdate(status);
	}
}
