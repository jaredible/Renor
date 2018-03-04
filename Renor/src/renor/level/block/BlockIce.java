package renor.level.block;

import renor.level.material.Material;

public class BlockIce extends Block {
	protected BlockIce(int id, Material material) {
		super(id, material);

		slipperiness = 0.98f;
	}

	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return super.shouldSideBeRendered(blockAccess, x, y, z, 1 - side);
	}

	public boolean isOpaque() {
		return false;
	}

	public int getRenderBlockPass() {
		return 1;
	}
}
