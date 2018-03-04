package renor.level.block;

import renor.level.material.Material;
import renor.vector.Vec3Pool;

public abstract interface IBlockAccess {
	public abstract int getBlockId(int x, int y, int z);

	public abstract int getBlockMetadata(int x, int y, int z);

	public abstract Material getBlockMaterial(int x, int y, int z);

	public abstract boolean isAirBlock(int x, int y, int z);

	public abstract float getBrightness(int x, int y, int z, int opacity);

	public abstract float getLightBrightness(int x, int y, int z);

	public abstract int getLightBrightnessForSkyBlocks(int x, int y, int z, int opacity);

	public abstract boolean isBlockOpaque(int x, int y, int z);

	public abstract int getHeight();

	public abstract Vec3Pool getLevelVec3Pool();
}
