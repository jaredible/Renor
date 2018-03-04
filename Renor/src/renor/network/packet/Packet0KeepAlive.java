package renor.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import renor.network.NetHandler;

public class Packet0KeepAlive extends Packet {
	public int randomId;

	public Packet0KeepAlive() {
	}

	public Packet0KeepAlive(int randomId) {
		this.randomId = randomId;
	}

	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		randomId = dataInputStream.readInt();
	}

	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(randomId);
	}

	public void processPacket(NetHandler netHandler) {
		netHandler.handleKeepAlive(this);
	}

	public int getPacketSize() {
		return 4;
	}

	public boolean canProcessAsync() {
		return true;
	}
}
