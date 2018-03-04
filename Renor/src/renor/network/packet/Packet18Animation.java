package renor.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import renor.level.entity.Entity;
import renor.network.NetHandler;

public class Packet18Animation extends Packet {
	public int entityId;
	public int animate;

	public Packet18Animation() {
	}

	public Packet18Animation(Entity entity, int animate) {
		entityId = entity.entityId;
		this.animate = animate;
	}

	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		entityId = dataInputStream.readInt();
		animate = dataInputStream.readByte();
	}

	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeInt(entityId);
		dataOutputStream.writeByte(animate);
	}

	public void processPacket(NetHandler netHandler) {
		netHandler.handleAnimation(this);
	}

	public int getPacketSize() {
		return 5;
	}
}
