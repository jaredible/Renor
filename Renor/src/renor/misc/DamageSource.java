package renor.misc;

import renor.level.entity.Entity;
import renor.level.entity.EntityLiving;
import renor.level.entity.EntityPlayer;

public class DamageSource {
	public String damageType;
	public static DamageSource generic = new DamageSource("generic");
	public static DamageSource fall = new DamageSource("fall");
	public static DamageSource inWall = new DamageSource("inWall");
	public static DamageSource outOfLevel = new DamageSource("outOfLevel");

	protected DamageSource(String damageType) {
		this.damageType = damageType;
	}

	public static DamageSource causePlayerDamage(EntityPlayer player) {
		return new EntityDamageSource("player", player);
	}

	public Entity getEntity() {
		return null;
	}

	public boolean isUnblockable() {
		return false;
	}

	public String getDamageMessage(EntityLiving living) {
		String a = "death.attack." + damageType;
		return "";
	}
}
