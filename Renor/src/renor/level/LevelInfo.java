package renor.level;

import renor.misc.GameRules;

public class LevelInfo {
	private GameRules theGameRules;
	private long randomSeed;
	private long sizeOnDisk;
	private long lastTimePlayed;
	private long totalTime;
	private long levelTime;
	private int spawnX;
	private int spawnY;
	private int spawnZ;

	public LevelInfo() {
		theGameRules = new GameRules();
	}

	public long getTotatLevelTime() {
		return totalTime;
	}

	public void incrementTotalLevelTime(long n) {
		totalTime = n;
	}

	public long getLevelTime() {
		return levelTime;
	}

	public void setLevelTime(long n) {
		levelTime = n;
	}

	public GameRules getGameRulesInstance() {
		return theGameRules;
	}
}
