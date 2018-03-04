package renor.level.chunk;

import java.util.Random;

import renor.level.Level;

public class ChunkProviderGenerate implements IChunkProvider {
	private Random rand;
	private Level levelObj;

	public ChunkProviderGenerate(Level level, long seed) {
		levelObj = level;
		rand = new Random(seed);
	}

	public void generateTerrain(int x, int z, byte[] data) {
		int xx;
		int yy = 0;
		int zz;

		for (xx = 0; xx < 16; ++xx)
			for (zz = 0; zz < 16; ++zz)
				data[xx << 11 | zz << 7 | yy] = 1;
	}

	public boolean chunkExists(int x, int z) {
		return false;
	}

	public Chunk provideChunk(int x, int z) {
		rand.setSeed((long) x * 341873128712l + (long) z * 132897987541l);
		byte[] data = new byte[65536];
		generateTerrain(x, z, data);

		// chunk
		Chunk c = new Chunk(levelObj, data, x, z);
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
		return false;
	}

	public String makeString() {
		return null;
	}
}
