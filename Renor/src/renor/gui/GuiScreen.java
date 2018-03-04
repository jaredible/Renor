package renor.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import renor.Renor;
import renor.renderer.FontRenderer;

public class GuiScreen extends Gui {
	protected List<GuiButton> buttonList = new ArrayList<GuiButton>();
	protected Renor renor;
	protected FontRenderer fontRenderer;
	private GuiButton selectedButton;
	private long lastMouseEvent;
	public int width;
	public int height;
	private int eventButton;
	private int a;
	public boolean allowUserInput = false;

	public void setResolution(Renor renor, int width, int height) {
		this.renor = renor;
		this.width = width;
		this.height = height;

		fontRenderer = renor.fontRenderer;
		buttonList.clear();
		initGui();
	}

	public void initGui() {
	}

	public void updateScreen() {
	}

	public void drawScreen(int mouseX, int mouseY, float partialTickTime) {
		int i;

		for (i = 0; i < buttonList.size(); ++i)
			buttonList.get(i).drawButton(renor, mouseX, mouseY);

		// handle drawing labels here
	}

	protected void keyTyped(char c, int key) {
		if (key == 1) {
			renor.displayGuiScreen(null);
			renor.setIngameFocus();
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (button == 0) {
			for (int i = 0; i < buttonList.size(); ++i) {
				GuiButton b = buttonList.get(i);

				if (b.mousePressed(renor, mouseX, mouseY)) {
					selectedButton = b;
					// play sound here
					actionPerformed(b);
				}
			}
		}
	}

	protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
		if (selectedButton != null && button == 0) {
			selectedButton.mouseReleased(mouseX, mouseY);
			selectedButton = null;
		}
	}

	protected void mouseClickMove(int mouseX, int mouseY, int button, long mouseEventTime) {
	}

	protected void actionPerformed(GuiButton button) {
	}

	public void onGuiClosed() {
	}

	public boolean doesGuiPauseGame() {
		return true;
	}

	public void handleInput() {
		if (Mouse.isCreated()) {
			while (Mouse.next())
				handleMouseInput();
		}

		if (Keyboard.isCreated()) {
			while (Keyboard.next())
				handleKeyboardInput();
		}
	}

	public void handleMouseInput() {
		int mx = Mouse.getEventX() * width / renor.displayWidth;
		int my = height - Mouse.getEventY() * height / renor.displayHeight - 1;
		int b = Mouse.getEventButton();

		if (Renor.isRunningOnMac && b == 0 && (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157))) b = 1;

		if (Mouse.getEventButtonState()) {
			if (renor.gameSettings.touchscreen && a++ > 0) return;

			eventButton = b;
			lastMouseEvent = Renor.getSystemTime();
			mouseClicked(mx, my, eventButton);
		} else if (b != -1) {
			if (renor.gameSettings.touchscreen && --a > 0) return;

			eventButton = -1;
			mouseMovedOrUp(mx, my, b);
		} else if (eventButton != -1 && lastMouseEvent > 0) {
			long n = Renor.getSystemTime() - lastMouseEvent;
			mouseClickMove(mx, my, eventButton, n);
		}
	}

	public void handleKeyboardInput() {
		if (Keyboard.getEventKeyState()) {
			int key = Keyboard.getEventKey();
			char c = Keyboard.getEventCharacter();

			if (key == 87) {
				renor.toggleFullscreen();
				return;
			}

			keyTyped(c, key);
		}
	}

	public static boolean isCtrlKeyDown() {
		return Renor.isRunningOnMac ? Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA) : Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
	}

	public static boolean isShiftKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}
}
