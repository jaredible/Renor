package renor.network;

import renor.network.packet.Packet;

public abstract interface INetworkManager {
	public abstract void addToSendQueue(Packet packet);

	public abstract void wakeThreads();

	public abstract void processReadPackets();

	public abstract void closeConnections();
}
