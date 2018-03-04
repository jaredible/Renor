package renor.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import renor.network.NetHandler;

public class Packet255KickDisconnect extends Packet {
	public String reason;

	public Packet255KickDisconnect() {
	}

	public Packet255KickDisconnect(String reason) {
		this.reason = reason;
	}

	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		reason = readString(dataInputStream, 256);
	}

	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		writeString(reason, dataOutputStream);
	}

	public void processPacket(NetHandler netHandler) {
		netHandler.handleKickDisconnect(this);
	}

	public int getPacketSize() {
		return reason.length();
	}
}
