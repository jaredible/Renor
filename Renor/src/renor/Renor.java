package renor;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluErrorString;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import renor.aabb.AxisAlignedBB;
import renor.gui.GuiGameOver;
import renor.gui.GuiIngame;
import renor.gui.GuiIngameMenu;
import renor.gui.GuiMainMenu;
import renor.gui.GuiMemoryErrorScreen;
import renor.gui.GuiScreen;
import renor.init.Bootstrap;
import renor.level.LevelClient;
import renor.level.block.Block;
import renor.level.entity.Entity;
import renor.level.entity.EntityClientPlayerMP;
import renor.level.entity.EntityLiving;
import renor.level.entity.EntityPlayer;
import renor.level.entity.EntityZombie;
import renor.level.item.ItemStack;
import renor.misc.EnumChatFormatting;
import renor.misc.GLAllocation;
import renor.misc.GameSettings;
import renor.misc.MovingObjectPosition;
import renor.misc.ScaledResolution;
import renor.misc.StatCollector;
import renor.misc.Timer;
import renor.network.INetworkManager;
import renor.network.MemoryConnection;
import renor.network.NetClientHandler;
import renor.renderer.EffectRenderer;
import renor.renderer.EntityRenderer;
import renor.renderer.FontRenderer;
import renor.renderer.LevelRenderer;
import renor.renderer.RenderEngine;
import renor.renderer.RenderGlobal;
import renor.renderer.RenderManager;
import renor.sound.SoundManager;
import renor.util.EnumOSHelper;
import renor.util.KeyBinding;
import renor.util.MouseHelper;
import renor.util.MovementInputFromOptions;
import renor.util.OpenGLHelper;
import renor.util.PlayerControllerMP;
import renor.util.ReloadManager;
import renor.util.ScreenshotListener;
import renor.util.Session;
import renor.util.Tessellator;
import renor.util.ThreadDownloadResources;
import renor.util.ThreadShutdown;
import renor.util.Util;
import renor.util.crashreport.CrashReport;
import renor.util.logger.ILogAgent;
import renor.util.logger.LogAgent;
import renor.util.profiler.Profiler;
import renor.util.texture.TextureManager;
import renor.util.throwable.RenorError;
import renor.util.throwable.ReportedException;

public class Renor implements Runnable {
	/** a 10MiB preallocation to ensure the heap is reasonably sized */
	public static byte[] memoryReserve = new byte[10485760];
	private static File renorDir = null;
	public final File renorDataDir;
	public String debug;
	private final String launchedVersion;
	private final Proxy proxy;
	private static Renor theRenor;
	private IntegratedServer theIntegratedServer;
	private INetworkManager myNetworkManager;
	private final Session session;
	private CrashReport crashReporter;
	private final ILogAgent logAgent;
	public final Profiler theProfiler = new Profiler();
	private Timer timer = new Timer(20.0f);
	public GameSettings gameSettings;
	private ReloadManager reloadManager;
	public LevelClient theLevel;
	public PlayerControllerMP playerController = new PlayerControllerMP(this, null);
	public EntityPlayer thePlayer;
	public EntityClientPlayerMP thePlayerMP;
	public EntityLiving renderViewEntity;
	public Entity pointedEntity;
	public RenderEngine renderEngine;
	public RenderGlobal renderGlobal;
	public FontRenderer fontRenderer;
	public EntityRenderer entityRenderer;
	public EffectRenderer effectRenderer;
	public LoadingScreenRenderer loadingScreen;
	public GuiScreen currentScreen;
	public GuiIngame guiIngame;
	public MovingObjectPosition objectMouseOver = null;
	public MouseHelper mouseHelper;
	public SoundManager sndManager;
	private ThreadDownloadResources downloadResourcesThread;
	long debugUpdateTime = getSystemTime();
	long systemTime = getSystemTime();
	private long debugCrashTime;
	private static int debugFPS;
	public int displayWidth;
	public int displayHeight;
	private int tempDisplayWidth;
	private int tempDisplayHeight;
	int fpsCounter;
	private int leftClickCounter = 0;
	private int rightClickDelayCounter = 0;
	public static final boolean isRunningOnMac = Util.getOSType() == Util.EnumOS.MACOS;
	volatile boolean running = true;
	private boolean hasCrashed;
	private boolean isGamePaused;
	private boolean fullscreen;
	public boolean skipRenderLevel;
	public boolean inGameHasFocus;
	boolean isTakingScreenshot;
	private final boolean jvm64bit;
	private boolean integratedServerIsRunning;

