package renor.level.material;

public class MaterialTransparent extends Material {
	public MaterialTransparent() {
		setReplaceable();
	}

	public boolean isSolid() {
		return false;
	}

	public boolean blocksMovement() {
		return false;
	}
}
