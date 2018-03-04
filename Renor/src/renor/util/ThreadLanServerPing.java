package renor.util;

import java.io.IOException;
import java.net.DatagramSocket;

public class ThreadLanServerPing extends Thread {
	private final String motd;
	private final String address;
	private final DatagramSocket socket;
	private boolean isStopping = true;

	public ThreadLanServerPing(String motd, String address) throws IOException {
		super("LanServerPinger");
		this.motd = motd;
		this.address = address;

		setDaemon(true);
		socket = new DatagramSocket();
	}

	public void run() {
	}

	public void interrupt() {
		super.interrupt();
		isStopping = false;
	}
}
