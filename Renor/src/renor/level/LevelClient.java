package renor.level;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import renor.Renor;
import renor.level.chunk.Chunk;
import renor.level.chunk.ChunkCoordIntPair;
import renor.level.chunk.ChunkProviderClient;
import renor.level.chunk.ChunkProviderFlat;
import renor.level.chunk.IChunkProvider;
import renor.level.entity.Entity;
import renor.network.NetClientHandler;
import renor.util.logger.ILogAgent;
import renor.util.profiler.Profiler;

public class LevelClient extends Level {
	private Set<Entity> entitySpawnQueue = new HashSet<Entity>();
	private final Set<ChunkCoordIntPair> previousActiveChunkSet = new HashSet<ChunkCoordIntPair>();
	private final Renor renor = Renor.getRenor();
	private NetClientHandler sendQueue;
	private ChunkProviderClient clientChunkProvider;

	public LevelClient(NetClientHandler netClientHandler, Profiler profiler, ILogAgent logAgent) {
		super(profiler, logAgent);
		sendQueue = netClientHandler;

		// TODO initial chunk load test
		// test!
		if (clientChunkProvider != null) initialChunkLoadTest();

		setLevelTime(22000);
	}

	private void initialChunkLoadTest() {
		for (int x = -192; x <= 192; x += 16)
			for (int z = -192; z <= 192; z += 16)
				clientChunkProvider.loadChunk(x >> 4, z >> 4);
	}

	protected IChunkProvider createChunkProvider() {
		boolean test = true;
		if (test) {
			clientChunkProvider = new ChunkProviderClient(this);
			return clientChunkProvider;
		} else return new ChunkProviderFlat(this);
	}

	public void playSound(double x, double y, double z, String name, float volume, float pitch) {
		if (true) return;

		float r = 16.0f;

		if (volume > 1.0f) r *= volume;

		double d = renor.renderViewEntity.getDistanceSq(x, y, z);

		if (d < (double) (r * r)) {
		}

		renor.sndManager.playSound(name, (float) x, (float) y, (float) z, volume, pitch);
	}

	public void tick() {
		super.tick();
		incrementTotalLevelTime(getTotalLevelTime() + 1);
		setLevelTime(getLevelTime() + 1);

		// theProfiler.startSection("connection");
		// sendQueue.processReadPackets();
		// theProfiler.endStartSection("chunkCache");
		// clientChunkProvider.unloadQueuedChunks();
		theProfiler.endStartSection("blocks");
		tickBlocksAndAmbiance();
		// theProfiler.endSection();
	}

	protected void tickBlocksAndAmbiance() {
		super.tickBlocksAndAmbiance();

		if (true) return;

		previousActiveChunkSet.retainAll(activeChunkSet);

		if (previousActiveChunkSet.size() == activeChunkSet.size()) previousActiveChunkSet.clear();

		int i = 0;
		// activeChunkSetIterator
		Iterator<ChunkCoordIntPair> var0 = activeChunkSet.iterator();

		while (var0.hasNext()) {
			// chunkCoordIntPair
			ChunkCoordIntPair var1 = var0.next();

			if (!previousActiveChunkSet.contains(var1)) {
				int cx = var1.chunkXPos * 16;
				int cz = var1.chunkZPos * 16;
				theProfiler.startSection("getChunk");
				// chunk
				Chunk c = getChunkFromChunkCoords(cx, cz);
				// do other stuff here
				theProfiler.endSection();
				previousActiveChunkSet.add(var1);
				++i;

				if (i >= 10) return;
			}
		}
	}

	public void doPreChunk(int x, int z, boolean par0) {
		if (par0) clientChunkProvider.loadChunk(x, z);
		else clientChunkProvider.unloadChunk(x, z);

		if (!par0) markBlockRangeForRenderUpdate(x * 16, 0, z * 16, x * 16 + 15, 256, z * 16 + 15);
	}
}
