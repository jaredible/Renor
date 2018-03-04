package renor;

import java.io.File;
import java.io.IOException;

import renor.util.IntegratedServerListenThread;
import renor.util.NetworkListenThread;
import renor.util.ThreadLanServerPing;
import renor.util.crashreport.CrashReport;
import renor.util.logger.ILogAgent;
import renor.util.logger.LogAgent;

public class IntegratedServer extends RenorServer {
	private final Renor renor;
	private final ILogAgent serverLogAgent;
	private IntegratedServerListenThread theServerListeningThread;
	private ThreadLanServerPing lanServerPing;
	private boolean isGamePaused;
	private boolean isPublic;

	public IntegratedServer(Renor renor) {
		this.renor = renor;

		serverProxy = renor.getProxy();
		serverLogAgent = new LogAgent("Renor-Server", " [Server]", new File(renor.renorDataDir, "output-server.log").getAbsolutePath());
		setServerOwner(renor.getSession().getUsername());

		try {
			theServerListeningThread = new IntegratedServerListenThread(this);
		} catch (IOException e) {
			throw new Error();
		}
	}

	protected boolean startServer() throws IOException {
		loadLevel();
		return true;
	}

	public void stopServer() {
		super.stopServer();

		if (lanServerPing != null) {
			lanServerPing.interrupt();
			lanServerPing = null;
		}
	}

	public void initiateShutdown() {
		super.initiateShutdown();

		if (lanServerPing != null) {
			lanServerPing.interrupt();
			lanServerPing = null;
		}
	}

	protected void finalTick(CrashReport crashReport) {
		renor.crash(crashReport);
	}

	public void tick() {
		if (!isGamePaused) super.tick();
	}

	public String shareToLAN() {
		return null;
	}

	protected File getDataDirectory() {
		return renor.renorDataDir;
	}

	public NetworkListenThread getNetworkThread() {
		return getServerListeningThread();
	}

	public ILogAgent getLogAgent() {
		return serverLogAgent;
	}

	public IntegratedServerListenThread getServerListeningThread() {
		return theServerListeningThread;
	}

	public boolean isPublic() {
		return isPublic;
	}
}
