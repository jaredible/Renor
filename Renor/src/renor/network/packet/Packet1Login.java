package renor.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import renor.network.NetHandler;

public class Packet1Login extends Packet {
	public int clientEntityId = 0;
	public byte maxPlayers;

	public Packet1Login() {
	}

	public Packet1Login(int clientEntityId, int maxPlayers) {
		this.clientEntityId = clientEntityId;
		this.maxPlayers = (byte) maxPlayers;
	}

	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		clientEntityId = dataInputStream.readInt();
		maxPlayers = dataInputStream.readByte();
	}

	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(clientEntityId);
		dataOutputStream.writeByte(maxPlayers);
	}

	public void processPacket(NetHandler netHandler) {
		netHandler.handleLogin(this);
	}

	public int getPacketSize() {
		return 5;
	}
}
