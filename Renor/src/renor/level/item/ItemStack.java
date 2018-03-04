package renor.level.item;

import renor.level.Level;
import renor.level.entity.Entity;
import renor.level.entity.EntityPlayer;

public final class ItemStack {
	public int itemId;
	public int stackSize;

	public ItemStack(int itemId, int stackSize) {
		this.itemId = itemId;
		this.stackSize = stackSize;
	}

	public ItemStack(Item item) {
		this(item.itemId, 1);
	}

	public ItemStack splitStack(int stackSize) {
		ItemStack itemStack = new ItemStack(itemId, stackSize);

		return itemStack;
	}

	public Item getItem() {
		return Item.itemsList[itemId];
	}

	public int getMaxStackSize() {
		return getItem().getItemStackLimit();
	}

	public boolean isStackable() {
		return getMaxStackSize() > 1;
	}

	public String getDisplayName() {
		return "";
	}

	public boolean tryPlaceItemIntoLevel(EntityPlayer player, Level level, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		boolean use = getItem().onItemUse(this, player, level, x, y, z, side, hitX, hitY, hitZ);
		return use;
	}

	public boolean allowUse() {
		return getItem().allowUse();
	}

	public int getDamageVsEntity(Entity entity) {
		return Item.itemsList[itemId].getDamageVsEntity(entity);
	}

	public void onPlayerStoppedUsing(Level level, EntityPlayer player, int itemInUseCount) {
		getItem().onPlayerStoppedUsing(this, level, player, itemInUseCount);
	}
}
