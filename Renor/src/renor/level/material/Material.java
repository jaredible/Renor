package renor.level.material;

public class Material {
	private boolean replaceable;
	private boolean isTranslucent;

	public static final Material air = new MaterialTransparent();
	public static final Material grass = new Material();
	public static final Material ice = new Material().setTranslucent();

	// TODO ice material should be solid

	public boolean isSolid() {
		return true;
	}

	public boolean isReplaceable() {
		return replaceable;
	}

	public Material setReplaceable() {
		replaceable = true;
		return this;
	}

	public boolean blocksMovement() {
		return true;
	}

	public boolean isOpaque() {
		return isTranslucent ? false : blocksMovement();
	}

	private Material setTranslucent() {
		isTranslucent = true;
		return this;
	}
}
