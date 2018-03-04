package renor.level.block;

import java.util.List;

import renor.aabb.AxisAlignedBB;
import renor.level.Level;
import renor.level.entity.Entity;
import renor.level.entity.EntityLiving;
import renor.level.item.ItemStack;
import renor.level.material.Material;
import renor.misc.MovingObjectPosition;
import renor.util.texture.Icon;
import renor.util.texture.IconRegister;
import renor.vector.Vec3;

public class Block {
	private String unlocalizedName;
	public final Material blockMaterial;
	protected Icon blockIcon;
	public final int blockId;
	protected double minX;
	protected double minY;
	protected double minZ;
	protected double maxX;
	protected double maxY;
	protected double maxZ;
	public float slipperiness;

	public static final Block[] blocksList = new Block[4096];
	public static final int[] lightOpacity = new int[4096];
	public static final int[] lightValue = new int[4096];
	public static final boolean[] useNeighborBrightness = new boolean[4096];
	public static final Block grass = new BlockGrass(1, Material.grass).setUnlocalizedName("grass");
	public static final Block ice = new BlockIce(2, Material.ice).setUnlocalizedName("ice");
	public static final Block glow = new Block(3, Material.grass).setLightValue(1.0f).setUnlocalizedName("glow");

	protected Block(int id, Material material) {
		if (blocksList[id] != null) throw new IllegalArgumentException("Slot " + id + " is already occupied by " + blocksList[id] + " when adding " + this);
		else {
			blockId = id;
			blockMaterial = material;
			blocksList[id] = this;
			lightOpacity[id] = isOpaque() ? 255 : 0;
			slipperiness = 0.6f;
			setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	protected void initializeBlock() {
	}

	protected final void setBlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.minX = (double) minX;
		this.minY = (double) minY;
		this.minZ = (double) minZ;
		this.maxX = (double) maxX;
		this.maxY = (double) maxY;
		this.maxZ = (double) maxZ;
	}

	public AxisAlignedBB getCollisionBoundingBoxFromPool(Level level, int x, int y, int z) {
		return AxisAlignedBB.getAABBPool().getAABB((double) x + minX, (double) y + minY, (double) z + minZ, (double) x + maxX, (double) y + maxY, (double) z + maxZ);
	}

	public AxisAlignedBB getSelectedBoundingBoxFromPool(Level level, int x, int y, int z) {
		return AxisAlignedBB.getAABBPool().getAABB((double) x + minX, (double) y + minY, (double) z + minZ, (double) x + maxX, (double) y + maxY, (double) z + maxZ);
	}

	public void addCollisionBoxesToList(Level level, int x, int y, int z, AxisAlignedBB axisAlignedBB, List<AxisAlignedBB> boxes) {
		AxisAlignedBB aabb = getCollisionBoundingBoxFromPool(level, x, y, z);

		if (aabb != null && axisAlignedBB.intersectsWith(aabb)) boxes.add(aabb);
	}

	public void registerIcons(IconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon(unlocalizedName);
	}

	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return getIcon(side, blockAccess.getBlockMetadata(x, y, z));
	}

	public Icon getIcon(int side, int metadata) {
		return blockIcon;
	}

	public Block setUnlocalizedName(String name) {
		unlocalizedName = name;
		return this;
	}

	public String getUnlocalizedName() {
		return "block." + unlocalizedName;
	}

	public final double getBlockBoundsMinX() {
		return minX;
	}

	public final double getBlockBoundsMaxX() {
		return maxX;
	}

	public final double getBlockBoundsMinY() {
		return minY;
	}

	public final double getBlockBoundsMaxY() {
		return maxY;
	}

	public final double getBlockBoundsMinZ() {
		return minZ;
	}

	public final double getBlockBoundsMaxZ() {
		return maxZ;
	}

