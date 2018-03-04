package renor.level.entity;

import renor.level.Level;
import renor.level.block.Block;
import renor.level.item.ItemStack;
import renor.misc.DamageSource;
import renor.util.MathHelper;

public class EntityItem extends Entity {
	public int age;
	public int delayBeforeCanPickup;
	private int health;

	public EntityItem(Level level) {
		super(level);

		health = 5;
		yOffset = height / 2.0f;
		setSize(0.25f, 0.25f);
	}

	public EntityItem(Level level, double x, double y, double z) {
		super(level);

		age = 0;
		health = 5;
		motionX = (double) ((float) (Math.random() * 0.20000000298023224 - 0.10000000149011612));
		motionY = 0.20000000298023224;
		motionZ = (double) ((float) (Math.random() * 0.20000000298023224 - 0.10000000149011612));
		rotationYaw = (float) (Math.random() * 360.0);
		yOffset = height / 2.0f;
		setSize(0.25f, 0.25f);
		setPosition(x, y, z);
	}

	public void onUpdate() {
		super.onUpdate();

		if (delayBeforeCanPickup > 0) --delayBeforeCanPickup;

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		motionY -= 0.03999999910593033;
		noClip = pushOutOfBlocks(posX, (boundingBox.minY + boundingBox.maxY) / 2.0, posZ);
		moveEntity(motionX, motionY, motionZ);
		boolean v0 = (int) prevPosX != (int) posX || (int) prevPosY != (int) posY || (int) prevPosZ != (int) posZ;

		if (v0 || ticksExisted % 25 == 0) {
			if (!levelObj.isClient) searchForOtherItemsNearby();
		}

		float v1 = 0.98f;

		if (onGround) {
			v1 = 0.58800006f;
			// blockId
			int id = levelObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));

			if (id > 0) v1 = Block.blocksList[id].slipperiness * 0.98f;
		}

		motionX *= (double) v1;
		motionY *= 0.9800000190734863;
		motionZ *= (double) v1;

		if (onGround) motionY *= -0.5f;

		++age;

		if (!levelObj.isClient && age >= 6000) setDead();
	}

	protected boolean canTriggerWalking() {
		return false;
	}

	public boolean attackEntityFrom(DamageSource damageSource, float damage) {
		if (isEntityInvulnerable()) {
			return false;
		} else {
			setBeenAttacked();
			health = (int) ((float) health - damage);

			if (health <= 0) setDead();

			return false;
		}
	}

	private void searchForOtherItemsNearby() {
		// Iterator var1 = levelObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(0.5, 0.0, 0.5)).iterator();

		// while (var1.hasNext()) {
		// EntityItem item = (EntityItem) var1.next();
		// combineItems(item);
		// }
	}

	public boolean combineItems(EntityItem entityItem) {
		if (entityItem == this) return false;
		else if (entityItem.isEntityAlive() && isEntityAlive()) {
			// do other stuff here
			return true;
		} else return false;
	}

	public ItemStack getEntityItem() {
		return null;
	}
}
