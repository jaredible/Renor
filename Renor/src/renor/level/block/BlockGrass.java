package renor.level.block;

import renor.level.material.Material;
import renor.util.texture.Icon;
import renor.util.texture.IconRegister;

public class BlockGrass extends Block {
	private Icon iconArray;

	protected BlockGrass(int id, Material material) {
		super(id, material);
	}

	public void registerIcons(IconRegister iconRegister) {
		iconArray = iconRegister.registerIcon("grass");
	}

	public Icon getIcon(int side, int metadata) {
		return iconArray;
	}
}
