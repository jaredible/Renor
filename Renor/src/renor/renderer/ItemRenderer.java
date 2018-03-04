package renor.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import renor.Renor;
import renor.level.entity.EntityPlayer;
import renor.level.item.ItemStack;
import renor.util.OpenGLHelper;
import renor.util.RenderHelper;
import renor.util.Tessellator;
import renor.util.texture.Icon;

public class ItemRenderer {
	private Renor renor;
	private ItemStack itemToRender = null;
	private float equippedProgress = 0.0f;
	private float prevEquippedProgress = 0.0f;

	public ItemRenderer(Renor renor) {
		this.renor = renor;
	}

	public static void renderItemIn2D(Tessellator tessellator, float par0, float par1, float par2, float par3, float par4, float par5, float par6) {
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0f, 0.0f, 1.0f);
		tessellator.addVertexWithUV(0.0, 0.0, 0.0, par0, par3);
		tessellator.addVertexWithUV(1.0, 0.0, 0.0, par2, par3);
		tessellator.addVertexWithUV(1.0, 1.0, 0.0, par2, par1);
		tessellator.addVertexWithUV(0.0, 1.0, 0.0, par0, par1);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0xffff00);
		tessellator.setNormal(0.0f, 0.0f, -1.0f);
		tessellator.addVertexWithUV(0.0, 1.0, 0.0 - par6, par0, par1);
		tessellator.addVertexWithUV(1.0, 1.0, 0.0 - par6, par2, par1);
		tessellator.addVertexWithUV(1.0, 0.0, 0.0 - par6, par2, par3);
		tessellator.addVertexWithUV(0.0, 0.0, 0.0 - par6, par0, par3);
		tessellator.draw();
	}

	public void renderItemInFirstPerson(float partialTickTime) {
		float var0 = prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTickTime;
		EntityPlayer player = renor.thePlayer;
		GL11.glPushMatrix();
		GL11.glRotatef(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTickTime, 1.0f, 0.0f, 0.0f);
		GL11.glRotatef(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTickTime, 0.0f, 1.0f, 0.0f);
		RenderHelper.enableStandardLighting();
		GL11.glPopMatrix();

		float armYaw = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * partialTickTime;
		float armPitch = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * partialTickTime;
		GL11.glRotatef((player.rotationPitch - armPitch) * 0.1f, 1.0f, 0.0f, 0.0f);
		GL11.glRotatef((player.rotationYaw - armYaw) * 0.1f, 0.0f, 1.0f, 0.0f);

		ItemStack itemStack = itemToRender;
		int col = 0xf0f0;
		int u = col % 65536;
		int v = col / 65536;
		OpenGLHelper.setLightmapTextureCoords(OpenGLHelper.lightmapTexUnit, u / 1.0f, v / 1.0f);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		GL11.glPushMatrix();
		float var1 = 0.8f;
		GL11.glTranslatef(0.8f * var1, -0.75f * var1 - (1.0f - var0) * 0.6f, -0.9f * var1);
		renderItem();
		GL11.glPopMatrix();

		RenderHelper.disableStandardLighting();
	}

	private void renderItem() {
		GL11.glPushMatrix();
		renor.renderEngine.bindTexture("/textures/blocks/stoneBrick.png");
		Tessellator tess = Tessellator.instance;
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef(-1.2f, 1.0f, 1.2f);
		float n = 1.5f;
		GL11.glScalef(n, n, n);
		GL11.glRotatef(20.0f, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(355.0f, 0.0f, 0.0f, 1.0f);
		GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);//
		GL11.glTranslatef(-0.9375f, -0.0625f, 0.0f);
		GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);//
		renderItemIn2D(tess, 16.0f, 0.0f, 0.0f, 16.0f, 64.0f, 64.0f, 0.0625f);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	private void renderInsideOfBlock(Icon icon) {
		Tessellator tess = Tessellator.instance;
		float n = 0.1f;
		GL11.glColor4f(n, n, n, 0.5f);
		GL11.glPushMatrix();
		// render here
		GL11.glPopMatrix();
	}

	public void updateEquippedItem() {
		prevEquippedProgress = equippedProgress;
		// player
		EntityPlayer p = renor.thePlayer;
		ItemStack itemStack = null;
		boolean var0 = itemStack == itemToRender;

		if (itemToRender == null && itemStack == null) var0 = true;

		if (itemStack != null && itemToRender != null && itemStack != itemToRender && itemStack.itemId == itemToRender.itemId) {
			itemToRender = itemStack;
			var0 = true;
		}

		float var1 = 0.4f;
		float var2 = var0 ? 1.0f : 0.0f;
		float var3 = var2 - equippedProgress;

		if (var3 < -var1) var3 = -var1;
		if (var3 > var1) var3 = var1;

		equippedProgress += var3;

		if (equippedProgress < 0.1f) itemToRender = itemStack;
	}
}
