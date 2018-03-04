package renor.level.chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import renor.level.Level;
import renor.util.LongHashMap;

public class ChunkProviderClient implements IChunkProvider {
	private List<Chunk> chunkListing = new ArrayList<Chunk>();
	private Level levelObj;
	private EmptyChunk blankChunk;
	private LongHashMap chunkMapping = new LongHashMap();

	public ChunkProviderClient(Level level) {
		levelObj = level;

		blankChunk = new EmptyChunk(level, 0, 0);
	}

	private void generateTerrain(byte[] data, int x, int z) {
		int xx;
		int yy;
		int zz;

		for (xx = 0; xx < 16; ++xx)
			for (zz = 0; zz < 16; ++zz)
				for (yy = 0; yy < 64; ++yy)
					data[xx * 256 * 16 | zz * 256 | yy] = 1;

		if (x < 0 && z >= 0 && x >= -12 && z <= 12) {
			for (xx = 0; xx < 16; ++xx)
				for (zz = 0; zz < 16; ++zz)
					data[xx * 256 * 16 | zz * 256 | 64 - 1] = 2;
		}

		Random r = new Random();

		if (x >= 0 && z >= 0 && x <= 12 && z <= 12) {
			for (xx = 0; xx < 4; ++xx)
				for (zz = 0; zz < 4; ++zz)
					for (yy = 0; yy < 12 * 4; ++yy)
						data[xx * 256 * 16 * 4 | zz * 256 * 4 | 64 + yy * 4] = (byte) (r.nextInt(10) == 0 ? 3 : 1);

			for (xx = 0; xx < 4; ++xx)
				for (zz = 0; zz < 4; ++zz)
					data[xx * 256 * 16 * 4 | zz * 256 * 4 | 64] = 0;
		}
	}

	public boolean chunkExists(int x, int z) {
		return true;
	}

	public Chunk provideChunk(int x, int z) {
		Chunk c = (Chunk) chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
		return c == null ? blankChunk : c;
	}

	public Chunk loadChunk(int x, int z) {
		byte[] data = new byte[65536];
		generateTerrain(data, x, z);

		// chunk
		Chunk c = new Chunk(levelObj, data, x, z);
		c.generateSkylightMap();

		chunkMapping.add(ChunkCoordIntPair.chunkXZ2Int(x, z), c);
		c.isChunkLoaded = true;
		return c;
	}

	public void populate(int x, int z) {
	}

	public boolean unloadQueuedChunks() {
		return false;
	}

	public boolean canSave() {
		return false;
	}

	public String makeString() {
		return "MultiplayerChunkCache: " + chunkMapping.getNumHashElements();
	}

	public void unloadChunk(int x, int z) {
		Chunk chunk = provideChunk(x, z);

		if (!chunk.isEmpty()) chunk.onChunkUnload();

		chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(x, z));
		chunkListing.remove(chunk);
	}
}
