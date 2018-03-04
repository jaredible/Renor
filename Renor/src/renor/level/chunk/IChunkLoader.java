package renor.level.chunk;

import java.io.IOException;

import renor.level.Level;
import renor.util.throwable.RenorException;

public abstract interface IChunkLoader {
	public abstract Chunk loadChunk(Level level, int x, int z);

	public abstract void saveChunk(Level level, Chunk chunk) throws RenorException, IOException;

	public abstract void saveExtraData();
}
