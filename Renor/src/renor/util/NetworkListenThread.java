package renor.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import renor.RenorServer;
import renor.network.NetServerHandler;

public abstract class NetworkListenThread {
	private final List<NetServerHandler> connection = Collections.synchronizedList(new ArrayList<NetServerHandler>());
	private final RenorServer renorServer;
	public volatile boolean isListening;

	public NetworkListenThread(RenorServer renorServer) throws IOException {
		this.renorServer = renorServer;

		isListening = true;
	}

	public void networkTick() {
	}
}
