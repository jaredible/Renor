package renor.level.item;

import renor.level.Level;
import renor.level.block.Block;
import renor.level.entity.EntityPlayer;

public class ItemBlock extends Item {
	private int blockId;

	public ItemBlock(int id) {
		super(id);

		blockId = id;
	}

	public int getBlockId() {
		return blockId;
	}

	// TODO check this!
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, Level level, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (side == 0) --y;

		if (side == 1) ++y;

		if (side == 2) --z;

		if (side == 3) ++z;

		if (side == 4) --x;

		if (side == 5) ++x;

		if (itemStack.stackSize == 0) return false;
		else if (!player.canPlayerEdit(x, y, z, side, itemStack)) return false;
		else if (y == 255 && Block.blocksList[blockId].blockMaterial.isSolid()) return false;
		else if (level.canPlaceBlockOnSide(blockId, x, y, z, false, side, player, itemStack)) {
			int metadata = Block.blocksList[blockId].onBlockPlaced(level, x, y, z, side, hitX, hitY, hitZ, 0);

			if (level.setBlock(x, y, z, blockId, metadata, 3)) {
				if (level.getBlockId(x, y, z) == blockId) {
					Block.blocksList[blockId].onBlockPlacedBy(level, x, y, z, player, itemStack);
					Block.blocksList[blockId].onPostBlockPlaced(level, x, y, z, metadata);
				}

				--itemStack.stackSize;
			}

			return true;
		} else return false;
	}

	public boolean canPlaceItemBlockOnSide(Level level, int x, int y, int z, int side, EntityPlayer player, ItemStack itemStack) {
		if (side == 0) --y;

		if (side == 1) ++y;

		if (side == 2) --z;

		if (side == 3) ++z;

		if (side == 4) --x;

		if (side == 5) ++x;

		return level.canPlaceBlockOnSide(getBlockId(), x, y, z, false, side, null, itemStack);
	}
}
