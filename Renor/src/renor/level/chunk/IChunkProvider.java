package renor.level.chunk;

public abstract interface IChunkProvider {
	public abstract boolean chunkExists(int x, int z);

	public abstract Chunk provideChunk(int x, int z);

	public abstract Chunk loadChunk(int x, int z);

	public abstract void populate(int x, int z);

	public abstract boolean unloadQueuedChunks();

	public abstract boolean canSave();

	public abstract String makeString();
}
