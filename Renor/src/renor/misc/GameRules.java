package renor.misc;

import java.util.TreeMap;

public class GameRules {
	private TreeMap<String, GameRuleValue> theGameRules = new TreeMap<String, GameRuleValue>();

	public GameRules() {
		addGameRule("keepInventory", "false");
		addGameRule("doMobLoot", "true");
	}

	public void addGameRule(String description, String value) {
		theGameRules.put(description, new GameRuleValue(value));
	}

	public boolean getGameRuleBooleanValue(String value) {
		return true;
	}
}
