package renor.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import renor.network.packet.Packet;
import renor.util.logger.ILogAgent;

public class MemoryConnection implements INetworkManager {
	private static final SocketAddress mySocketAddress = new InetSocketAddress("127.0.0.1", 0);
	private final List<Packet> readPacketCache = Collections.synchronizedList(new ArrayList<Packet>());
	private String shutdownReason = "";
	private Object[] objects;
	private final ILogAgent logAgent;
	private NetHandler myNetHandler;
	private MemoryConnection pairedConnection;
	private boolean gamePaused = false;
	private boolean shuttingDown = false;

	public MemoryConnection(ILogAgent logAgent, NetHandler netHandler) {
		this.logAgent = logAgent;
		myNetHandler = netHandler;
	}

	public void addToSendQueue(Packet packet) {
		if (!shuttingDown) pairedConnection.processOrCachePacket(packet);
	}

	public void wakeThreads() {
	}

	public void processReadPackets() {
		// maxPacketsToProcess
		int max = 2500;

		while (max-- >= 0 && !readPacketCache.isEmpty()) {
			// packet
			Packet p = readPacketCache.remove(0);
			p.processPacket(myNetHandler);
		}

		if (readPacketCache.size() > max) logAgent.logWarning("Memory connection overburdened, after processing 2500 packets, we still have " + readPacketCache.size() + " to go!");

		if (shuttingDown && readPacketCache.isEmpty()) myNetHandler.handleErrorMessage(shutdownReason, objects);
	}

	public void closeConnections() {
		pairedConnection = null;
		myNetHandler = null;
	}

	public void setGamePaused(boolean paused) {
		gamePaused = paused;
	}

	public void processOrCachePacket(Packet packet) {
		if (packet.canProcessAsync() && myNetHandler.canProcessPacketsAsync()) packet.processPacket(myNetHandler);
		else readPacketCache.add(packet);
	}
}
