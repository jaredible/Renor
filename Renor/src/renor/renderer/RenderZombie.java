package renor.renderer;

import renor.level.entity.Entity;
import renor.level.entity.EntityZombie;

public class RenderZombie extends RenderLiving {

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
		doRenderZombie((EntityZombie) entity, x, y, z, yaw, partialTickTime);
	}

	public void doRenderZombie(EntityZombie zombie, double x, double y, double z, float yaw, float partialTickTime) {
		doRenderLiving(zombie, x, y, z, yaw, partialTickTime);
	}
}
