package renor.level.entity;

import renor.level.Level;
import renor.util.IMerchant;

public class EntityMerchant extends EntityLiving implements IMerchant {
	private EntityPlayer buyingPlayer;

	public EntityMerchant(Level level) {
		super(level);
	}

	public float getMaxHealth() {
		return 20;
	}

	protected void updateAITick() {
	}

	public EntityPlayer getCustomer() {
		return buyingPlayer;
	}

	public void setCustomer(EntityPlayer player) {
		buyingPlayer = player;
	}
}
