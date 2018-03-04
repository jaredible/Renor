package renor.util;

import renor.RenorServer;

public class ThreadRenorServer extends Thread {
	final RenorServer theServer;

	public ThreadRenorServer(RenorServer renorServer, String name) {
		super(name);
		theServer = renorServer;
	}

	public void run() {
		theServer.run();
	}
}
