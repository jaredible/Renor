package renor.util;

import java.io.IOException;

import renor.IntegratedServer;
import renor.network.MemoryConnection;

public class IntegratedServerListenThread extends NetworkListenThread {
	private String username;
	private final MemoryConnection netMemoryConnection;
	private MemoryConnection theMemoryConnection;

	public IntegratedServerListenThread(IntegratedServer integratedServer) throws IOException {
		super(integratedServer);

		netMemoryConnection = new MemoryConnection(integratedServer.getLogAgent(), null);
	}

	public void a(MemoryConnection memoryConnection, String username) {
		theMemoryConnection = memoryConnection;
		this.username = username;
	}
}
