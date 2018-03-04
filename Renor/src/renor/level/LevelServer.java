package renor.level;

import renor.RenorServer;
import renor.level.chunk.IChunkProvider;
import renor.util.logger.ILogAgent;
import renor.util.profiler.Profiler;

public class LevelServer extends Level {
	private final RenorServer renorServer;

	public LevelServer(RenorServer renorServer, Profiler profiler, ILogAgent logAgent) {
		super(profiler, logAgent);
		this.renorServer = renorServer;
	}

	protected IChunkProvider createChunkProvider() {
		return null;
	}

	public void tick() {
		super.tick();
	}

	public void tickBlocksAndAmbiance() {
	}

	public void flush() {
	}
}
