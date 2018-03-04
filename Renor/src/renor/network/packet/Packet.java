package renor.network.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import renor.network.NetHandler;

public abstract class Packet {
	public static long sentId;
	public static long sentSize;
	public static long receivedId;
	public static long receivedSize;

	public abstract void readPacketData(DataInputStream dataInputStream) throws IOException;

	public abstract void writePacketData(DataOutputStream dataOutputStream) throws IOException;

	public abstract void processPacket(NetHandler netHandler);

	public abstract int getPacketSize();

	public boolean canProcessAsync() {
		return false;
	}

	public static String readString(DataInputStream dataInputStream, int maxLength) throws IOException {
		short readLength = dataInputStream.readShort();

		if (readLength > maxLength) throw new IOException("Received string length longer than maximum allowed (" + readLength + " > " + maxLength + ")");
		else if (readLength < 0) throw new IOException("Received string length is less than zero! Weird string!");
		else {
			// stringBuilder
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < readLength; ++i)
				sb.append(dataInputStream.readChar());

			return sb.toString();
		}
	}

	public static void writeString(String string, DataOutputStream dataOutputStream) throws IOException {
		if (string.length() > 32767) throw new IOException("String too big");
		else {
			dataOutputStream.writeShort(string.length());
			dataOutputStream.writeChars(string);
		}
	}
}
