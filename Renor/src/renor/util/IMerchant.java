package renor.util;

import renor.level.entity.EntityPlayer;

public abstract interface IMerchant {
	public abstract EntityPlayer getCustomer();

	public abstract void setCustomer(EntityPlayer player);
}
