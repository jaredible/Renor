package renor.container;

import renor.level.entity.EntityPlayer;

public class ContainerPlayer extends Container {
	private final EntityPlayer thePlayer;

	public ContainerPlayer(EntityPlayer player) {
		thePlayer = player;
	}
}
