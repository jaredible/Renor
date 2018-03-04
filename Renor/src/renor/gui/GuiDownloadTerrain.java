package renor.gui;

import renor.network.NetClientHandler;
import renor.network.packet.Packet0KeepAlive;

public class GuiDownloadTerrain extends GuiScreen {
	private NetClientHandler netHandler;
	private int updateCounter = 0;

	public GuiDownloadTerrain(NetClientHandler netClientHandler) {
		netHandler = netClientHandler;
	}

	public void initGui() {
		buttonList.clear();
	}

	public void updateScreen() {
		++updateCounter;

		if (updateCounter % 20 == 0) netHandler.addToSendQueue(new Packet0KeepAlive());

		if (netHandler != null) netHandler.processReadPackets();
	}

	public void drawScreen(int mouseX, int mouseY, float partialTickTime) {
		// do stuff here
	}
}
