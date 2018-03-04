package renor.container;

import renor.level.entity.Entity;
import renor.level.entity.EntityPlayer;
import renor.level.item.Item;
import renor.level.item.ItemStack;

public class InventoryPlayer {
	public EntityPlayer player;
	public ItemStack[] mainInventory = new ItemStack[3];
	public int currentItem = 0;

	public InventoryPlayer(EntityPlayer player) {
		this.player = player;

		mainInventory[0] = new ItemStack(Item.grass);
		mainInventory[1] = new ItemStack(Item.ice);
		mainInventory[2] = new ItemStack(Item.glow);
	}

	public ItemStack getCurrentItem() {
		return currentItem < 3 && currentItem >= 0 ? mainInventory[currentItem] : null;
	}

	public void changeCurrentItem(int scroll) {
		if (scroll > 0) scroll = 1;

		if (scroll < 0) scroll = -1;

		for (currentItem -= scroll; currentItem < 0; currentItem += 3) {
			;
		}

		while (currentItem >= 3)
			currentItem -= 3;
	}

	public int getDamageVsEntity(Entity entity) {
		ItemStack stack = getStackInSlot(currentItem);
		return stack != null ? stack.getDamageVsEntity(entity) : 1;
	}

	public ItemStack getStackInSlot(int i) {
		ItemStack[] slots = mainInventory;

		if (i >= slots.length) i -= slots.length;

		return slots[i];
	}

	public void dropAllItems() {
	}

	public void setCurrentItem(int id) {
		int slot;

		slot = getInventorySlotContainItem(id);

		if (slot >= 0 && slot < 9) currentItem = slot;
		else {
			if (id > 0) {
				// slot
				int s = getFirstEmptyStack();

				if (s >= 0 && s < 9) currentItem = s;

				a(Item.itemsList[s]);
			}
		}
	}

	private int getInventorySlotContainItem(int id) {
		for (int i = 0; i < mainInventory.length; ++i)
			if (mainInventory[i] != null && mainInventory[i].itemId == id) return i;

		return -1;
	}

	public int getFirstEmptyStack() {
		for (int i = 0; i < mainInventory.length; ++i)
			if (mainInventory[i] == null) return i;

		return -1;
	}

	public void a(Item item) {
		if (item != null) {
		}
	}
}
