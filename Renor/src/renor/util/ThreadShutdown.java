package renor.util;

import renor.Renor;

public final class ThreadShutdown extends Thread {
	public void run() {
		Renor.stopIntegratedServer();
	}
}