	public Renor(Session session, int width, int height, boolean fullscreen, File dataDir, Proxy proxy, String version) {
		theRenor = this;
		logAgent = new LogAgent("Renor-Client", " [CLIENT]", new File(dataDir, "output-client.log").getAbsolutePath());
		this.session = session;
		displayWidth = width;
		displayHeight = height;
		this.fullscreen = fullscreen;
		renorDataDir = dataDir;
		this.proxy = proxy == null ? Proxy.NO_PROXY : proxy;
		launchedVersion = version;

		tempDisplayWidth = width;
		tempDisplayHeight = height;
		jvm64bit = isJvm64bit();
		startTimerHackThread();
		getLogAgent().logInfo("Setting user: " + session.getUsername());
		getLogAgent().logInfo("(Session Id is " + session.getSessionId() + ")");
		TextureManager.init();
		// ImageIO.setUseCache(false);
		Bootstrap.init();
	}

	private static boolean isJvm64bit() {
		String[] properties = new String[] { "sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch" };

		for (int i = 0; i < properties.length; ++i) {
			String p = properties[i];
			String systemProperty = System.getProperty(p);

			if (systemProperty != null && systemProperty.contains("64")) return true;
		}

		return false;
	}

	public static void a(String name) {
		try {
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Class<? extends Toolkit> toolkitClass = toolkit.getClass();

			if (toolkitClass.getName().equals("sun.awt.X11.XToolkit")) {
				Field field = toolkitClass.getDeclaredField("awtAppClassName");
				field.setAccessible(true);
				field.set(toolkit, name);
			}
		} catch (Exception e) {
			;
		}
	}

	private synchronized void startMainThread() {
		Thread mainThread = new Thread(this, "Renor main thread");
		mainThread.start();
	}

	private void startTimerHackThread() {
		Thread clientSleep = new Thread("Timer hack thread") {
			public void run() {
				while (running) {
					try {
						Thread.sleep(Integer.MAX_VALUE);
					} catch (InterruptedException e) {
						;
					}
				}
			}
		};
		clientSleep.setDaemon(true);
		clientSleep.start();
	}

