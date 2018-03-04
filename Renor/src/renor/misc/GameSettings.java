package renor.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.lwjgl.input.Keyboard;

import renor.Renor;
import renor.util.KeyBinding;

public class GameSettings {
	private static final String[] GUISCALES = new String[] { "options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large" };
	private File optionsFile;
	private Renor renor;
	public KeyBinding keyBindForward = new KeyBinding("key.forward", Keyboard.KEY_W);
	public KeyBinding keyBindLeft = new KeyBinding("key.left", Keyboard.KEY_A);
	public KeyBinding keyBindBack = new KeyBinding("key.down", Keyboard.KEY_S);
	public KeyBinding keyBindRight = new KeyBinding("key.right", Keyboard.KEY_D);
	public KeyBinding keyBindJump = new KeyBinding("key.jump", Keyboard.KEY_SPACE);
	public KeyBinding keyBindSprint = new KeyBinding("key.sprint", Keyboard.KEY_LSHIFT);
	public KeyBinding keyBindSneak = new KeyBinding("key.sneak", Keyboard.KEY_LCONTROL);
	public KeyBinding keyBindDrop = new KeyBinding("key.drop", Keyboard.KEY_Q);
	public KeyBinding keyBindChat = new KeyBinding("key.chat", Keyboard.KEY_T);
	public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100);
	public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99);
	public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98);
	public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", Keyboard.KEY_F5);
	public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", Keyboard.KEY_Y);
	public KeyBinding[] keyBindings;
	public KeyBinding[] keyBindsHotBar;
	public int guiScale = 0;
	public int limitFramerate = 120;
	public int thirdPersonView = 0;
	public int renderDistanceChunks = -1;
	public int overrideWidth;
	public int overrideHeight;
	public float mouseSensitivity = 0.5f;
	public float fov = 0.0f;
	public float gamma = 0.0f;
	public float soundVolume = 1.0f;
	public float musicVolume = 1.0f;
	public boolean fullscreen = false;
	public boolean enableVsync = true;
	public boolean hideGUI = false;
	public boolean viewBobbing = true;
	public boolean ambientOcclusion = false;
	public boolean pauseOnLostFocus = false;
	public boolean invertMouse = false;
	public boolean smoothCamera = false;
	public boolean anaglyph = false;
	public boolean showDebugInfo = false;
	public boolean showDebugProfilerChart = false;
	public boolean touchscreen = false;

	public GameSettings(Renor renor, File dir) {
		this.renor = renor;
		optionsFile = new File(dir, "options.txt");

		keyBindsHotBar = new KeyBinding[] { new KeyBinding("key.hotbar.1", Keyboard.KEY_1), new KeyBinding("key.hotbar.2", Keyboard.KEY_2), new KeyBinding("key.hotbar.3", Keyboard.KEY_3) };
		keyBindings = new KeyBinding[] { keyBindForward, keyBindLeft, keyBindBack, keyBindRight, keyBindJump, keyBindSprint, keyBindSneak, keyBindDrop, keyBindChat, keyBindAttack, keyBindUseItem, keyBindPickBlock };
		renderDistanceChunks = renor.isJava64bit() ? 12 : 8;
		// TODO renderDistanceChunks test
		// test!
		renderDistanceChunks = 2;
		loadOptions();
	}

	public void loadOptions() {
		try {
			if (!optionsFile.exists()) return;

			BufferedReader reader = new BufferedReader(new FileReader(optionsFile));
			String line = "";

			while ((line = reader.readLine()) != null) {
				String[] split = line.split(":");

				try {
					for (int i = 0; i < keyBindings.length; ++i)
						if (split[0].equals("key_" + keyBindings[i].keyDescription)) keyBindings[i].keyCode = Integer.parseInt(split[1]);
				} catch (Exception e) {
					renor.getLogAgent().logWarning("Skipping bad option: " + line);
				}
			}

			KeyBinding.resetKeyBindingArrayAndHash();
			reader.close();
		} catch (Exception e) {
			renor.getLogAgent().logWarning("Failed to load options");
			e.printStackTrace();
		}
	}

	public void saveOptions() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(optionsFile));

			for (int i = 0; i < keyBindings.length; ++i)
				writer.println("key_" + keyBindings[i].keyDescription + ":" + keyBindings[i].keyCode);

			writer.close();
		} catch (Exception e) {
			renor.getLogAgent().logWarning("Failed to save options");
			e.printStackTrace();
		}
	}

	public static enum Options {
		FRAMERATE_LIMIT("FRAMERATE_LIMIT", 0, "options.framerateLimit", true, false, 10.0f, 260.0f, 10.0f);

		private final String enumString;
		private final float valueStep;
		private float valueMin;
		private float valueMax;
		private final boolean enumFloat;
		private final boolean enumBoolean;

		private Options(String name, int id, String enumString, boolean enumFloat, boolean enumBoolean, float valueMin, float valueMax, float valueStep) {
			this.enumString = enumString;
			this.enumFloat = enumFloat;
			this.enumBoolean = enumBoolean;
			this.valueMin = valueMin;
			this.valueMax = valueMax;
			this.valueStep = valueStep;
		}

		private Options(String name, int id, String enumString, boolean enumFloat, boolean enumBoolean) {
			this(name, id, enumString, enumFloat, enumBoolean, 0.0f, 1.0f, 0.0f);
		}

		public String getEnumString() {
			return enumString;
		}

		public float getValueMax() {
			return valueMax;
		}

		public boolean getEnumFloat() {
			return enumFloat;
		}

		public boolean getEnumBoolean() {
			return enumBoolean;
		}

		public int returnEnumOrdinal() {
			return ordinal();
		}
	}
}
