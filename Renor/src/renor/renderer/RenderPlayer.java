package renor.renderer;

import renor.level.entity.Entity;
import renor.level.entity.EntityPlayer;

public class RenderPlayer extends RenderLiving {

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
		doRenderPlayer((EntityPlayer) entity, x, y, z, yaw, partialTickTime);
	}

	public void doRenderPlayer(EntityPlayer player, double x, double y, double z, float yaw, float partialTickTime) {
		doRenderLiving(player, x, y, z, yaw, partialTickTime);
	}
}