	public void startGame() throws LWJGLException {
		gameSettings = new GameSettings(this, renorDataDir);

		if (gameSettings.overrideWidth > 0 && gameSettings.overrideHeight > 0) {
			displayWidth = gameSettings.overrideWidth;
			displayHeight = gameSettings.overrideHeight;
		}

		if (fullscreen) {
			Display.setFullscreen(true);
			displayWidth = Display.getDisplayMode().getWidth();
			displayHeight = Display.getDisplayMode().getHeight();

			if (displayWidth <= 0) displayWidth = 1;
			if (displayHeight <= 0) displayHeight = 1;
		} else Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));

		Display.setResizable(true);
		// version number = major.minor.revision
		Display.setTitle("Renor 0.0.5a");
		getLogAgent().logInfo("LWJGL Version: " + Sys.getVersion());
		Util.EnumOS os = Util.getOSType();

		if (os != Util.EnumOS.MACOS) {
			// try {
			// Display.setIcon(new ByteBuffer[] { readImage(new File(fileAssets,
			// "/icons/icon_16x16.png")), readImage(new File(fileAssets,
			// "/icons/icon_32x32.png")) });
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			if (os != Util.EnumOS.WINDOWS) a("Renor");
		}

		try {
			Display.create(new PixelFormat().withDepthBits(24));
		} catch (LWJGLException e) {
			e.printStackTrace();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				;
			}

			if (fullscreen) updateDisplayMode();

			Display.create();
		}

		OpenGLHelper.initializeTextures();
		gameSettings = new GameSettings(this, renorDataDir);
		reloadManager = new ReloadManager();
		renderEngine = new RenderEngine(gameSettings);
		fontRenderer = new FontRenderer(gameSettings, renderEngine, "/font.png", false);
		reloadManager.registerReloadListener(fontRenderer);
		entityRenderer = new EntityRenderer(this);
		effectRenderer = new EffectRenderer(renderEngine);
		loadingScreen = new LoadingScreenRenderer(this);
		guiIngame = new GuiIngame(this);
		mouseHelper = new MouseHelper();
		// sndManager = new SoundManager(gameSettings, fileAssets);
		// reloadManager.registerReloadListener(sndManager);

		loadScreen();
		checkGLError("Pre startup");
		glEnable(GL_TEXTURE_2D);
		glShadeModel(GL_SMOOTH);
		glClearDepth(1.0);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.1f);
		glCullFace(GL_BACK);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glMatrixMode(GL_MODELVIEW);
		checkGLError("Startup");
		renderGlobal = new RenderGlobal(this, renderEngine);
		renderEngine.refreshTextureMaps();
		glViewport(0, 0, displayWidth, displayHeight);
		checkGLError("Post startup");

		try {
			downloadResourcesThread = new ThreadDownloadResources(renorDataDir, this);
			downloadResourcesThread.start();
		} catch (Exception e) {
			;
		}

		// displayGuiScreen(new GuiMainMenu());

		// TODO server test
		boolean TRY_THE_BIG_SERVER_TEST = false;

		if (TRY_THE_BIG_SERVER_TEST) launchIntegratedServer();
		else {
			loadLevel(new LevelClient(null, theProfiler, getLogAgent()), EnumChatFormatting.GOLD + "Can you see this?");

			EntityZombie zombie;
			for (int i = 0; i < 64; ++i) {
				int x = i % 8;
				int z = i / 8;
				zombie = new EntityZombie(theLevel);
				zombie.setLocationAndAngles(-0.5 - (double) x, 60 + 40 + i * 2.0, -0.5 - (double) z, 0.0f, 0.0f);
				theLevel.spawnEntityInLevel(zombie);
			}

			loadingScreen.displayProgressMessage(StatCollector.translateToLocal("menu.loadingLevel"));
			int i = 0;

			while (i <= 10) {
				loadingScreen.setLoadingProgress(i * 10);

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					;
				}

				++i;
			}

			setIngameFocus();
		}

		if (gameSettings.fullscreen && !fullscreen) toggleFullscreen();

		Display.setVSyncEnabled(gameSettings.enableVsync);
	}

	private void loadScreen() {
		ScaledResolution res = new ScaledResolution(gameSettings, displayWidth, displayHeight);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0.0, res.getScaledWidth_double(), res.getScaledHeight_double(), 0.0, 1000.0, 3000.0);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glTranslatef(0.0f, 0.0f, -2000.0f);
		glViewport(0, 0, displayWidth, displayHeight);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glDisable(GL_FOG);
		glDisable(GL_LIGHTING);
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		renderEngine.bindTexture("/title.png");
		tess.startDrawingQuads();
		tess.setColorOpaque_I(16777215);
		tess.addVertexWithUV(0.0, (double) displayHeight, 0.0, 0.0, 0.0);
		tess.addVertexWithUV((double) displayWidth, (double) displayHeight, 0.0, 0.0, 0.0);
		tess.addVertexWithUV((double) displayWidth, 0.0, 0.0, 0.0, 0.0);
		tess.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 0.0);
		tess.draw();
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		tess.setColorOpaque_I(16777215);
		short width = 256;
		short height = 256;
		scaledTessellator((res.getScaledWidth() - width) / 2, (res.getScaledHeight() - height) / 2, 0, 0, width, height);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.1f);
		glFlush();
		updateDisplay();
	}

	public void scaledTessellator(int x, int y, int u, int v, int width, int height) {
		float w = 0.00390625f;
		float h = 0.00390625f;
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV((double) (x + 0), (double) (y + height), 0.0, (double) ((float) (u + 0) * w), (double) ((float) (v + height) * h));
		tess.addVertexWithUV((double) (x + width), (double) (y + height), 0.0, (double) ((float) (u + width) * w), (double) ((float) (v + height) * h));
		tess.addVertexWithUV((double) (x + width), (double) (y + 0), 0.0, (double) ((float) (u + width) * w), (double) ((float) (v + 0) * h));
		tess.addVertexWithUV((double) (x + 0), (double) (y + 0), 0.0, (double) ((float) (u + 0) * w), (double) ((float) (v + 0) * h));
		tess.draw();
	}

	private ByteBuffer readImage(File imageFile) throws IOException {
		BufferedImage image = ImageIO.read(imageFile);
		int[] data = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);

		for (int i = 0; i < data.length; ++i) {
			int n = data[i];
			buffer.putInt(n << 8 | n >> 24 & 255);
		}

		buffer.flip();
		return buffer;
	}

	private void checkGLError(String string) {
		int n = glGetError();

		if (n != 0) {
			String error = gluErrorString(n);
			getLogAgent().logSevere("########## GL ERROR ##########");
			getLogAgent().logSevere("@ " + string);
			getLogAgent().logSevere(n + ": " + error);
		}
	}

	public void crash(CrashReport crashReport) {
		crashReporter = crashReport;
		hasCrashed = true;
	}

	public void displayCrashReport(CrashReport crashReport) {
		File saveDir = new File(getRenor().renorDataDir, "crash-reports");
		File saveFile = new File(saveDir, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");
		System.err.println(crashReport.getCompleteReport());

		if (crashReport.getFile() != null) {
			System.err.println("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getFile());
			System.exit(-1);
		} else if (crashReport.saveToFile(saveFile, getLogAgent())) {
			System.err.println("#@!@# Game crashed! Crash report saved to: #@!@# " + saveFile.getAbsolutePath());
			System.exit(-1);
		} else {
			System.err.println("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			System.exit(-2);
		}
	}

	public void displayGuiScreen(GuiScreen guiScreen) {
		if (currentScreen != null) currentScreen.onGuiClosed();

		if (guiScreen == null && theLevel == null) guiScreen = new GuiMainMenu();
		else if (guiScreen == null && thePlayer.getHealth() <= 0.0f) guiScreen = new GuiGameOver();

		if (guiScreen instanceof GuiMainMenu) {
			gameSettings.showDebugInfo = false;
			// do other stuff here
		}

		currentScreen = guiScreen;

		if (guiScreen != null) {
			setIngameNotInFocus();
			ScaledResolution res = new ScaledResolution(gameSettings, displayWidth, displayHeight);
			int w = res.getScaledWidth();
			int h = res.getScaledHeight();
			guiScreen.setResolution(this, w, h);
			skipRenderLevel = false;
		} else {
			// handle sound
			setIngameFocus();
		}
	}

	public void displayIngameMenu() {
		if (currentScreen == null) {
			displayGuiScreen(new GuiIngameMenu());
			// sndManager.pauseAllSounds();
		}
	}

	public void displayDebugInfo(long nanoTime) {
		if (theProfiler.profilingEnabled) {
			// do other in stuff here
		}
	}

	public void launchIntegratedServer() {
		loadLevel(null);
		System.gc();

		try {
			theIntegratedServer = new IntegratedServer(this);
			theIntegratedServer.startServerThread();
			integratedServerIsRunning = true;
		} catch (Throwable e) {
			CrashReport crashReport = CrashReport.makeCrashReport(e, "Starting integrated server");
			throw new ReportedException(crashReport);
		}

		loadingScreen.displayProgressMessage(StatCollector.translateToLocal("menu.loadingLevel"));

		while (!theIntegratedServer.serverIsInRunLoop()) {
			// message
			String msg = theIntegratedServer.getUserMessage();

			if (msg != null) loadingScreen.resetProgressAndWorkingMessage(StatCollector.translateToLocal(msg));
			else loadingScreen.resetProgressAndWorkingMessage("");

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				;
			}
		}

		displayGuiScreen(null);

		try {
			NetClientHandler netClientHandler = new NetClientHandler(this, theIntegratedServer);
			myNetworkManager = netClientHandler.getNetManager();
		} catch (IOException e) {
			// do stuff in here
		}
	}

	public void loadLevel(LevelClient levelClient, String message) {
		if (levelClient == null) {
			NetClientHandler netClientHandler = getNetHandler();

			if (netClientHandler != null) netClientHandler.cleanup();

			if (myNetworkManager != null) myNetworkManager.closeConnections();

			if (theIntegratedServer != null) theIntegratedServer.initiateShutdown();

			theIntegratedServer = null;
		}

		renderViewEntity = null;
		myNetworkManager = null;

		if (loadingScreen != null) {
			loadingScreen.resetProgressAndMessage(message);
			loadingScreen.resetProgressAndWorkingMessage("");
		}

		if (levelClient == null && theLevel != null) integratedServerIsRunning = false;

		// TODO stop all sounds
		// sndManager.playStreaming(null, 0.0f, 0.0f, 0.0f);
		// sndManager.stopAllSounds();
		theLevel = levelClient;

		if (levelClient != null) {
			if (renderGlobal != null) renderGlobal.setLevelAndLoadRenderers(levelClient);

			if (effectRenderer != null) effectRenderer.clearEffects();

			if (thePlayer == null) {
				thePlayer = playerController.createNewPlayer(levelClient);
				playerController.flipPlayer(thePlayer);
			}

			thePlayer.preparePlayerToSpawn();
			thePlayer.movementInput = new MovementInputFromOptions(gameSettings);
			levelClient.spawnEntityInLevel(thePlayer);
			renderViewEntity = thePlayer;
		} else thePlayer = null;

		System.gc();
		systemTime = 0;
	}

	public void loadLevel(LevelClient levelClient) {
		loadLevel(levelClient, "");
	}

	public void setIngameFocus() {
		if (Display.isActive()) {
			if (!inGameHasFocus) {
				inGameHasFocus = true;
				mouseHelper.grabMouseCursor();
				displayGuiScreen(null);
				leftClickCounter = 10000;
			}
		}
	}

	public void setIngameNotInFocus() {
		if (inGameHasFocus) {
			inGameHasFocus = false;
			mouseHelper.ungrabMouseCursor();
		}
	}

	public void shutdown() {
		running = false;
	}

	public void shutdownRenor() {
		try {
			try {
				if (downloadResourcesThread != null) downloadResourcesThread.closeRenor();
			} catch (Exception e) {
				;
			}

			getLogAgent().logInfo("Stopping!");

			try {
				loadLevel(null);
			} catch (Throwable e) {
				;
			}

			try {
				GLAllocation.deleteTexturesAndDisplayLists();
			} catch (Throwable e) {
				;
			}

			// sndManager.cleanup();
		} finally {
			Display.destroy();

			if (!hasCrashed) System.exit(0);
		}

		System.gc();
	}

	private int getLimitFramerate() {
		return theLevel == null && currentScreen != null ? 30 : gameSettings.limitFramerate;
	}

	public boolean isFrameRateLimitBelowMax() {
		return (float) getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
	}

	private void freeMemory() {
		try {
			memoryReserve = new byte[0];
			renderGlobal.deleteAllDisplayLists();
		} catch (Throwable e) {
			;
		}

		try {
			System.gc();
			AxisAlignedBB.getAABBPool().clearPool();
			theLevel.getLevelVec3Pool().clearAndFreeCache();
		} catch (Throwable e) {
			;
		}

		try {
			System.gc();
			loadLevel(null);
		} catch (Throwable e) {
			;
		}

		System.gc();
	}

	public void run() {
		running = true;
		// crashReport
		CrashReport cr;

		try {
			startGame();
		} catch (Throwable e) {
			cr = CrashReport.makeCrashReport(e, "Initializing game");
			displayCrashReport(cr);
			return;
		}

		while (true) {
			try {
				while (running) {
					if (!hasCrashed || crashReporter == null) {
						try {
							runGameLoop();
						} catch (OutOfMemoryError e) {
							freeMemory();
							displayGuiScreen(new GuiMemoryErrorScreen());
							System.gc();
						}

						continue;
					}

					displayCrashReport(crashReporter);
				}
			} catch (RenorError e) {
				break;
			} catch (ReportedException e) {
				freeMemory();
				e.printStackTrace();
				displayCrashReport(e.getCrashReport());
				break;
			} catch (Throwable e) {
				cr = new CrashReport("Unexpected error", e);
				freeMemory();
				e.printStackTrace();
				displayCrashReport(cr);
				break;
			} finally {
				shutdownRenor();
			}

			return;
		}
	}

	private void runGameLoop() {
		AxisAlignedBB.getAABBPool().cleanPool();

		if (theLevel != null) theLevel.getLevelVec3Pool().clear();

		theProfiler.startSection("root");

		if (Display.isCreated() && Display.isCloseRequested()) shutdown();

		if (isGamePaused && theLevel != null) {
			float n = timer.renderPartialTicks;
			timer.updateTimer();
			timer.renderPartialTicks = n;
		} else timer.updateTimer();

		// nanoTime
		long nt = System.nanoTime();
		theProfiler.startSection("tick");

		for (int i = 0; i < timer.elapsedTicks; ++i)
			runTick();

		theProfiler.endStartSection("preRenderErrors");
		// passedNanoTime
		long n = System.nanoTime() - nt;
		checkGLError("Pre render");
		theProfiler.endStartSection("sound");
		// sndManager.setListener(thePlayer, timer.renderPartialTicks);
		theProfiler.endStartSection("render");
		glPushMatrix();
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		theProfiler.startSection("display");
		glEnable(GL_TEXTURE_2D);

		if (thePlayer != null && thePlayer.isEntityInsideOpaqueBlock()) gameSettings.thirdPersonView = 0;

		theProfiler.endSection();

		if (!skipRenderLevel) {
			theProfiler.endStartSection("gameRenderer");
			entityRenderer.updateCameraAndRender(timer.renderPartialTicks);
			theProfiler.endSection();
		}

		glFlush();
		// TODO critical bug here, fix!
		// theProfiler.endSection();

		if (!Display.isActive() && fullscreen) toggleFullscreen();

		if (gameSettings.showDebugInfo && gameSettings.showDebugProfilerChart) {
			if (!theProfiler.profilingEnabled) theProfiler.clearProfiling();

			theProfiler.profilingEnabled = true;
			displayDebugInfo(n);
		} else theProfiler.profilingEnabled = false;

		glPopMatrix();
		theProfiler.startSection("root");
		updateDisplay();
		Thread.yield();
		screenshotListener();
		checkGLError("Post render");
		++fpsCounter;
		// paused
		boolean p = isGamePaused;
		isGamePaused = isSingleplayer() && currentScreen != null && currentScreen.doesGuiPauseGame() && !theIntegratedServer.isPublic();

		if (isIntegratedServerRunning() && thePlayerMP != null && thePlayerMP.sendQueue != null && isGamePaused != p) ((MemoryConnection) thePlayerMP.sendQueue.getNetManager()).setGamePaused(isGamePaused);

		while (getSystemTime() >= debugUpdateTime + 1000) {
			debugFPS = fpsCounter;
			debug = debugFPS + " fps, " + LevelRenderer.chunksUpdated + " chunk updates";
			LevelRenderer.chunksUpdated = 0;
			debugUpdateTime += 1000;
			fpsCounter = 0;
		}

		theProfiler.endSection();

		if (isFrameRateLimitBelowMax()) Display.sync(getLimitFramerate());
	}

	private void runTick() {
		if (rightClickDelayCounter > 0) --rightClickDelayCounter;

		theProfiler.startSection("gui");

		if (!isGamePaused) guiIngame.updateTick();

		theProfiler.endStartSection("pick");
		entityRenderer.getMouseOver(1.0f);
		theProfiler.endStartSection("gameMode");

		// if (!isGamePaused && theLevel != null)
		// playerController.updateController();

		if (currentScreen != null) leftClickCounter = 10000;

		// crashReport
		CrashReport cr;

		if (currentScreen != null) {
			try {
				currentScreen.handleInput();
			} catch (Throwable e) {
				cr = CrashReport.makeCrashReport(e, "Updating screen events");
				throw new ReportedException(cr);
			}

			if (currentScreen != null) {
				try {
					currentScreen.updateScreen();
				} catch (Throwable e) {
					cr = CrashReport.makeCrashReport(e, "Ticking screen");
					throw new ReportedException(cr);
				}
			}
		}

		if (currentScreen == null || currentScreen.allowUserInput) {
			theProfiler.endStartSection("mouse");
			int n;

			while (Mouse.next()) {
				n = Mouse.getEventButton();

				if (isRunningOnMac && n == 0 && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) n = 1;

				KeyBinding.setKeyBindState(n - 100, Mouse.getEventButtonState());

				if (Mouse.getEventButtonState()) KeyBinding.onTick(n - 100);

				// passedTime
				long pt = getSystemTime() - systemTime;

				if (pt <= 200) {
					// scroll
					int s = Mouse.getEventDWheel();

					if (s != 0) thePlayer.inventory.changeCurrentItem(s);

					if (currentScreen == null) {
						if (!inGameHasFocus && Mouse.getEventButtonState()) setIngameFocus();
					} else if (currentScreen != null) currentScreen.handleMouseInput();
				}
			}

			if (leftClickCounter > 0) --leftClickCounter;

			theProfiler.endStartSection("keyboard");
			boolean v0;

			while (Keyboard.next()) {
				KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());

				if (Keyboard.getEventKeyState()) KeyBinding.onTick(Keyboard.getEventKey());

				if (debugCrashTime > 0) {
					if (getSystemTime() - debugCrashTime >= 6000) throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));

					if (!Keyboard.isKeyDown(Keyboard.KEY_C) || !Keyboard.isKeyDown(Keyboard.KEY_F3)) debugCrashTime = -1;
				} else if (Keyboard.isKeyDown(Keyboard.KEY_C) && Keyboard.isKeyDown(Keyboard.KEY_F3)) debugCrashTime = getSystemTime();

				if (Keyboard.getEventKeyState()) {
					if (Keyboard.getEventKey() == Keyboard.KEY_F11) toggleFullscreen();
					else {
						if (currentScreen != null) currentScreen.handleKeyboardInput();
						else {
							// if (Keyboard.getEventKey() ==
							// Keyboard.KEY_ESCAPE) displayIngameMenu();
							// TODO test
							if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE && inGameHasFocus) setIngameNotInFocus();

							if (Keyboard.getEventKey() == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) forceReload();

							if (Keyboard.getEventKey() == Keyboard.KEY_F4) {
								// TODO render distance test
								--gameSettings.renderDistanceChunks;

								if (gameSettings.renderDistanceChunks < 2) gameSettings.renderDistanceChunks = 4;
							}

							if (Keyboard.getEventKey() == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_F3)) renderGlobal.loadRenderers();

							if (Keyboard.getEventKey() == Keyboard.KEY_B && Keyboard.isKeyDown(Keyboard.KEY_F3)) RenderManager.a = !RenderManager.a;

							if (Keyboard.getEventKey() == Keyboard.KEY_F1) gameSettings.hideGUI = !gameSettings.hideGUI;

							if (Keyboard.getEventKey() == Keyboard.KEY_F3) {
								gameSettings.showDebugInfo = !gameSettings.showDebugInfo;
								gameSettings.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
							}

							if (gameSettings.keyBindTogglePerspective.isPressed()) {
								++gameSettings.thirdPersonView;

								if (gameSettings.thirdPersonView > 2) gameSettings.thirdPersonView = 0;
							}

							if (gameSettings.keyBindSmoothCamera.isPressed()) gameSettings.smoothCamera = !gameSettings.smoothCamera;

							if (objectMouseOver != null && Keyboard.getEventKey() == Keyboard.KEY_Q) {
								int x = objectMouseOver.blockX;
								int y = objectMouseOver.blockY;
								int z = objectMouseOver.blockZ;
								EntityZombie zombie = new EntityZombie(theLevel);
								zombie.setLocationAndAngles(x + 0.5, y + 2, z + 0.5, 0.0f, 0.0f);
								theLevel.spawnEntityInLevel(zombie);
							}

							if (Keyboard.getEventKey() == Keyboard.KEY_N) {
								if (theLevel != null) theLevel.setLevelTime(20000);
							}
							
							if (Keyboard.getEventKey() == Keyboard.KEY_M) {
								if (theLevel != null) theLevel.setLevelTime(0);
							}
						}
					}
				}
			}

			for (int i = 0; i < 3; ++i)
				if (gameSettings.keyBindsHotBar[i].isPressed()) thePlayer.inventory.currentItem = i;

			if (thePlayer.isUsingItem()) {
				if (!gameSettings.keyBindUseItem.getIsKeyPressed()) playerController.onStoppedUsingItem(thePlayer);

				label:

				while (true) {
					if (!gameSettings.keyBindAttack.isPressed()) {
						while (gameSettings.keyBindUseItem.isPressed()) {
							;
						}

						while (true) {
							if (gameSettings.keyBindPickBlock.isPressed()) continue;

							break label;
						}
					}
				}
			} else {
				while (gameSettings.keyBindAttack.isPressed())
					clickMouse(0);

				while (gameSettings.keyBindUseItem.isPressed())
					clickMouse(1);

				while (gameSettings.keyBindPickBlock.isPressed())
					clickMiddleMouseButton();
			}

			if (gameSettings.keyBindUseItem.getIsKeyPressed() && rightClickDelayCounter == 0 && !thePlayer.isUsingItem()) clickMouse(1);

			sendClickBlockToController(currentScreen == null && gameSettings.keyBindAttack.getIsKeyPressed() && inGameHasFocus);
		}

		if (theLevel != null) {
			theProfiler.endStartSection("gameRenderer");

			if (!isGamePaused) entityRenderer.updateRenderer();

			theProfiler.endStartSection("level");

			if (!isGamePaused) theLevel.updateEntities();

			if (!isGamePaused) {
				try {
					theLevel.tick();
				} catch (Throwable e) {
					cr = CrashReport.makeCrashReport(e, "Exception in level tick");

					if (theLevel == null) {
					} else {
					}

					throw new ReportedException(cr);
				}
			}

			theProfiler.endStartSection("particles");

			if (!isGamePaused) effectRenderer.updateEffects();
		} else if (myNetworkManager != null) {
			theProfiler.endStartSection("pendingConnection");
			// process received packets
		}

		theProfiler.endSection();
		systemTime = getSystemTime();
	}

	private void clickMouse(int n) {
		if (n != 0 || leftClickCounter <= 0) {
			if (n == 0) thePlayer.swingItem();

			if (n == 1) rightClickDelayCounter = 4;

			ItemStack currentItem = thePlayer.inventory.getCurrentItem();

			if (objectMouseOver == null) {
			} else if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
				if (n == 0) playerController.attackEntity(thePlayer, objectMouseOver.entityHit);
			} else if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				int x = objectMouseOver.blockX;
				int y = objectMouseOver.blockY;
				int z = objectMouseOver.blockZ;
				int side = objectMouseOver.sideHit;

				if (n == 0) playerController.clickBlock(x, y, z, objectMouseOver.sideHit);
				else {
					if (playerController.onPlayerRightClick(thePlayer, theLevel, currentItem, x, y, z, side, objectMouseOver.hitVec)) thePlayer.swingItem();

					if (currentItem == null) return;

					if (currentItem.stackSize == 0) thePlayer.inventory.mainInventory[thePlayer.inventory.currentItem] = null;
				}
			}
		}
	}

	private void clickMiddleMouseButton() {
		if (objectMouseOver != null) {
			int id;
			int n;

			if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				n = objectMouseOver.blockX;
				int y = objectMouseOver.blockY;
				int z = objectMouseOver.blockZ;
				// block
				Block b = Block.blocksList[theLevel.getBlockId(n, y, z)];

				if (b == null) return;

				id = b.idPicked(theLevel, n, y, z);

				if (id == 0) return;
			} else {
				if (objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || objectMouseOver.entityHit == null) return;

				// test
				id = 1;
			}

			thePlayer.inventory.setCurrentItem(id);

			int slot = thePlayer.inventoryContainer.inventoryItemStacks.size() - 9 + thePlayer.inventory.currentItem;
			playerController.sendSlotPacket(thePlayer.inventory.getStackInSlot(thePlayer.inventory.currentItem), slot);
		}
	}

	private void sendClickBlockToController(boolean flag) {
		if (!flag) leftClickCounter = 0;

		if (leftClickCounter <= 0) {
			if (flag && objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				int x = objectMouseOver.blockX;
				int y = objectMouseOver.blockY;
				int z = objectMouseOver.blockZ;
				playerController.onPlayerDamageBlock(x, y, z, objectMouseOver.sideHit);

				if (thePlayer.canDamageBlock(x, y, z)) thePlayer.swingItem();

				// do other stuff here
			}
		}
	}

	private void screenshotListener() {
		if (Keyboard.isKeyDown(Keyboard.KEY_F2)) {
			if (!isTakingScreenshot) {
				isTakingScreenshot = true;
				getLogAgent().logInfo("[CHAT] " + ScreenshotListener.saveScreenshot(renorDataDir, displayWidth, displayHeight));
			}
		} else isTakingScreenshot = false;
	}

	public void installResource(String name, File file) {
	}

	public void forceReload() {
		reloadManager.reloadResources();
		downloadResourcesThread.reloadResources();
	}

	public void toggleFullscreen() {
		try {
			fullscreen = !fullscreen;

			if (fullscreen) {
				updateDisplayMode();
				displayWidth = Display.getDisplayMode().getWidth();
				displayHeight = Display.getDisplayMode().getHeight();

				if (displayWidth <= 0) displayWidth = 1;
				if (displayHeight <= 0) displayHeight = 1;
			} else {
				Display.setDisplayMode(new DisplayMode(tempDisplayWidth, tempDisplayHeight));
				displayWidth = tempDisplayWidth;
				displayHeight = tempDisplayHeight;

				if (displayWidth <= 0) displayWidth = 1;
				if (displayHeight <= 0) displayHeight = 1;
			}

			if (currentScreen != null) resize(displayWidth, displayHeight);

			Display.setFullscreen(fullscreen);
			Display.setVSyncEnabled(gameSettings.enableVsync);
			updateDisplay();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void resize(int width, int height) {
		displayWidth = width <= 0 ? 1 : width;
		displayHeight = height <= 0 ? 1 : height;

		if (currentScreen != null) {
			ScaledResolution res = new ScaledResolution(gameSettings, displayWidth, displayHeight);
			int w = res.getScaledWidth();
			int h = res.getScaledHeight();
			currentScreen.setResolution(this, w, h);
		}

		loadingScreen = new LoadingScreenRenderer(this);
	}

	private void updateDisplayMode() throws LWJGLException {
		// displayMode
		DisplayMode dm = Display.getDesktopDisplayMode();
		Display.setDisplayMode(dm);
		displayWidth = dm.getWidth();
		displayHeight = dm.getHeight();
	}

	public void updateDisplay() {
		Display.update();

		if (!fullscreen && Display.wasResized()) {
			int w = displayWidth;
			int h = displayHeight;
			displayWidth = Display.getWidth();
			displayHeight = Display.getHeight();

			if (displayWidth != w || displayHeight != h) {
				if (displayWidth <= 0) displayWidth = 1;
				if (displayHeight <= 0) displayHeight = 1;

				resize(displayWidth, displayHeight);
			}
		}
	}

	public static File getAppDir(String filename) {
		String userhome = System.getProperty("user.home", ".");
		File dir;

		switch (EnumOSHelper.osIds[Util.getOSType().ordinal()]) {
		case 1:
		case 2:
			dir = new File(userhome, '.' + filename + '/');
			break;
		case 3:
			String env = System.getenv("APPDATA");
			if (env != null) dir = new File(env, "." + filename + '/');
			else dir = new File(userhome, '.' + filename + '/');
			break;
		case 4:
			dir = new File(userhome, "Library/Application Support/" + filename);
			break;
		default:
			dir = new File(userhome, filename + '/');
		}

		if (!dir.exists() && !dir.mkdirs()) throw new RuntimeException("The working directory could not be created: " + dir);

		return dir;
	}

	public static File getRenorDir() {
		if (renorDir == null) renorDir = getAppDir("renor");

		return renorDir;
	}

	public static void main(String[] args) {
		String sessionName = "Syn";
		String sessionId = "0";
		int width = 854;
		int height = 480;
		boolean fullscreen = false;
		File dataDir = getRenorDir();
		Proxy proxy = Proxy.NO_PROXY;
		String launchedVersion = "pre-alpha";

		Renor renor = new Renor(new Session(sessionName, sessionId), width, height, fullscreen, dataDir, proxy, launchedVersion);
		renor.startMainThread();

		Runtime.getRuntime().addShutdownHook(new ThreadShutdown());
	}

	public boolean isSingleplayer() {
		return integratedServerIsRunning && theIntegratedServer != null;
	}

	public boolean isIntegratedServerRunning() {
		return integratedServerIsRunning;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public ILogAgent getLogAgent() {
		return logAgent;
	}

	public String getDebugRenderers() {
		return renderGlobal.getDebugRenderers();
	}

	public String getDebugEntities() {
		return renderGlobal.getDebugEntities();
	}

	public String getDebugInfoEntities() {
		return "T: " + theLevel.getDebugLoadedEntities();
	}

	public String getLevelProviderName() {
		return theLevel.getProviderName();
	}

	public Session getSession() {
		return session;
	}

	public IntegratedServer getIntegratedServer() {
		return theIntegratedServer;
	}

	public boolean isJava64bit() {
		return jvm64bit;
	}

	public NetClientHandler getNetHandler() {
		return thePlayerMP != null ? thePlayerMP.sendQueue : null;
	}

	public static boolean isAmbientOcclusionEnabled() {
		return theRenor != null && theRenor.gameSettings.ambientOcclusion;
	}

	public static long getSystemTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}

	public static void stopIntegratedServer() {
		if (theRenor != null) {
			IntegratedServer integratedServer = theRenor.getIntegratedServer();

			if (integratedServer != null) integratedServer.stopServer();
		}
	}

	public static Renor getRenor() {
		return theRenor;
	}
}
