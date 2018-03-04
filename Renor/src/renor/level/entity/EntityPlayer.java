package renor.level.entity;

import java.util.List;

import org.lwjgl.input.Keyboard;

import renor.Renor;
import renor.container.Container;
import renor.container.ContainerPlayer;
import renor.container.InventoryPlayer;
import renor.level.Level;
import renor.level.item.ItemStack;
import renor.misc.DamageSource;
import renor.util.MathHelper;
import renor.util.MovementInput;
import renor.util.PlayerCapabilities;
import renor.util.Session;

public class EntityPlayer extends EntityLiving {
	public String username;
	private Renor renor;
	public MovementInput movementInput;
	public PlayerCapabilities capabilities = new PlayerCapabilities();
	public InventoryPlayer inventory = new InventoryPlayer(this);
	public Container inventoryContainer;
	private ItemStack itemInUse;
	public int sprintingTicksLeft;
	private int itemInUseCount;
	private int testToggleTimer = 0;
	public float cameraYaw;
	public float prevCameraYaw;
	private float speedOnGround = 0.1f;
	private float speedInAir = 0.02f;
	public float renderArmYaw;
	public float renderArmPitch;
	public float prevRenderArmYaw;
	public float prevRenderArmPitch;

	public EntityPlayer(Renor renor, Level level, Session session) {
		super(level);
		this.renor = renor;
		username = session.getUsername();

		inventoryContainer = new ContainerPlayer(this);
		yOffset = 1.62f;
		setLocationAndAngles((double) posX + 0.5, (double) (posY + 1), (double) posZ + 0.5, 0.0f, 0.0f);
	}

	public void onUpdate() {
		if (itemInUse != null) {
			// do stuff here
		}

		super.onUpdate();

		if (testToggleTimer > 0) --testToggleTimer;

		if (Keyboard.getEventKeyState() && testToggleTimer == 0) {
			testToggleTimer = 5;

			if (Keyboard.getEventKey() == Keyboard.KEY_R) {
				capabilities.isFlying = false;
				motionX = motionY = motionZ = 0.0;
				moveForward = moveStrafe = 0.0f;
				setLocationAndAngles(0.5, 64, 0.5, 0.0f, 0.0f);
			}

			if (Keyboard.getEventKey() == Keyboard.KEY_F) {
				if (capabilities.isFlying) capabilities.isFlying = false;
				else {
					if (onGround) jump();
					capabilities.isFlying = true;
				}
			}

			if (Keyboard.getEventKey() == Keyboard.KEY_T) {
				// TODO test strafe jump here
			}
		}
	}

	public void onLivingUpdate() {
		if (sprintingTicksLeft > 0) {
			--sprintingTicksLeft;

			if (sprintingTicksLeft == 0) setSprinting(false);
		}

		float v0 = 0.8f;
		movementInput.updatePlayerMoveState();

		if (isUsingItem()) {
			movementInput.moveForward *= 0.2f;
			movementInput.moveStrafe *= 0.2f;
		}

		if (movementInput.sneak && ySize < 0.2f) ySize = 0.2f;

		pushOutOfBlocks(posX - (double) width * 0.35, boundingBox.minY + 0.5, posZ + (double) width * 0.35);
		pushOutOfBlocks(posX - (double) width * 0.35, boundingBox.minY + 0.5, posZ - (double) width * 0.35);
		pushOutOfBlocks(posX + (double) width * 0.35, boundingBox.minY + 0.5, posZ - (double) width * 0.35);
		pushOutOfBlocks(posX + (double) width * 0.35, boundingBox.minY + 0.5, posZ + (double) width * 0.35);

		if (!isSprinting() && movementInput.moveForward >= v0 && !isUsingItem() && renor.gameSettings.keyBindSprint.getIsKeyPressed()) setSprinting(true);

		if (isSprinting() && (movementInput.moveForward < v0 || isCollidedHorizontally)) setSprinting(false);

		if (capabilities.isFlying) {
			if (movementInput.sneak) motionY -= 0.15;

			if (movementInput.jump) motionY += 0.15;
		}

		prevCameraYaw = cameraYaw;
		super.onLivingUpdate();
		landMovementFactor = capabilities.getWalkSpeed();
		jumpMovementFactor = speedInAir;

		if (isSprinting()) {
			landMovementFactor = (float) ((double) landMovementFactor + (double) capabilities.getWalkSpeed() * 0.3);
			jumpMovementFactor = (float) ((double) jumpMovementFactor + (double) speedInAir * 0.3);
		}

		float v1 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
		float v2 = (float) Math.atan(-motionY * 0.20000000298023224) * 15.0f;

		if (v1 > 0.1f) v1 = 0.1f;

		if (!onGround && getHealth() <= 0.0f) v1 = 0.0f;

		if (onGround && getHealth() <= 0.0f) v2 = 0.0f;

		cameraYaw += (v1 - cameraYaw) * 0.4f;
		cameraPitch += (v2 - cameraPitch) * 0.8f;

		if (getHealth() > 0.0f) {
			List<Entity> entities = levelObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(1.0, 0.5, 1.0));

			if (entities != null) {
				for (int i = 0; i < entities.size(); ++i) {
					Entity e = entities.get(i);

					if (!e.isDead) collideWithPlayer(e);
				}
			}
		}

