package renor.level.chunk;

import java.util.ArrayList;
import java.util.List;

import renor.aabb.AxisAlignedBB;
import renor.level.Level;
import renor.level.block.Block;
import renor.level.entity.Entity;
import renor.misc.BlockStorage;
import renor.util.EnumLightValue;
import renor.util.MathHelper;

public class Chunk {
	public List<Entity> entityLists[];
	public Level levelObj;
	private BlockStorage[] storageArrays;
	public long lastSaveTime;
	public final int xPosition;
	public final int zPosition;
	public int heightMapMinimum;
	private int queuedLightChecks;
	public int[] heightMap;
	public static boolean isLit;
	public boolean isChunkLoaded;
	public boolean isModified;
	private boolean isGapLightingUpdated;
	public boolean hasEntities;
	public boolean[] updateSkylightColumns;

	public Chunk(Level level, int x, int z) {
		levelObj = level;
		xPosition = x;
		zPosition = z;

		entityLists = new List[16];
		storageArrays = new BlockStorage[16];

		for (int i = 0; i < entityLists.length; ++i)
			entityLists[i] = new ArrayList<Entity>();

		queuedLightChecks = 4096;
		heightMap = new int[256];
		isModified = false;
		isGapLightingUpdated = false;
		hasEntities = false;
		updateSkylightColumns = new boolean[256];
	}

	public Chunk(Level level, byte[] data, int x, int z) {
		this(level, x, z);

		int h = data.length / 256;

		for (int xx = 0; xx < 16; ++xx) {
			for (int zz = 0; zz < 16; ++zz) {
				for (int yy = 0; yy < h; ++yy) {
					// blockId
					byte id = data[xx * h * 16 | zz * h | yy];

					if (id != 0) {
						int yyy = yy >> 4;

						if (storageArrays[yyy] == null) storageArrays[yyy] = new BlockStorage(yyy << 4);

						storageArrays[yyy].setBlockId(xx, yy & 15, zz, id);
					}
				}
			}
		}
	}

	public int getHeightValue(int x, int z) {
		return heightMap[z << 4 | x];
	}

	public int getTopFilledSegment() {
		for (int i = storageArrays.length - 1; i >= 0; --i)
			if (storageArrays[i] != null) return storageArrays[i].getYLocation();

		return 0;
	}

	public void generateHeightMap() {
		int t = getTopFilledSegment();

		for (int x = 0; x < 16; ++x) {
			int z = 0;

			while (z < 16) {
				int y = t + 16 - 1;

				while (true) {
					if (y > 0) {
						// blockId
						int id = getBlockId(x, y - 1, z);

						if (Block.lightOpacity[id] == 0) {
							--y;
							continue;
						}

						heightMap[z << 4 | x] = y;
					}

					++z;
					break;
				}
			}
		}

		isModified = true;
	}

	public void generateSkylightMap() {
		int t = getTopFilledSegment();
		heightMapMinimum = Integer.MAX_VALUE;
		int x;
		int z;

		for (x = 0; x < 16; ++x) {
			z = 0;

			while (z < 16) {
				int y = t + 16 - 1;

				while (true) {
					if (y > 0) {
						if (getBlockLightOpacity(x, y - 1, z) == 0) {
							--y;
							continue;
						}

						heightMap[z << 4 | x] = y;

						if (y < heightMapMinimum) heightMapMinimum = y;
					}

					y = 15;
					int yy = t + 16 - 1;

					do {
						y -= getBlockLightOpacity(x, yy, z);

						if (y > 0) {
							BlockStorage storage = storageArrays[yy >> 4];

							if (storage != null) {
								storage.setSkyLightValue(x, yy & 15, z, y);
								levelObj.markBlockForRenderUpdate((xPosition << 4) + x, yy, (zPosition << 4) + z);
							}
						}

						--yy;
					} while (yy > 0 && y > 0);

					++z;
					break;
				}
			}
		}

		isModified = true;

		for (x = 0; x < 16; ++x)
			for (z = 0; z < 16; ++z)
				propagateSkylightOcclusion(x, z);
	}

	private void propagateSkylightOcclusion(int x, int z) {
		updateSkylightColumns[x + z * 16] = true;
		isGapLightingUpdated = true;
	}

	public int getBlockId(int x, int y, int z) {
		if (y >> 4 >= storageArrays.length) return 0;
		else {
			// blockStorage
			BlockStorage storage = storageArrays[y >> 4];
			return storage != null ? storage.getBlockId(x, y & 15, z) : 0;
		}
	}

	public int getBlockMetadata(int x, int y, int z) {
		if (y >> 4 >= storageArrays.length) return 0;
		else {
			// blockStorage
			BlockStorage storage = storageArrays[y >> 4];
			return storage != null ? storage.getBlockMetadata(x, y & 15, z) : 0;
		}
	}

