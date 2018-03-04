package renor.level.chunk;

import renor.level.Level;
import renor.misc.BlockStorage;

public class ChunkProviderFlat implements IChunkProvider {
	private Level levelObj;
	private final byte[] cachedBlockIds = new byte[256];

	public ChunkProviderFlat(Level level) {
		levelObj = level;
	}

	public boolean chunkExists(int x, int z) {
		return true;
	}

	public Chunk provideChunk(int x, int z) {
		// chunk
		Chunk c = new Chunk(levelObj, x, z);

		int y = 0 >> 4;
		BlockStorage storage = c.getBlockStorageArray()[y];

		if (storage == null) {
			storage = new BlockStorage(0);
			c.getBlockStorageArray()[y] = storage;
		}

		int xx;
		int zz;

		for (xx = 0; xx < 16; ++xx)
			for (zz = 0; zz < 16; ++zz)
				storage.setBlockId(xx, 0 & 15, zz, 1 & 255);

		c.generateSkylightMap();

		return c;
	}

	public Chunk loadChunk(int x, int z) {
		return provideChunk(x, z);
	}

	public void populate(int x, int z) {
	}

	public boolean unloadQueuedChunks() {
		return false;
	}

	public boolean canSave() {
		return true;
	}

	public String makeString() {
		return null;
	}
}
