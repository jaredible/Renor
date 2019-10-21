package renor.level.chunk;

public class ChunkCoordIntPair {
	public final int chunkXPos;
	public final int chunkZPos;

	public ChunkCoordIntPair(int x, int z) {
		chunkXPos = x;
		chunkZPos = z;
	}

	public static long chunkXZ2Int(int x, int z) {
		return (long) x & 0xffffffffl | ((long) z & 0xffffffffl) << 32;
	}
}
