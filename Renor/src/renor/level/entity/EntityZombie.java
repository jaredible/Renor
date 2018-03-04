package renor.level.entity;

import renor.level.Level;

public class EntityZombie extends EntityMob {
	public EntityZombie(Level level) {
		super(level);
	}

	public void onUpdate() {
		super.onUpdate();
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
	}

	protected void updateEntityActionState() {
	}

	public float getMaxHealth() {
		return 20;
	}
}
