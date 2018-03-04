package renor.level.entity;

import renor.Renor;
import renor.level.Level;
import renor.network.NetClientHandler;
import renor.util.Session;

public class EntityClientPlayerMP extends EntityPlayer {
	public NetClientHandler sendQueue;

	public EntityClientPlayerMP(Renor renor, Level level, Session session) {
		super(renor, level, session);
	}
}
