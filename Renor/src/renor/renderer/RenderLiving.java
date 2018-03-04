package renor.renderer;

import renor.level.entity.Entity;
import renor.level.entity.EntityLiving;

public class RenderLiving extends RenderEntity {

	public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
		doRenderLiving((EntityLiving) entity, x, y, z, yaw, partialTickTime);
	}

	public void doRenderLiving(EntityLiving living, double x, double y, double z, float yaw, float partialTickTime) {
		super.doRender(living, x, y, z, yaw, partialTickTime);
	}

	private float interpolateRotation(int par0, int par1, int par2) {
		float n;

		for (n = par1 - par0; n < -180.0f; n += 360.0f) {
			;
		}

		while (n >= 180.0f)
			n -= 360.0f;

		return par0 + par2 * n;
	}
}
