package renor.level;

public class LevelType {
	private final String levelType;
	public static final LevelType FLAT = new LevelType("flat");
	public static final LevelType GENERATE = new LevelType("generate");

	private LevelType(String levelType) {
		this.levelType = levelType;
	}
}
