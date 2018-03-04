package renor.level.entity;

import renor.level.Level;

public class EntityFX extends Entity {
	public static double interpPosX;
	public static double interpPosY;
	public static double interpPosZ;

	public EntityFX(Level level) {
		super(level);
	}

	public void onUpdate() {
	}

	public void renderParticle(float partialTickTime) {
		float x = (float) (prevPosX + (posX - prevPosX) * (double) partialTickTime - interpPosX);
		float y = (float) (prevPosY + (posY - prevPosY) * (double) partialTickTime - interpPosY);
		float z = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTickTime - interpPosZ);
	}

	public int getFXLayer() {
		return 0;
	}
}
