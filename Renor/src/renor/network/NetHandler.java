package renor.network;

import renor.network.packet.Packet;
import renor.network.packet.Packet0KeepAlive;
import renor.network.packet.Packet18Animation;
import renor.network.packet.Packet1Login;
import renor.network.packet.Packet255KickDisconnect;

public abstract class NetHandler {
	public void unexpectedPacket(Packet packet) {
	}

	public void handleErrorMessage(String message, Object[] objects) {
	}

	public void handleKickDisconnect(Packet255KickDisconnect packet255KickDisconnect) {
	}

	public void handleLogin(Packet1Login packet1Login) {
		unexpectedPacket(packet1Login);
	}

	public void handleKeepAlive(Packet0KeepAlive packet0KeepAlive) {
		unexpectedPacket(packet0KeepAlive);
	}

	public void handleAnimation(Packet18Animation packet18Animation) {
	}

	public boolean canProcessPacketsAsync() {
		return false;
	}
}
