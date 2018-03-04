package renor.gui;

import org.lwjgl.opengl.GL11;

import renor.renderer.FontRenderer;
import renor.util.OpenGLHelper;
import renor.util.Tessellator;

public class Gui {
	protected float zLevel;

	public static void drawRect(int minX, int minY, int maxX, int maxY, int color) {
		int n;

		if (minX < maxX) {
			n = minX;
			minX = maxX;
			maxX = n;
		}

		if (minY < maxY) {
			n = minY;
			minY = maxY;
			maxY = n;
		}

		float a = (float) (color >> 24 & 255) / 255.0f;
		float r = (float) (color >> 16 & 255) / 255.0f;
		float g = (float) (color >> 8 & 255) / 255.0f;
		float b = (float) (color & 255) / 255.0f;
		Tessellator tess = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGLHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(r, g, b, a);
		tess.startDrawingQuads();
		tess.addVertex((double) minX, (double) maxY, 0.0);
		tess.addVertex((double) maxX, (double) maxY, 0.0);
		tess.addVertex((double) maxX, (double) minY, 0.0);
		tess.addVertex((double) minX, (double) minY, 0.0);
		tess.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public void drawString(FontRenderer fontRenderer, String message, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(message, x, y, color);
	}

	public void drawCenteredString(FontRenderer fontRenderer, String message, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(message, x - fontRenderer.getStringWidth(message) / 2, y, color);
	}

	public void drawTexturedModalRect(int x, int y, int u, int v, int width, int height) {
		float w = 0.00390625f;
		float h = 0.00390625f;
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV((double) (x + 0), (double) (y + height), (double) zLevel, (double) ((float) (u + 0) * w), (double) ((float) (v + height) * h));
		tess.addVertexWithUV((double) (x + width), (double) (y + height), (double) zLevel, (double) ((float) (u + width) * w), (double) ((float) (v + height) * h));
		tess.addVertexWithUV((double) (x + width), (double) (y + 0), (double) zLevel, (double) ((float) (u + width) * w), (double) ((float) (v + 0) * h));
		tess.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) zLevel, (double) ((float) (u + 0) * w), (double) ((float) (v + 0) * h));
		tess.draw();
	}
}
