package renor.model;

import renor.level.entity.Entity;

public abstract class ModelBase {
	public int textureWidth = 64;
	public int textureHeight = 32;
	public float onGround;

	public void render(Entity entity, float par0, float par1, float par2, float par3, float par4, float par5) {
	}

	public void setRotationAngles(float par0, float par1, float par2, float par3, float par4, float par5, Entity entity) {
	}
}
