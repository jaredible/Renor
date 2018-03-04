package renor.util;

import renor.Renor;

public class LanServer {
	private String lanServerMotd;
	private String lanServerIpPort;
	private long timeLastSeen;

	public LanServer(String motd, String ipPort) {
		lanServerMotd = motd;
		lanServerIpPort = ipPort;
		timeLastSeen = Renor.getSystemTime();
	}

	public String getServerMotd() {
		return lanServerMotd;
	}

	public String getServerIpPort() {
		return lanServerIpPort;
	}

	public void updateLastSeen() {
		timeLastSeen = Renor.getSystemTime();
	}
}
