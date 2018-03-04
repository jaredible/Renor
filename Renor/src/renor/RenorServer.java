package renor;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;

import renor.aabb.AxisAlignedBB;
import renor.network.packet.Packet;
import renor.util.NetworkListenThread;
import renor.util.ThreadRenorServer;
import renor.util.crashreport.CrashReport;
import renor.util.logger.ILogAgent;
import renor.util.profiler.Profiler;
import renor.util.throwable.ReportedException;

public abstract class RenorServer implements Runnable {
	protected Proxy serverProxy;
	public String currentTask;
	private String serverOwner;
	private String userMessage;
	private static RenorServer renorServer;
	public final Profiler theProfiler = new Profiler();
	private long timeOfLastWarning;
	private long lastSentPacketId;
	private long lastSentPacketSize;
	private long lastReceivedId;
	private long lastReceivedSize;
	public final long[] sentPacketCountArray = new long[100];
	public final long[] sentPacketSizeArray = new long[100];
	public final long[] receivedPacketCountArray = new long[100];
	public final long[] receivedPacketSizeArray = new long[100];
	public final long[] tickTimeArray = new long[100];
	private int tickCounter = 0;
	private boolean serverRunning = true;
	private boolean serverStopped = false;
	private boolean serverIsRunning;

	public RenorServer() {
		serverProxy = Proxy.NO_PROXY;
		renorServer = this;
	}

	protected abstract boolean startServer() throws IOException;

	public void startServerThread() {
		new ThreadRenorServer(this, "Server thread").start();
	}

	public void stopServer() {
	}

	public void initiateShutdown() {
		serverRunning = false;
	}

	protected void systemExitNow() {
	}

	protected void finalTick(CrashReport crashReport) {
	}

	public void run() {
		try {
			if (startServer()) {
				// currentTime
				long ct = System.currentTimeMillis();

				for (long i = 0; serverRunning; serverIsRunning = true) {
					// nowTime
					long nt = System.currentTimeMillis();
					// passedTime
					long pt = nt - ct;

					if (pt > 2000 && ct - timeOfLastWarning >= 15000) {
						getLogAgent().logWarning("Can\'t keep up! Did the system time change, or is the server overloaded?");
						pt = 2000;
						timeOfLastWarning = ct;
					}

					if (pt < 0) {
						getLogAgent().logWarning("Time ran backwards! Did the system time change?");
						pt = 0;
					}

					i += pt;
					ct = nt;

					while (i > 50) {
						i -= 50;
						tick();
					}

					Thread.sleep(1);
				}
			} else finalTick(null);
		} catch (Throwable e) {
			e.printStackTrace();
			getLogAgent().logSevereException("Encountered an unexpected exception " + e.getClass().getSimpleName(), e);
			CrashReport crashReport = null;
			crashReport = CrashReport.makeCrashReport(e, "Test!");

			if (e instanceof ReportedException) {
			} else {
			}

			File saveDir = new File(new File(getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");

			if (crashReport.saveToFile(saveDir, getLogAgent())) getLogAgent().logSevere("This crash report has been saved to: " + saveDir.getAbsolutePath());
			else getLogAgent().logSevere("We were unable to save this crash report to disk.");

			finalTick(crashReport);
		} finally {
			try {
				stopServer();
				serverStopped = true;
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				systemExitNow();
			}
		}
	}

	public void tick() {
		// nanoTime
		long nt = System.nanoTime();
		AxisAlignedBB.getAABBPool().cleanPool();
		++tickCounter;

		theProfiler.startSection("root");
		updateEntities();

		theProfiler.startSection("tallying");
		tickTimeArray[tickCounter % 100] = System.nanoTime() - nt;
		sentPacketCountArray[tickCounter % 100] = Packet.sentId - lastSentPacketId;
		lastSentPacketId = Packet.sentId;
		sentPacketSizeArray[tickCounter % 100] = Packet.sentSize - lastSentPacketSize;
		lastSentPacketSize = Packet.sentSize;
		receivedPacketCountArray[tickCounter % 100] = Packet.receivedId - lastReceivedId;
		lastReceivedId = Packet.receivedId;
		receivedPacketSizeArray[tickCounter % 100] = Packet.receivedSize - lastReceivedSize;
		lastReceivedSize = Packet.receivedSize;
		theProfiler.endSection();
		theProfiler.endSection();
	}

	public void updateEntities() {
		theProfiler.startSection("connection");
		getNetworkThread().networkTick();
		theProfiler.endSection();
	}

	protected void loadLevel() {
		setUserMessage("menu.loadingLevel");

		initialLevelChunkLoad();
	}

	protected void initialLevelChunkLoad() {
		setUserMessage("menu.generatingTerrain");
	}

	protected File getDataDirectory() {
		return new File(".");
	}

	public String getServerOwner() {
		return serverOwner;
	}

	public void setServerOwner(String username) {
		serverOwner = username;
	}

	public synchronized String getUserMessage() {
		return userMessage;
	}

	protected synchronized void setUserMessage(String message) {
		userMessage = message;
	}

	public abstract NetworkListenThread getNetworkThread();

	public abstract ILogAgent getLogAgent();

	public boolean serverIsInRunLoop() {
		return serverIsRunning;
	}
}
