package renor.gui;

import org.lwjgl.opengl.GL11;

import renor.Renor;
import renor.renderer.FontRenderer;
import renor.util.OpenGLHelper;

public class GuiButton extends Gui {
	public String displayString;
	protected int width;
	protected int height;
	public int id;
	public int xPosition;
	public int yPosition;
	public boolean enabled;
	public boolean drawButton;
	protected boolean hovering;

	public GuiButton(int id, int x, int y, int width, int height, String displayString) {
		this.id = id;
		xPosition = x;
		yPosition = y;
		this.width = width;
		this.height = height;
		this.displayString = displayString;

		enabled = true;
		drawButton = true;
	}

	public GuiButton(int id, int x, int y, String displayString) {
		this(id, x, y, 200, 20, displayString);
	}

	protected int getHoverState(boolean flag) {
		byte n = 1;

		if (!enabled) n = 0;
		else if (flag) n = 2;

		return n;
	}

	public void drawButton(Renor renor, int mouseX, int mouseY) {
		if (drawButton) {
			// fontRenderer
			FontRenderer fr = renor.fontRenderer;
			renor.renderEngine.bindTexture("/textures/gui/widgets.png");
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			hovering = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
			int n = getHoverState(hovering);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGLHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(xPosition, yPosition, 0, 46 + n * 20, width / 2, height);
			drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 46 + n * 20, width / 2, height);
			mouseDragged(renor, mouseX, mouseY);
			int col = 14737632;

			if (!enabled) col = 10526880;
			else if (hovering) col = 16777120;

			drawCenteredString(fr, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, col);
		}
	}

	protected void mouseDragged(Renor renor, int mouseX, int mouseY) {
	}

	public void mouseReleased(int mouseX, int mouseY) {
	}

	public boolean mousePressed(Renor renor, int mouseX, int mouseY) {
		return enabled && drawButton && mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
	}
}
