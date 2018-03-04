package renor.network;

import java.io.IOException;

import renor.IntegratedServer;
import renor.Renor;
import renor.level.LevelClient;
import renor.network.packet.Packet;
import renor.network.packet.Packet0KeepAlive;
import renor.network.packet.Packet1Login;
import renor.network.packet.Packet255KickDisconnect;
import renor.util.PlayerControllerMP;

public class NetClientHandler extends NetHandler {
	private Renor renor;
	private INetworkManager netManager;
	private LevelClient levelClient;
	public int currentServerMaxPlayers = 20;
	private boolean disconnected = false;

	public NetClientHandler(Renor renor, IntegratedServer integratedServer) throws IOException {
		this.renor = renor;

		netManager = new MemoryConnection(renor.getLogAgent(), this);
		integratedServer.getServerListeningThread().a((MemoryConnection) netManager, renor.getSession().getUsername());
	}

	public void cleanup() {
		if (netManager != null) netManager.wakeThreads();

		netManager = null;
		levelClient = null;
	}

	public void processReadPackets() {
		if (!disconnected && netManager != null) netManager.processReadPackets();

		if (netManager != null) netManager.wakeThreads();
	}

	public void handleErrorMessage(String message, Object[] objects) {
	}

	public void handleKickDisconnect(Packet255KickDisconnect packet255KickDisconnect) {
	}

	public void handleLogin(Packet1Login packet1Login) {
		renor.playerController = new PlayerControllerMP(renor, this);
		levelClient = new LevelClient(this, renor.theProfiler, renor.getLogAgent());
		levelClient.isClient = true;
		renor.loadLevel(levelClient);
		renor.thePlayer.entityId = packet1Login.clientEntityId;
		currentServerMaxPlayers = packet1Login.maxPlayers;
	}

	public void handleKeepAlive(Packet0KeepAlive packet0KeepAlive) {
		addToSendQueue(new Packet0KeepAlive(packet0KeepAlive.randomId));
	}

	public void addToSendQueue(Packet packet) {
		if (!disconnected) netManager.addToSendQueue(packet);
	}

	public INetworkManager getNetManager() {
		return netManager;
	}

	public boolean canProcessPacketsAsync() {
		return renor != null && renor.theLevel != null && renor.thePlayer != null && levelClient != null;
	}
}
