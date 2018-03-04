package renor.misc;

public class GameRuleValue {
	private String valueString;
	private boolean valueBoolean;

	public GameRuleValue(String value) {
		setValue(value);
	}

	public void setValue(String value) {
		valueString = value;
		valueBoolean = Boolean.parseBoolean(value);
	}
}
