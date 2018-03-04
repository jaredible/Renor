package renor.level.chunk;

import renor.level.Level;

public class EmptyChunk extends Chunk {
	public EmptyChunk(Level level, int x, int z) {
		super(level, x, z);
	}

	public void generateHeightMap() {
	}

	public void generateSkylightMap() {
	}

	public int getBlockId(int x, int y, int z) {
		return 0;
	}

	public void onChunkLoad() {
	}

	public void onChunkUnload() {
	}
}
