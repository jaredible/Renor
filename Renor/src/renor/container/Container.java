package renor.container;

import java.util.ArrayList;
import java.util.List;

import renor.level.item.ItemStack;

public abstract class Container {
	public List<ItemStack> inventoryItemStacks = new ArrayList<ItemStack>();
	public List<Slot> inventorySlots = new ArrayList<Slot>();

	protected Slot addSlotToContainer(Slot slot) {
		slot.slotNumber = inventorySlots.size();
		inventorySlots.add(slot);
		inventoryItemStacks.add(null);
		return slot;
	}
}
