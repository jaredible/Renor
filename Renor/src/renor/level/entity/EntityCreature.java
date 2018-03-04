package renor.level.entity;

import renor.level.Level;

public abstract class EntityCreature extends EntityLiving {

	public EntityCreature(Level level) {
		super(level);
	}

	public float getMaxHealth() {
		return 20;
	}
}
