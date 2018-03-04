package renor.level.chunk;

import renor.level.Level;
import renor.level.block.Block;
import renor.level.block.IBlockAccess;
import renor.level.material.Material;
import renor.util.EnumLightValue;
import renor.vector.Vec3Pool;

public class ChunkCache implements IBlockAccess {
	private Level levelObj;
	private Chunk[][] chunkArray;
	private int chunkX;
	private int chunkZ;
	private boolean hasExtendedLevels;

	public ChunkCache(Level level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int radius) {
		levelObj = level;
		chunkX = minX - radius >> 4;
		chunkZ = minZ - radius >> 4;
		int var0 = maxX + radius >> 4;
		int var1 = maxZ + radius >> 4;
		chunkArray = new Chunk[var0 - chunkX + 1][var1 - chunkZ + 1];
		hasExtendedLevels = true;
		int cx;
		int cz;
		// chunk
		Chunk c;

		for (cx = chunkX; cx <= var0; ++cx) {
			for (cz = chunkZ; cz <= var1; ++cz) {
				c = level.getChunkFromChunkCoords(cx, cz);

				if (c != null) chunkArray[cx - chunkX][cz - chunkZ] = c;
			}
		}

		for (cx = minX >> 4; cx <= maxX >> 4; ++cx) {
			for (cz = minZ >> 4; cz <= maxZ >> 4; ++cz) {
				c = chunkArray[cx - chunkX][cz - chunkZ];

				if (c != null && !c.getAreLevelsEmpty(minY, maxY)) hasExtendedLevels = false;
			}
		}
	}

	public int getBlockId(int x, int y, int z) {
		if (y < 0) return 0;
		else if (y >= 256) return 0;
		else {
			int xx = (x >> 4) - chunkX;
			int zz = (z >> 4) - chunkZ;

			if (xx >= 0 && xx < chunkArray.length && zz >= 0 && zz < chunkArray[xx].length) {
				// chunk
				Chunk c = chunkArray[xx][zz];
				return c == null ? 0 : c.getBlockId(x & 15, y, z & 15);
			} else return 0;
		}
	}

	public int getBlockMetadata(int x, int y, int z) {
		if (y < 0) return 0;
		else if (y >= 256) return 0;
		else {
			int xx = (x >> 4) - chunkX;
			int zz = (z >> 4) - chunkZ;
			return chunkArray[xx][zz].getBlockMetadata(x & 15, y, z & 15);
		}
	}

	public Material getBlockMaterial(int x, int y, int z) {
		// blockId
		int id = getBlockId(x, y, z);
		return id == 0 ? Material.air : Block.blocksList[id].blockMaterial;
	}

	public boolean isAirBlock(int x, int y, int z) {
		// block
		Block b = Block.blocksList[getBlockId(x, y, z)];
		return b == null;
	}

	public float getBrightness(int x, int y, int z, int opacity) {
		int n = getLightValue(x, y, z);

		if (n < opacity) n = opacity;

		return levelObj.provider.lightBrightnessTable[n];
	}

	public float getLightBrightness(int x, int y, int z) {
		return levelObj.provider.lightBrightnessTable[getLightValue(x, y, z)];
	}

	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int opacity) {
		int var0 = getSkyBlockTypeBrightness(EnumLightValue.Sky, x, y, z);
		int var1 = getSkyBlockTypeBrightness(EnumLightValue.Block, x, y, z);

		if (var1 < opacity) var1 = opacity;

		return var0 << 20 | var1 << 4;
	}

	public boolean isBlockOpaque(int x, int y, int z) {
		// block
		Block b = Block.blocksList[getBlockId(x, y, z)];
		return b == null ? false : b.isOpaque();
	}

	public int getHeight() {
		return 256;
	}

	public Vec3Pool getLevelVec3Pool() {
		return levelObj.getLevelVec3Pool();
	}

	public int getSkyBlockTypeBrightness(EnumLightValue enumLightValue, int x, int y, int z) {
		if (y < 0) y = 0;

		if (y >= 256) y = 255;

		if (y >= 0 && y < 256 && x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			int xx;
			int zz;

			xx = (x >> 4) - chunkX;
			zz = (z >> 4) - chunkZ;
			return chunkArray[xx][zz].getSavedLightValue(enumLightValue, x & 15, y, z & 15);
		} else return enumLightValue.defaultLightValue;
	}

	public boolean extendedLevelsInChunkCache() {
		return hasExtendedLevels;
	}

	public int getLightValue(int x, int y, int z) {
		return getLightValue(x, y, z, true);
	}

	public int getLightValue(int x, int y, int z, boolean flag) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z <= 30000000) {
			int xx;
			int zz;

			if (flag) {
				// do other stuff here
			}

			if (y < 0) return 0;
			else if (y >= 256) {
				xx = 15 - levelObj.skylightSubtracted;

				if (xx < 0) xx = 0;

				return xx;
			} else {
				xx = (x >> 4) - chunkX;
				zz = (z >> 4) - chunkZ;
				return chunkArray[xx][zz].getBlockLightValue(x & 15, y, z & 15, levelObj.skylightSubtracted);
			}
		} else return 15;
	}
}