	public int getMixedBrightnessForBlock(IBlockAccess blockAccess, int x, int y, int z) {
		return blockAccess.getLightBrightnessForSkyBlocks(x, y, z, lightOpacity[blockAccess.getBlockId(x, y, z)]);
	}

	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return side == 0 && minY > 0.0 ? true : (side == 1 && maxY < 1.0 ? true : (side == 2 && minZ > 0.0 ? true : (side == 3 && maxZ < 1.0 ? true : (side == 4 && minX > 0.0 ? true : (side == 5 && maxX < 1.0 ? true : !blockAccess.isBlockOpaque(x, y, z))))));
	}

	public boolean isOpaque() {
		return true;
	}

	public int getRenderBlockPass() {
		return 0;
	}

	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
	}

	public MovingObjectPosition collisionRayTrace(Level level, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
		setBlockBoundsBasedOnState(level, x, y, z);
		startVec = startVec.addVector((double) (-x), (double) (-y), (double) (-z));
		endVec = endVec.addVector((double) (-x), (double) (-y), (double) (-z));
		Vec3 v0 = startVec.getIntermediateWithXValue(endVec, minX);
		Vec3 v1 = startVec.getIntermediateWithXValue(endVec, maxX);
		Vec3 v2 = startVec.getIntermediateWithYValue(endVec, minY);
		Vec3 v3 = startVec.getIntermediateWithYValue(endVec, maxY);
		Vec3 v4 = startVec.getIntermediateWithZValue(endVec, minZ);
		Vec3 v5 = startVec.getIntermediateWithZValue(endVec, maxZ);

		if (!isVecInsideYZBounds(v0)) v0 = null;

		if (!isVecInsideYZBounds(v1)) v1 = null;

		if (!isVecInsideXZBounds(v2)) v2 = null;

		if (!isVecInsideXZBounds(v3)) v3 = null;

		if (!isVecInsideXYBounds(v4)) v4 = null;

		if (!isVecInsideXYBounds(v5)) v5 = null;

		Vec3 hitVec = null;

		if (v0 != null && (hitVec == null || startVec.squareDistanceTo(v0) < startVec.squareDistanceTo(hitVec))) hitVec = v0;

		if (v1 != null && (hitVec == null || startVec.squareDistanceTo(v1) < startVec.squareDistanceTo(hitVec))) hitVec = v1;

		if (v2 != null && (hitVec == null || startVec.squareDistanceTo(v2) < startVec.squareDistanceTo(hitVec))) hitVec = v2;

		if (v3 != null && (hitVec == null || startVec.squareDistanceTo(v3) < startVec.squareDistanceTo(hitVec))) hitVec = v3;

		if (v4 != null && (hitVec == null || startVec.squareDistanceTo(v4) < startVec.squareDistanceTo(hitVec))) hitVec = v4;

		if (v5 != null && (hitVec == null || startVec.squareDistanceTo(v5) < startVec.squareDistanceTo(hitVec))) hitVec = v5;

		if (hitVec == null) return null;
		else {
			byte side = -1;

			if (hitVec == v0) side = 4;

			if (hitVec == v1) side = 5;

			if (hitVec == v2) side = 0;

			if (hitVec == v3) side = 1;

			if (hitVec == v4) side = 2;

			if (hitVec == v5) side = 3;

			return new MovingObjectPosition(x, y, z, side, hitVec.addVector((double) x, (double) y, (double) z));
		}
	}

	private boolean isVecInsideYZBounds(Vec3 vec3) {
		return vec3 == null ? false : vec3.yCoord >= minY && vec3.yCoord <= maxY && vec3.zCoord >= minZ && vec3.zCoord <= maxZ;
	}

	private boolean isVecInsideXZBounds(Vec3 vec3) {
		return vec3 == null ? false : vec3.xCoord >= minX && vec3.xCoord <= maxX && vec3.zCoord >= minZ && vec3.zCoord <= maxZ;
	}

	private boolean isVecInsideXYBounds(Vec3 vec3) {
		return vec3 == null ? false : vec3.xCoord >= minX && vec3.xCoord <= maxX && vec3.yCoord >= minY && vec3.yCoord <= maxY;
	}

	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return 16777215;
	}

	public void onEntityCollidedWithBlock(Level level, int x, int y, int z, Entity entity) {
	}

	public void onBlockDestroyedByPlayer(Level level, int x, int y, int z, int metadata) {
	}

	public void breakBlock(Level level, int x, int y, int z, int id, int metadata) {
	}

	public void onSetBlockIdWithMetadata(Level level, int x, int y, int z, int metadata) {
	}

	public void onBlockAdded(Level level, int x, int y, int z) {
	}

	public int onBlockPlaced(Level level, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		return metadata;
	}

	public void onBlockPlacedBy(Level level, int x, int y, int z, EntityLiving living, ItemStack itemStack) {
	}

	public void onPostBlockPlaced(Level level, int x, int y, int z, int metadata) {
	}

	public boolean canPlaceBlockOnSide(Level level, int x, int y, int z, int side, ItemStack itemStack) {
		return canPlaceBlockOnSide(level, x, y, z, side);
	}

	public boolean canPlaceBlockOnSide(Level level, int x, int y, int z, int side) {
		return canPlaceBlockAt(level, x, y, z);
	}

	public boolean canPlaceBlockAt(Level level, int x, int y, int z) {
		// blockId
		int id = level.getBlockId(x, y, z);
		return id == 0 || blocksList[id].blockMaterial.isReplaceable();
	}

	public int idPicked(Level level, int x, int y, int z) {
		return blockId;
	}

	public void onEntityWalking(Level level, int x, int y, int z, Entity entity) {
	}

	protected Block setLightValue(float n) {
		lightValue[blockId] = (int) (15.0f * n);
		return this;
	}

	public static boolean isNormalCube(int id) {
		// block
		Block b = blocksList[id];
		return b == null ? false : b.blockMaterial.isOpaque() && b.renderAsNormalBlock();
	}

	public boolean renderAsNormalBlock() {
		return true;
	}

	public int getRenderType() {
		return 0;
	}

	// TODO check this!
	static {
		for (int i = 0; i < 256; ++i) {
			boolean var0 = false;

			if (lightOpacity[i] == 0) var0 = true;

			useNeighborBrightness[i] = var0;
		}
	}
}
