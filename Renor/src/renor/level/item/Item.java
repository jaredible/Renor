package renor.level.item;

import renor.level.Level;
import renor.level.entity.Entity;
import renor.level.entity.EntityPlayer;

public class Item {
	private String unlocalizedName;
	public final int itemId;
	protected int maxStackSize = 64;

	public static Item[] itemsList = new Item[256];
	public static Item grass = new ItemBlock(1).setUnlocalizedName("grass");
	public static Item ice = new ItemBlock(2).setUnlocalizedName("ice");
	public static Item glow = new ItemBlock(3).setUnlocalizedName("glow");

	public Item(int id) {
		itemId = id;

		if (itemsList[id] != null) System.err.println("CONFLICT @ " + id);

		itemsList[id] = this;
	}

	public int getItemStackLimit() {
		return maxStackSize;
	}

	public Item setUnlocalizedName(String name) {
		unlocalizedName = name;
		return this;
	}

	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, Level level, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return false;
	}

	public boolean allowUse() {
		return true;
	}

	public int getDamageVsEntity(Entity entity) {
		return 1;
	}

	public void onPlayerStoppedUsing(ItemStack itemStack, Level level, EntityPlayer player, int itemInUseCount) {
	}
}
