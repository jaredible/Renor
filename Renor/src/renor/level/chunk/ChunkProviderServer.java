package renor.level.chunk;

public class ChunkProviderServer implements IChunkProvider {
	public boolean chunkExists(int x, int z) {
		return false;
	}

	public Chunk provideChunk(int x, int z) {
		return null;
	}

	public Chunk loadChunk(int x, int z) {
		return null;
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
