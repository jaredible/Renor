package renor.renderer;

import renor.level.entity.Entity;
import renor.level.entity.EntityItem;

public class RenderItem extends RenderEntity {

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
		doRenderItem((EntityItem) entity, x, y, z, yaw, partialTickTime);
	}

	public void doRenderItem(EntityItem item, double x, double y, double z, float yaw, float partialTickTime) {
	}
}
