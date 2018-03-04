package renor.renderer;

import static org.lwjgl.opengl.GL11.*;
import renor.aabb.AxisAlignedBB;
import renor.level.entity.Entity;
import renor.util.Tessellator;
import renor.util.texture.IconRegister;

public abstract class Render {
	protected RenderManager renderManager;
	private boolean a = false;

	public abstract void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime);

	public boolean a() {
		return a;
	}

	protected void bindTexture(String name) {
		renderManager.renderEngine.bindTexture(name);
	}

	public static void renderOffsetAABB(AxisAlignedBB axisAlignedBB, double x, double y, double z) {
		glDisable(GL_TEXTURE_2D);
		Tessellator tess = Tessellator.instance;
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		tess.startDrawingQuads();
		tess.setTranslation(x, y, z);
		tess.setNormal(0.0f, 0.0f, -1.0f);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.setNormal(0.0f, 0.0f, 1.0f);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.setNormal(0.0f, -1.0f, 0.0f);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.setNormal(0.0f, 1.0f, 0.0f);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.setNormal(-1.0f, 0.0f, 0.0f);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.setNormal(1.0f, 0.0f, 0.0f);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.setTranslation(0.0, 0.0, 0.0);
		tess.draw();
		glEnable(GL_TEXTURE_2D);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static void renderAABB(AxisAlignedBB axisAlignedBB) {
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.draw();
	}

	public void setRenderManager(RenderManager renderManager) {
		this.renderManager = renderManager;
	}

	public void updateIcons(IconRegister iconRegister) {
	}
}
