package renor.gui;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import renor.Renor;
import renor.level.chunk.Chunk;
import renor.misc.ScaledResolution;
import renor.renderer.FontRenderer;
import renor.util.Direction;
import renor.util.EnumLightValue;
import renor.util.MathHelper;
import renor.util.OpenGLHelper;
import renor.util.Tessellator;

public class GuiIngame extends Gui {
	private final Random random = new Random();
	private final Renor renor;
	public float prevVignetteBrightness = 1.0f;

	public GuiIngame(Renor renor) {
		this.renor = renor;
	}

	public void renderGameOverlay(float partialTickTime, int mouseX, int mouseY) {
		ScaledResolution res = new ScaledResolution(renor.gameSettings, renor.displayWidth, renor.displayHeight);
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();
		// fontRenderer
		FontRenderer fr = renor.fontRenderer;
		renor.entityRenderer.setupOverlayRendering();
		GL11.glEnable(GL11.GL_BLEND);

		renderVignette(renor.thePlayer.getBrightness(), width, height);

		// do other stuff here

		if (renor.gameSettings.showDebugInfo) {
			renor.theProfiler.startSection("debug");
			GL11.glPushMatrix();
			fr.drawStringWithShadow("Renor Pre-Alpha. (" + renor.debug + ")", 2, 2, 16777215);
			fr.drawStringWithShadow(renor.getDebugRenderers(), 2, 12, 16777215);
			fr.drawStringWithShadow(renor.getDebugEntities(), 2, 22, 16777215);
			fr.drawStringWithShadow(renor.getDebugInfoEntities(), 2, 32, 16777215);
			fr.drawStringWithShadow(renor.getLevelProviderName(), 2, 42, 16777215);
			long maxMemory = Runtime.getRuntime().maxMemory();
			long totalMemory = Runtime.getRuntime().totalMemory();
			long freeMemory = Runtime.getRuntime().freeMemory();
			long usedMemory = totalMemory - freeMemory;
			String memory = "Used memory: " + usedMemory * 100 / maxMemory + "% (" + usedMemory / 1024 / 1024 + "MB) of " + maxMemory / 1024 / 1024 + "MB";
			drawString(fr, memory, width - fr.getStringWidth(memory) - 2, 2, 14737632);
			memory = "Allocated memory: " + totalMemory * 100 / maxMemory + "% (" + totalMemory / 1024 / 1024 + "MB)";
			drawString(fr, memory, width - fr.getStringWidth(memory) - 2, 12, 14737632);
			int x = MathHelper.floor_double(renor.thePlayer.posX);
			int y = MathHelper.floor_double(renor.thePlayer.posY);
			int z = MathHelper.floor_double(renor.thePlayer.posZ);
			drawString(fr, String.format("x: %.5f (%d) // c: %d (%d)", new Object[] { Double.valueOf(renor.thePlayer.posX), Integer.valueOf(x), Integer.valueOf(x >> 4), Integer.valueOf(x & 15) }), 2, 64, 14737632);
			drawString(fr, String.format("y: %.3f %.3f", new Object[] { Double.valueOf(renor.thePlayer.boundingBox.minY), Double.valueOf(renor.thePlayer.posY) }), 2, 72, 14737632);
			drawString(fr, String.format("z: %.5f (%d) // c: %d (%d)", new Object[] { Double.valueOf(renor.thePlayer.posZ), Integer.valueOf(z), Integer.valueOf(z >> 4), Integer.valueOf(z & 15) }), 2, 80, 14737632);
			int yaw = MathHelper.floor_double((double) (renor.thePlayer.rotationYaw * 4.0f / 360.0f) + 0.5) & 3;
			drawString(fr, "f: " + yaw + " (" + Direction.directions[yaw] + ") / " + MathHelper.wrapAngleTo180_float(renor.thePlayer.rotationYaw), 2, 88, 14737632);

			if (renor.theLevel != null && renor.theLevel.blockExists(x, y, z)) {
				Chunk c = renor.theLevel.getChunkFromBlockCoords(x, z);
				drawString(fr, "lc: " + (c.getTopFilledSegment() + 15) + " bl: " + c.getSavedLightValue(EnumLightValue.Block, x & 15, y, z & 15) + " sl: " + c.getSavedLightValue(EnumLightValue.Sky, x & 15, y, z & 15) + " rl: " + c.getBlockLightValue(x & 15, y, z & 15, 0), 2, 96, 14737632);
			}

			drawString(fr, String.format("ws: %.3f, fs: %.3f, g: %b", new Object[] { Float.valueOf(renor.thePlayer.capabilities.getWalkSpeed()), Float.valueOf(renor.thePlayer.capabilities.getFlySpeed()), Boolean.valueOf(renor.thePlayer.onGround) }), 2, 104, 14737632);
			GL11.glPopMatrix();
			renor.theProfiler.endSection();
		}
	}

	private void renderVignette(float brightness, int width, int height) {
		brightness = 1.0f - brightness;

		if (brightness < 0.0f) brightness = 0.0f;

		if (brightness > 1.0f) brightness = 1.0f;

		prevVignetteBrightness = (float) ((double) prevVignetteBrightness + (double) (brightness - prevVignetteBrightness) * 0.01);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGLHelper.glBlendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glColor4f(prevVignetteBrightness, prevVignetteBrightness, prevVignetteBrightness, 1.0f);
		renor.renderEngine.bindTexture("%blur%/vignette.png");
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(0.0, (double) height, -90.0, 0.0, 1.0);
		tess.addVertexWithUV((double) width, (double) height, -90.0, 1.0, 1.0);
		tess.addVertexWithUV((double) width, 0.0, -90.0, 1.0, 0.0);
		tess.addVertexWithUV(0.0, 0.0, -90.0, 0.0, 0.0);
		tess.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		OpenGLHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	}

	public void updateTick() {
	}
}
