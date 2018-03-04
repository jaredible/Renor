package renor.misc;

import renor.level.entity.Entity;
import renor.level.entity.EntityLiving;

public class EntityDamageSource extends DamageSource {
	protected Entity damageSourceEntity;

	protected EntityDamageSource(String damageType, Entity entity) {
		super(damageType);
		damageSourceEntity = entity;
	}

	public Entity getEntity() {
		return damageSourceEntity;
	}

	public String getDeathMessage(EntityLiving living) {
		String a = "death.attack." + damageType;
		return "";
	}
}
