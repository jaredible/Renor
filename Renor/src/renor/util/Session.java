package renor.util;

public class Session {
	private final String username;
	private final String sessionId;

	public Session(String username, String sessionId) {
		this.username = username;
		this.sessionId = sessionId;
	}

	public String getUsername() {
		return username;
	}

	public String getSessionId() {
		return sessionId;
	}
}
