package renor.gui;

import org.lwjgl.input.Keyboard;

public class GuiMultiplayer extends GuiScreen {

	public void init() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
	}
}
