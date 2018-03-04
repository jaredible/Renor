package renor.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import renor.level.entity.Entity;
import renor.level.entity.EntityFX;
import renor.misc.ActiveRenderInfo;

public class EffectRenderer {
	private Random random = new Random();
	private List<EntityFX>[] fxLayers = new List[4];
	private RenderEngine renderEngine;

	public EffectRenderer(RenderEngine renderEngine) {
		this.renderEngine = renderEngine;

		for (int i = 0; i < 4; ++i)
			fxLayers[i] = new ArrayList<EntityFX>();
	}

	public void updateEffects() {
		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < fxLayers[i].size(); ++j) {
				EntityFX fx = fxLayers[i].get(j);

				fx.onUpdate();

				if (fx.isDead) fxLayers[i].remove(j--);
			}
		}
	}

	public void addEffect(EntityFX fx) {
		int layer = fx.getFXLayer();

		if (fxLayers[layer].size() >= 8000) fxLayers[layer].remove(0);

		fxLayers[layer].add(fx);
	}

	public void renderParticles(Entity entity, float partialTickTime) {
		float rotationX = ActiveRenderInfo.rotationX;
		float rotationZ = ActiveRenderInfo.rotationZ;
		float rotationYZ = ActiveRenderInfo.rotationYZ;
		float rotationXY = ActiveRenderInfo.rotationXY;
		float rotationXZ = ActiveRenderInfo.rotationXZ;
		EntityFX.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTickTime;
		EntityFX.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTickTime;
		EntityFX.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTickTime;
	}

	public void renderLitParticles(Entity entity, float partialTickTime) {
		byte layer = 3;
	}

	public void clearEffects() {
		for (int i = 0; i < 4; ++i)
			fxLayers[i].clear();
	}
}
