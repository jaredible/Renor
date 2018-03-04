package renor.level.chunk;

public class ChunkCoordIntPair {
	public final int chunkXPos;
	public final int chunkZPos;

	public ChunkCoordIntPair(int x, int z) {
		chunkXPos = x;
		chunkZPos = z;
	}

	public static long chunkXZ2Int(int x, int z) {
		return (long) x & 4294967295l | ((long) z & 4294967295l) << 32;
	}
}
