package renor.level;

public final class LevelSettings {
	private final long seed;

	public LevelSettings(long seed) {
		this.seed = seed;
	}

	public long getSeed() {
		return seed;
	}

	public static enum GameType {
	}
}