	public boolean setBlockIdWithMetadata(int x, int y, int z, int id, int metadata) {
		int h = heightMap[z << 4 | x];
		int oldId = getBlockId(x, y, z);
		int oldMetadata = getBlockMetadata(x, y, z);

		// blockStorage
		BlockStorage storage = storageArrays[y >> 4];
		// needsSkylightUpdate
		boolean flag = false;

		if (oldId == id && oldMetadata == metadata) return false;
		else {
			if (storage == null) {
				if (id == 0) return false;

				storage = storageArrays[y >> 4] = new BlockStorage(y >> 4 << 4);
				flag = y >= h;
			}

			int xx = xPosition * 16 + x;
			int zz = zPosition * 16 + z;

			if (oldId != 0 && !levelObj.isClient) Block.blocksList[oldId].onSetBlockIdWithMetadata(levelObj, xx, y, zz, oldMetadata);

			storage.setBlockId(x, y & 15, z, id);

			if (oldId != 0) {
				if (!levelObj.isClient) Block.blocksList[oldId].breakBlock(levelObj, xx, y, zz, oldId, oldMetadata);
			}

			if (storage.getBlockId(x, y & 15, z) != id) return false;
			else {
				storage.setBlockMetadata(x, y & 15, z, metadata);

				if (flag) generateSkylightMap();
				else {
					if (Block.lightOpacity[id & 4095] > 0) {
						if (y >= h) relightBlock(x, y + 1, z);
					} else if (y == h - 1) relightBlock(x, y, z);

					propagateSkylightOcclusion(x, z);
				}

				if (id != 0) {
					if (!levelObj.isClient) Block.blocksList[id].onBlockAdded(levelObj, xx, y, zz);
				}

				isModified = true;
				return true;
			}
		}
	}

	public int getBlockLightOpacity(int x, int y, int z) {
		return Block.lightOpacity[getBlockId(x, y, z)];
	}

	public int getSavedLightValue(EnumLightValue enumLightValue, int x, int y, int z) {
		// blockStorage
		BlockStorage storage = storageArrays[y >> 4];
		return storage == null ? (canBlockSeeTheSky(x, y, z) ? enumLightValue.defaultLightValue : 0) : (enumLightValue == EnumLightValue.Sky ? storage.getSkyLightValue(x, y & 15, z) : (enumLightValue == EnumLightValue.Block ? storage.getBlockLightValue(x, y & 15, z) : enumLightValue.defaultLightValue));
	}

	public void setLightValue(EnumLightValue enumLightValue, int x, int y, int z, int lightValue) {
		// blockStorage
		BlockStorage storage = storageArrays[y >> 4];

		if (storage == null) {
			storage = storageArrays[y >> 4] = new BlockStorage(y >> 4 << 4);
			generateSkylightMap();
		}

		isModified = true;

		if (enumLightValue == EnumLightValue.Sky) storage.setSkyLightValue(x, y & 15, z, lightValue);
		else if (enumLightValue == EnumLightValue.Block) storage.setBlockLightValue(x, y & 15, z, lightValue);
	}

	public int getBlockLightValue(int x, int y, int z, int lightValue) {
		// blockStorage
		BlockStorage storage = storageArrays[y >> 4];

		if (storage == null) return lightValue < EnumLightValue.Sky.defaultLightValue ? EnumLightValue.Sky.defaultLightValue - lightValue : 0;
		else {
			// lightValue2
			int lv = storage.getSkyLightValue(x, y & 15, z);

			if (lv > 0) isLit = true;

			lv -= lightValue;
			// blockLight
			int bl = storage.getBlockLightValue(x, y & 15, z);

			if (bl > lv) lv = bl;

			return lv;
		}
	}

	public boolean canBlockSeeTheSky(int x, int y, int z) {
		return y >= heightMap[z << 4 | x];
	}

	public void onChunkLoad() {
		isChunkLoaded = true;
	}

	public void onChunkUnload() {
		isChunkLoaded = false;
	}

	public void addEntity(Entity entity) {
		hasEntities = true;
		int cx = MathHelper.floor_double(entity.posX / 16.0);
		int cz = MathHelper.floor_double(entity.posZ / 16.0);

		if (cx != xPosition || cz != zPosition) {
			levelObj.getLevelLogAgent().logSevere("Wrong location! " + entity);
			Thread.dumpStack();
		}

		int cy = MathHelper.floor_double(entity.posY / 16.0);

		if (cy < 0) cy = 0;

		if (cy >= entityLists.length) cy = entityLists.length - 1;

		entity.addedToChunk = true;
		entity.chunkCoordX = xPosition;
		entity.chunkCoordY = cy;
		entity.chunkCoordZ = zPosition;
		entityLists[cy].add(entity);
	}

