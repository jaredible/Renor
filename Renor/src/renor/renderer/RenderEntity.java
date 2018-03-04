package renor.renderer;

import static org.lwjgl.opengl.GL11.*;
import renor.level.entity.Entity;

public class RenderEntity extends Render {

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
		glPushMatrix();
		renderOffsetAABB(entity.boundingBox, x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
		glPopMatrix();
	}
}
