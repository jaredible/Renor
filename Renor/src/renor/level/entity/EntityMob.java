package renor.level.entity;

import renor.level.Level;

public abstract class EntityMob extends EntityLiving {
	public EntityMob(Level level) {
		super(level);
	}

	public void onUpdate() {
		super.onUpdate();
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
	}
}