		if (onGround && capabilities.isFlying) capabilities.isFlying = false;
	}

	private void collideWithPlayer(Entity entity) {
		entity.onCollideWithPlayer(this);
	}

	protected boolean pushOutOfBlocks(double x, double y, double z) {
		int xInt = MathHelper.floor_double(x);
		int yInt = MathHelper.floor_double(y);
		int zInt = MathHelper.floor_double(z);
		double xx = x - (double) xInt;
		double zz = z - (double) zInt;

		if (isBlockTranslucent(xInt, yInt, zInt) || isBlockTranslucent(xInt, yInt + 1, zInt)) {
			boolean v0 = !isBlockTranslucent(xInt - 1, yInt, zInt) && !isBlockTranslucent(xInt - 1, yInt + 1, zInt);
			boolean v1 = !isBlockTranslucent(xInt + 1, yInt, zInt) && !isBlockTranslucent(xInt + 1, yInt + 1, zInt);
			boolean v2 = !isBlockTranslucent(xInt, yInt, zInt - 1) && !isBlockTranslucent(xInt, yInt + 1, zInt - 1);
			boolean v3 = !isBlockTranslucent(xInt, yInt, zInt + 1) && !isBlockTranslucent(xInt, yInt + 1, zInt + 1);
			byte side = -1;
			double v4 = 9999.0;

			if (v0 && xx < v4) {
				v4 = xx;
				side = 0;
			}

			if (v1 && 1.0 - xx < v4) {
				v4 = 1.0 - xx;
				side = 1;
			}

			if (v2 && zz < v4) {
				v4 = zz;
				side = 4;
			}

			if (v3 && 1.0 - zz < v4) {
				v4 = 1.0 - zz;
				side = 5;
			}

			float n = 0.1f;

			if (side == 0) motionX = (double) (-n);

			if (side == 1) motionX = (double) n;

			if (side == 4) motionZ = (double) (-n);

			if (side == 5) motionZ = (double) n;
		}

		return false;
	}

	private boolean isBlockTranslucent(int x, int y, int z) {
		return levelObj.isBlockNormalCube(x, y, z);
	}

	protected void updateEntityActionState() {
		super.updateEntityActionState();
		moveForward = movementInput.moveForward;
		moveStrafe = movementInput.moveStrafe;
		isJumping = movementInput.jump;
		prevRenderArmYaw = renderArmYaw;
		prevRenderArmPitch = renderArmPitch;
		renderArmPitch = (float) ((double) renderArmPitch + (double) (rotationPitch - renderArmPitch) * 0.5);
		renderArmYaw = (float) ((double) renderArmYaw + (double) (rotationYaw - renderArmYaw) * 0.5);
	}

	public void moveEntityWithHeading(float strafe, float forward) {
		double oldX = posX;
		double oldY = posY;
		double oldZ = posZ;

		if (capabilities.isFlying) {
			double v0 = motionY;
			float v1 = jumpMovementFactor;
			jumpMovementFactor = capabilities.getFlySpeed();
			super.moveEntityWithHeading(strafe, forward);
			motionY = v0 * 0.6;
			jumpMovementFactor = v1;
		} else super.moveEntityWithHeading(strafe, forward);

		addMovementStat(posX - oldX, posY - oldY, posZ - oldZ);
	}

	public float getMaxHealth() {
		return 20;
	}

	public void setSprinting(boolean flag) {
		super.setSprinting(flag);
		sprintingTicksLeft = flag ? 600 : 0;
	}

	public float getEyeHeight() {
		return 0.12f;
	}

	protected boolean canTriggerWalking() {
		return !capabilities.isFlying;
	}

	public void playSound(String name, float volume, float pitch) {
		levelObj.playSound(posX, posY - yOffset, posZ, name, volume, pitch);
	}

	public void preparePlayerToSpawn() {
		yOffset = 1.62f;
		setSize(0.6f, 1.8f);

		if (levelObj != null) {
			while (posY > 0.0) {
				setPosition(posX, posY, posZ);

				if (levelObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty()) break;

				++posY;
			}

			motionX = motionY = motionZ = 0.0;
			rotationPitch = 0.0f;
		}

		setHealth(getHealth());
		deathTime = 0;
	}

	public float getFOVMultiplier() {
		float n = 1.0f;

		if (capabilities.isFlying) n *= 1.1f;

		n *= (landMovementFactor * getSpeedModifier() / speedOnGround + 1.0f) / 2.0f;

		if (capabilities.getWalkSpeed() == 0.0f || Float.isNaN(n) || Float.isInfinite(n)) n = 1.0f;

		return n;
	}

	public void addMovementStat(double x, double y, double z) {
		if (onGround) {
			int d = Math.round(MathHelper.sqrt_double(x * x + z * z) * 100.0f);

			if (d > 0) {
			}
		} else {
			int d = Math.round(MathHelper.sqrt_double(x * x + z * z) * 100.0f);

			if (d > 25) {
			}
		}
	}

	public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		if (isEntityInvulnerable()) return false;
		else if (capabilities.disableDamage) return false;
		else {
			entityAge = 0;

			if (getHealth() <= 0.0f) return false;
			else {
				if (damage == 0.0f) return false;
				else {
					// do other stuff here

					return super.attackEntityFrom(damageSource, damage);
				}
			}
		}
	}

	public void onDeath(DamageSource damageSource) {
		super.onDeath(damageSource);
		setSize(0.2f, 0.2f);
		setPosition(posX, posY, posZ);
		motionY = 0.10000000149011612;

		// TODO Syn's exclusive death message
		if (username.equals("Syn")) {
			System.out.println("Exclusive message for Syn's death.");
			Thread.dumpStack();
		}

		if (!levelObj.getGameRules().getGameRuleBooleanValue("keepInventory")) inventory.dropAllItems();

		if (damageSource != null) {
			motionX = (double) (-MathHelper.cos((attackedAtYaw + rotationYaw) * (float) Math.PI / 180.0f) * 0.1f);
			motionZ = (double) (-MathHelper.sin((attackedAtYaw + rotationYaw) * (float) Math.PI / 180.0f) * 0.1f);
		} else motionX = motionZ = 0.0;

		yOffset = 0.1f;
		// TODO player death stat
		// add death stat here
	}

	protected void damageEntity(DamageSource damageSource, float damage) {
		if (!isEntityInvulnerable()) {
			if (!damageSource.isUnblockable() && isBlocking() && damage > 0.0f) damage = (1.0f + damage) * 0.5f;

			if (damage != 0.0f) {
				float h = getHealth();
				setHealth(h - damage);
			}
		}
	}

	public void onKillEntity(EntityLiving living) {
	}

	public boolean isSneaking() {
		return movementInput.sneak;
	}

	public boolean canPlayerEdit(int x, int y, int z, int side, ItemStack itemStack) {
		return capabilities.allowEdit ? true : (itemStack != null ? itemStack.allowUse() : false);
	}

	// TODO rename to canCurrentToolHarvestBlock()?
	public boolean canDamageBlock(int x, int y, int z) {
		if (capabilities.allowEdit) return true;
		else {
			// do other stuff here

			return false;
		}
	}

	public void attackTargetEntityWithCurrentItem(Entity entity) {
		if (entity.canAttackWithItem()) {
			float damage = inventory.getDamageVsEntity(entity);
			int velocity = 0;

			if (isSprinting()) ++velocity;

			if (damage > 0.0f) {
				boolean attacked = entity.attackEntityFrom(DamageSource.causePlayerDamage(this), damage);

				if (attacked) {
					if (velocity > 0) {
						entity.addVelocity((double) (-MathHelper.sin(rotationYaw * (float) Math.PI / 180.0f) * (float) velocity * 0.5f), 0.1, (double) (MathHelper.cos(rotationYaw * (float) Math.PI / 180.0f) * (float) velocity * 0.5f));
						motionX *= 0.6;
						motionZ *= 0.6;
						setSprinting(false);
					}

					setLastAttackingEntity(entity);
				}
			}
		}
	}

	protected boolean isClientLevel() {
		return true;
	}

	public void handleHealthUpdate(byte status) {
		if (status == 9) onItemUseFinish();
		else super.handleHealthUpdate(status);
	}

	protected void updateItemUse(ItemStack itemStack, int n) {
	}

	protected void onItemUseFinish() {
		if (itemInUse != null) {
		}
	}

	public boolean isUsingItem() {
		return itemInUse != null;
	}

	public void stopUsingItem() {
		if (itemInUse != null) itemInUse.onPlayerStoppedUsing(levelObj, this, itemInUseCount);

		clearItemInUse();
	}

	public void clearItemInUse() {
		itemInUse = null;
		itemInUseCount = 0;
	}

	public boolean isBlocking() {
		return false;
	}
}