	public void removeEntity(Entity entity) {
		removeEntityAtIndex(entity, entity.chunkCoordY);
	}

	public void removeEntityAtIndex(Entity entity, int chunkY) {
		if (chunkY < 0) chunkY = 0;

		if (chunkY >= entityLists.length) chunkY = entityLists.length - 1;

		entityLists[chunkY].remove(entity);
	}

	public BlockStorage[] getBlockStorageArray() {
		return storageArrays;
	}

	public boolean isEmpty() {
		return false;
	}

	public boolean getAreLevelsEmpty(int minY, int maxY) {
		if (minY < 0) minY = 0;

		if (maxY >= 256) maxY = 255;

		for (int i = minY; i <= maxY; i += 16) {
			// blockStorage
			BlockStorage s = storageArrays[i >> 4];

			if (s != null && !s.isEmpty()) return false;
		}

		return true;
	}

	private void relightBlock(int x, int y, int z) {
		int h = heightMap[z << 4 | x] & 255;
		int hh = h;

		if (y > h) hh = y;

		while (hh > 0 && getBlockLightOpacity(x, hh - 1, z) == 0)
			--hh;

		if (hh != h) {
			levelObj.markBlocksDirtyVertical(x + xPosition * 16, z + zPosition * 16, hh, h);
			heightMap[z << 4 | x] = hh;
			int xx = xPosition * 16 + x;
			int zz = zPosition * 16 + z;
			int i;
			int n;

			// blockStorage
			BlockStorage storage;

			if (hh < h) {
				for (i = hh; i < h; ++i) {
					storage = storageArrays[i >> 4];

					if (storage != null) {
						storage.setSkyLightValue(x, i & 15, z, 15);
						levelObj.markBlockForRenderUpdate((xPosition << 4) + x, i, (zPosition << 4) + z);
					}
				}
			} else {
				for (i = h; i < hh; ++i) {
					storage = storageArrays[i >> 4];

					if (storage != null) {
						storage.setSkyLightValue(x, i & 15, z, 0);
						levelObj.markBlockForRenderUpdate((xPosition << 4) + x, i, (zPosition << 4) + z);
					}
				}
			}

			i = 15;

			while (hh > 0 && i > 0) {
				--hh;
				n = getBlockLightOpacity(x, hh, z);

				if (n == 0) n = 1;

				i -= n;

				if (i < 0) i = 0;

				// blockStorage2
				BlockStorage s = storageArrays[hh >> 4];

				if (s != null) s.setSkyLightValue(x, hh & 15, z, i);
			}

			i = heightMap[z << 4 | x];
			n = h;
			int yy = i;

			if (i < h) {
				n = i;
				yy = h;
			}

			if (i < heightMapMinimum) heightMapMinimum = i;

			updateSkylightNeighborHeight(xx - 1, zz, n, yy);
			updateSkylightNeighborHeight(xx + 1, zz, n, yy);
			updateSkylightNeighborHeight(xx, zz - 1, n, yy);
			updateSkylightNeighborHeight(xx, zz + 1, n, yy);
			updateSkylightNeighborHeight(xx, zz, n, yy);

			isModified = true;
		}
	}

	private void checkSkylightNeighborHeight(int x, int z, int y) {
		int h = levelObj.getHeightValue(x, z);

		if (h > y) updateSkylightNeighborHeight(x, z, y, h + 1);
		else if (h < y) updateSkylightNeighborHeight(x, z, h, y + 1);
	}

	private void updateSkylightNeighborHeight(int x, int z, int minY, int maxY) {
		if (maxY > minY && levelObj.doChunksNearChunkExist(x, 0, z, 16)) {
			for (int i = minY; i < maxY; ++i)
				levelObj.updateLightByType(EnumLightValue.Sky, x, i, z);

			isModified = true;
		}
	}

	public void getEntitiesWithinAABBForEntity(Entity entity, AxisAlignedBB axisAlignedBB, List<Entity> entities) {
		int minY = MathHelper.floor_double((axisAlignedBB.minY - 2.0) / 16.0);
		int maxY = MathHelper.floor_double((axisAlignedBB.maxY + 2.0) / 16.0);

		if (minY < 0) {
			minY = 0;
			maxY = Math.max(minY, maxY);
		}

		if (maxY >= entityLists.length) {
			maxY = entityLists.length - 1;
			minY = Math.min(minY, maxY);
		}

		for (int i = minY; i <= maxY; ++i) {
			List<Entity> es = entityLists[i];

			for (int j = 0; j < es.size(); ++j) {
				Entity e = es.get(j);

				if (e != entity && e.boundingBox.intersectsWith(axisAlignedBB)) entities.add(e);
			}
		}
	}
}
