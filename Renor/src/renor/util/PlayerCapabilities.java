package renor.util;

public class PlayerCapabilities {
	private float walkSpeed = 0.1f;
	private float flySpeed = 0.05f;
	public boolean isFlying = false;
	public boolean allowFlying = false;
	public boolean allowEdit = true;
	public boolean disableDamage = false;

	public float getWalkSpeed() {
		return walkSpeed;
	}

	public float getFlySpeed() {
		return flySpeed;
	}
}
