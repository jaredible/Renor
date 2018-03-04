package renor.util;

import renor.Renor;
import renor.level.Level;
import renor.level.LevelClient;
import renor.level.block.Block;
import renor.level.entity.Entity;
import renor.level.entity.EntityPlayer;
import renor.level.item.ItemBlock;
import renor.level.item.ItemStack;
import renor.network.NetClientHandler;
import renor.vector.Vec3;

public class PlayerControllerMP {
	private final Renor renor;
	private final NetClientHandler netClientHandler;
	private int blockHitDelay = 0;

	public PlayerControllerMP(Renor renor, NetClientHandler netClientHandler) {
		this.renor = renor;
		this.netClientHandler = netClientHandler;
	}

	public void flipPlayer(EntityPlayer player) {
		player.rotationYaw = -180.0f;
	}

	public void updateController() {
		renor.sndManager.playRandomMusicIfReady();
	}

	public EntityPlayer createNewPlayer(Level level) {
		return new EntityPlayer(renor, level, renor.getSession());
	}

	public float getBlockReachDistance() {
		return 5.0f;
	}

	public boolean extendedReach() {
		return true;
	}

	public static void clickBlock(PlayerControllerMP playerControllerMP, int x, int y, int z, int side) {
		playerControllerMP.onPlayerDestroyBlock(x, y, z, side);
	}

	public void clickBlock(int x, int y, int z, int side) {
		clickBlock(this, x, y, z, side);
		blockHitDelay = 5;
	}

	public boolean onPlayerDestroyBlock(int x, int y, int z, int side) {
		// levelClient
		LevelClient lc = renor.theLevel;
		// block
		Block b = Block.blocksList[lc.getBlockId(x, y, z)];

		if (b == null) return false;
		else {
			int metadata = lc.getBlockMetadata(x, y, z);
			boolean setToAir = lc.setBlockToAir(x, y, z);

			if (setToAir) b.onBlockDestroyedByPlayer(lc, x, y, z, metadata);

			return setToAir;
		}
	}

	public void onPlayerDamageBlock(int x, int y, int z, int side) {
		if (blockHitDelay > 0) --blockHitDelay;
		else {
			blockHitDelay = 5;
			clickBlock(this, x, y, z, side);
		}
	}

	// TODO check this!
	public boolean onPlayerRightClick(EntityPlayer player, Level level, ItemStack itemStack, int x, int y, int z, int side, Vec3 hitVec) {
		float hitX = (float) hitVec.xCoord - (float) x;
		float hitY = (float) hitVec.yCoord - (float) y;
		float hitZ = (float) hitVec.zCoord - (float) z;
		boolean var0 = false;

		if (!var0 && itemStack != null && itemStack.getItem() instanceof ItemBlock) {
			ItemBlock block = (ItemBlock) itemStack.getItem();

			// TODO not done
			if (!block.canPlaceItemBlockOnSide(level, x, y, z, side, player, itemStack)) return false;
		}

		if (var0) return true;
		else if (itemStack == null) return false;
		else {
			// oldStackSize
			int size = itemStack.stackSize;
			boolean var1 = itemStack.tryPlaceItemIntoLevel(player, level, x, y, z, side, hitX, hitY, hitZ);
			itemStack.stackSize = size;
			return var1;
		}
	}

	public void attackEntity(EntityPlayer player, Entity entity) {
		player.attackTargetEntityWithCurrentItem(entity);
	}

	public void onStoppedUsingItem(EntityPlayer player) {
		player.stopUsingItem();
	}

	public void sendSlotPacket(ItemStack itemStack, int slot) {
	}
}
