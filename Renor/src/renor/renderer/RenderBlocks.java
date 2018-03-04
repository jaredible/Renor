package renor.renderer;

import renor.Renor;
import renor.level.block.Block;
import renor.level.block.IBlockAccess;
import renor.util.Tessellator;
import renor.util.texture.Icon;

public class RenderBlocks {
	public IBlockAccess blockAccess;
	private final Renor renor;
	private int uvRotateEast = 0;
	private int uvRotateWest = 0;
	private int uvRotateSouth = 0;
	private int uvRotateNorth = 0;
	private int uvRotateTop = 0;
	private int uvRotateBottom = 0;
	private int brightnessTopLeft;
	private int brightnessBottomLeft;
	private int brightnessBottomRight;
	private int brightnessTopRight;
	private int aoBrightnessXYZNNN;
	private int aoBrightnessXYNN;
	private int aoBrightnessXYZNNP;
	private int aoBrightnessYZNN;
	private int aoBrightnessYZNP;
	private int aoBrightnessXYZPNN;
	private int aoBrightnessXYPN;
	private int aoBrightnessXYZPNP;
	private int aoBrightnessXYZNPN;
	private int aoBrightnessXYNP;
	private int aoBrightnessXYZNPP;
	private int aoBrightnessYZPN;
	private int aoBrightnessXYZPPN;
	private int aoBrightnessXYPP;
	private int aoBrightnessYZPP;
	private int aoBrightnessXYZPPP;
	private int aoBrightnessXZNN;
	private int aoBrightnessXZPN;
	private int aoBrightnessXZNP;
	private int aoBrightnessXZPP;
	private double renderMinX;
	private double renderMinY;
	private double renderMinZ;
	private double renderMaxX;
	private double renderMaxY;
	private double renderMaxZ;
	private float colorRedTopLeft;
	private float colorRedBottomLeft;
	private float colorRedBottomRight;
	private float colorRedTopRight;
	private float colorGreenTopLeft;
	private float colorGreenBottomLeft;
	private float colorGreenBottomRight;
	private float colorGreenTopRight;
	private float colorBlueTopLeft;
	private float colorBlueBottomLeft;
	private float colorBlueBottomRight;
	private float colorBlueTopRight;
	private float aoLightValueScratchXYZNNN;
	private float aoLightValueScratchXYNN;
	private float aoLightValueScratchXYZNNP;
	private float aoLightValueScratchYZNN;
	private float aoLightValueScratchYZNP;
	private float aoLightValueScratchXYZPNN;
	private float aoLightValueScratchXYPN;
	private float aoLightValueScratchXYZPNP;
	private float aoLightValueScratchXYZNPN;
	private float aoLightValueScratchXYNP;
	private float aoLightValueScratchXYZNPP;
	private float aoLightValueScratchYZPN;
	private float aoLightValueScratchXYZPPN;
	private float aoLightValueScratchXYPP;
	private float aoLightValueScratchYZPP;
	private float aoLightValueScratchXYZPPP;
	private float aoLightValueScratchXZNN;
	private float aoLightValueScratchXZPN;
	private float aoLightValueScratchXZNP;
	private float aoLightValueScratchXZPP;
	private boolean renderAllFaces = false;
	private boolean flipTexture = false;
	private boolean enableAO;
	private boolean renderFromInside = false;

	public RenderBlocks(IBlockAccess blockAccess) {
		this.blockAccess = blockAccess;

		renor = Renor.getRenor();
	}

	public boolean renderBlockByRenderType(Block block, int x, int y, int z) {
		// renderType
		int n = block.getRenderType();

		if (n == -1) return false;
		else {
			block.setBlockBoundsBasedOnState(blockAccess, x, y, z);
			setRenderBoundsFromBlock(block);
			return n == 0 ? renderStandardBlock(block, x, y, z) : false;
		}
	}

	public boolean renderStandardBlock(Block block, int x, int y, int z) {
		int color = block.colorMultiplier(blockAccess, x, y, z);
		float r = (color >> 16 & 255) / 255.0f;
		float g = (color >> 8 & 255) / 255.0f;
		float b = (color & 255) / 255.0f;

		if (EntityRenderer.anaglyphEnabled) {
			float ar = (r * 30.0f + g * 59.0f + b * 11.0f) / 100.0f;
			float ag = (r * 30.0f + g * 70.0f) / 100.0f;
			float ab = (r * 30.0f + b * 70.0f) / 100.0f;
			r = ar;
			g = ag;
			b = ab;
		}

		return Renor.isAmbientOcclusionEnabled() && Block.lightValue[block.blockId] == 0 ? renderStandardBlockWithAmbientOcclusion(block, x, y, z, r, g, b) : renderStandardBlockWithColorMultiplier(block, x, y, z, r, g, b);
	}

	public boolean renderStandardBlockWithColorMultiplier(Block block, int x, int y, int z, float r, float g, float b) {
		enableAO = false;
		boolean rendered = false;
		Tessellator tess = Tessellator.instance;
		float v0 = 0.5f;
		float v1 = 1.0f;
		float v2 = 0.8f;
		float v3 = 0.6f;
		float v4 = v1 * r;
		float v5 = v1 * g;
		float v6 = v1 * b;
		float v7 = v0;
		float v8 = v2;
		float v9 = v3;
		float v10 = v0;
		float v11 = v2;
		float v12 = v3;
		float v13 = v0;
		float v14 = v2;
		float v15 = v3;

		if (block != Block.grass) {
			v7 = v0 * r;
			v8 = v2 * r;
			v9 = v3 * r;
			v10 = v0 * g;
			v11 = v2 * g;
			v12 = v3 * g;
			v13 = v0 * b;
			v14 = v2 * b;
			v15 = v3 * b;
		}

		int br = block.getMixedBrightnessForBlock(blockAccess, x, y, z);

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x, y - 1, z, 0)) {
			tess.setBrightness(renderMinY > 0.0 ? br : block.getMixedBrightnessForBlock(blockAccess, x, y - 1, z));
			tess.setColorOpaque_F(v7, v10, v13);
			renderFaceYNeg(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, 0));
			rendered = true;
		}

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x, y + 1, z, 1)) {
			tess.setBrightness(renderMaxY < 1.0 ? br : block.getMixedBrightnessForBlock(blockAccess, x, y + 1, z));
			tess.setColorOpaque_F(v4, v5, v6);
			renderFaceYPos(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, 0));
			rendered = true;
		}

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x, y, z - 1, 2)) {
			tess.setBrightness(renderMinZ > 0.0 ? br : block.getMixedBrightnessForBlock(blockAccess, x, y, z - 1));
			tess.setColorOpaque_F(v8, v11, v14);
			renderFaceZNeg(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, 0));
			rendered = true;
		}

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x, y, z + 1, 3)) {
			tess.setBrightness(renderMaxZ < 1.0 ? br : block.getMixedBrightnessForBlock(blockAccess, x, y, z + 1));
			tess.setColorOpaque_F(v8, v11, v14);
			renderFaceZPos(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, 0));
			rendered = true;
		}

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x - 1, y, z, 4)) {
			tess.setBrightness(renderMinX > 0.0 ? br : block.getMixedBrightnessForBlock(blockAccess, x - 1, y, z));
			tess.setColorOpaque_F(v9, v12, v15);
			renderFaceXNeg(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, 0));
			rendered = true;
		}

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x + 1, y, z, 5)) {
			tess.setBrightness(renderMaxX < 1.0 ? br : block.getMixedBrightnessForBlock(blockAccess, x + 1, y, z));
			tess.setColorOpaque_F(v9, v12, v15);
			renderFaceXPos(block, x, y, z, getBlockIcon(block, blockAccess, x, y, z, 0));
			rendered = true;
		}

		return rendered;
	}

	public boolean renderStandardBlockWithAmbientOcclusion(Block block, int x, int y, int z, float r, float g, float b) {
		enableAO = true;
		boolean rendered = false;
		int br = block.getMixedBrightnessForBlock(blockAccess, x, y, z);
		Tessellator tess = Tessellator.instance;
		tess.setBrightness(983055);

		if (renderAllFaces || block.shouldSideBeRendered(blockAccess, x, y - 1, z, 0)) {
			if (renderMinY <= 0.0) --y;

			// continue this
		}

		enableAO = false;
		return rendered;
	}

	public void renderFaceXNeg(Block block, double x, double y, double z, Icon icon) {
		Tessellator tess = Tessellator.instance;

		double minX = x + renderMinX;
		double minY = y + renderMinY;
		double maxY = y + renderMaxY;
		double minZ = z + renderMinZ;
		double maxZ = z + renderMaxZ;

		if (enableAO) {
			tess.setColorOpaque_F(colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft);
			tess.setBrightness(brightnessTopLeft);
			tess.addVertexWithUV(minX, maxY, maxZ, 1.0, 1.0);
			tess.setColorOpaque_F(colorRedBottomLeft, colorGreenBottomLeft, colorBlueBottomLeft);
			tess.setBrightness(brightnessBottomLeft);
			tess.addVertexWithUV(minX, maxY, minZ, 1.0, 0.0);
			tess.setColorOpaque_F(colorRedBottomRight, colorGreenBottomRight, colorBlueBottomRight);
			tess.setBrightness(brightnessBottomRight);
			tess.addVertexWithUV(minX, minY, minZ, 0.0, 0.0);
			tess.setColorOpaque_F(colorRedTopRight, colorGreenTopRight, colorBlueTopRight);
			tess.setBrightness(brightnessTopRight);
			tess.addVertexWithUV(minX, minY, maxZ, 0.0, 1.0);
		} else {
			tess.addVertexWithUV(minX, maxY, maxZ, 1.0, 1.0);
			tess.addVertexWithUV(minX, maxY, minZ, 1.0, 0.0);
			tess.addVertexWithUV(minX, minY, minZ, 0.0, 0.0);
			tess.addVertexWithUV(minX, minY, maxZ, 0.0, 1.0);
		}
	}

	public void renderFaceXPos(Block block, double x, double y, double z, Icon icon) {
		Tessellator tess = Tessellator.instance;

		double maxX = x + renderMaxX;
		double minY = y + renderMinY;
		double maxY = y + renderMaxY;
		double minZ = z + renderMinZ;
		double maxZ = z + renderMaxZ;

		if (enableAO) {
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, minY, maxZ, 0.0, 1.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, minY, minZ, 0.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, maxY, minZ, 1.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, maxY, maxZ, 1.0, 1.0);
		} else {
			tess.addVertexWithUV(maxX, minY, maxZ, 0.0, 1.0);
			tess.addVertexWithUV(maxX, minY, minZ, 0.0, 0.0);
			tess.addVertexWithUV(maxX, maxY, minZ, 1.0, 0.0);
			tess.addVertexWithUV(maxX, maxY, maxZ, 1.0, 1.0);
		}
	}

	public void renderFaceYNeg(Block block, double x, double y, double z, Icon icon) {
		Tessellator tess = Tessellator.instance;

		double minX = x + renderMinX;
		double maxX = x + renderMaxX;
		double minY = y + renderMinY;
		double minZ = z + renderMinZ;
		double maxZ = z + renderMaxZ;

		if (enableAO) {
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, minY, maxZ, 0.0, 1.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, minY, minZ, 0.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, minY, minZ, 1.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, minY, maxZ, 1.0, 1.0);
		} else {
			tess.addVertexWithUV(minX, minY, maxZ, 0.0, 1.0);
			tess.addVertexWithUV(minX, minY, minZ, 0.0, 0.0);
			tess.addVertexWithUV(maxX, minY, minZ, 1.0, 0.0);
			tess.addVertexWithUV(maxX, minY, maxZ, 1.0, 1.0);
		}
	}

	public void renderFaceYPos(Block block, double x, double y, double z, Icon icon) {
		Tessellator tess = Tessellator.instance;

		double minX = x + renderMinX;
		double maxX = x + renderMaxX;
		double maxY = y + renderMaxY;
		double minZ = z + renderMinZ;
		double maxZ = z + renderMaxZ;

		if (enableAO) {
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, maxY, maxZ, 1.0, 1.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, maxY, minZ, 1.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, maxY, minZ, 0.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, maxY, maxZ, 0.0, 1.0);
		} else {
			tess.addVertexWithUV(maxX, maxY, maxZ, 1.0, 1.0);
			tess.addVertexWithUV(maxX, maxY, minZ, 1.0, 0.0);
			tess.addVertexWithUV(minX, maxY, minZ, 0.0, 0.0);
			tess.addVertexWithUV(minX, maxY, maxZ, 0.0, 1.0);
		}
	}

	public void renderFaceZNeg(Block block, double x, double y, double z, Icon icon) {
		Tessellator tess = Tessellator.instance;

		double minX = x + renderMinX;
		double maxX = x + renderMaxX;
		double minY = y + renderMinY;
		double maxY = y + renderMaxY;
		double minZ = z + renderMinZ;

		if (enableAO) {
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, maxY, minZ, 0.0, 1.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, maxY, minZ, 1.0, 1.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, minY, minZ, 1.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, minY, minZ, 0.0, 0.0);
		} else {
			tess.addVertexWithUV(minX, maxY, minZ, 0.0, 1.0);
			tess.addVertexWithUV(maxX, maxY, minZ, 1.0, 1.0);
			tess.addVertexWithUV(maxX, minY, minZ, 1.0, 0.0);
			tess.addVertexWithUV(minX, minY, minZ, 0.0, 0.0);
		}
	}

	public void renderFaceZPos(Block block, double x, double y, double z, Icon icon) {
		Tessellator tess = Tessellator.instance;

		double minX = x + renderMinX;
		double maxX = x + renderMaxX;
		double minY = y + renderMinY;
		double maxY = y + renderMaxY;
		double maxZ = z + renderMaxZ;

		if (enableAO) {
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, maxY, maxZ, 0.0, 1.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(minX, minY, maxZ, 0.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, minY, maxZ, 1.0, 0.0);
			tess.setColorOpaque_F(1.0f, 1.0f, 1.0f);
			tess.setBrightness(255);
			tess.addVertexWithUV(maxX, maxY, maxZ, 1.0, 1.0);
		} else {
			tess.addVertexWithUV(minX, maxY, maxZ, 0.0, 1.0);
			tess.addVertexWithUV(minX, minY, maxZ, 0.0, 0.0);
			tess.addVertexWithUV(maxX, minY, maxZ, 1.0, 0.0);
			tess.addVertexWithUV(maxX, maxY, maxZ, 1.0, 1.0);
		}
	}

	private int getAoBrightness(int par0, int par1, int par2, int par3) {
		if (par0 == 0) par0 = par3;

		if (par1 == 0) par1 = par3;

		if (par2 == 0) par2 = par3;

		return par0 + par1 + par2 + par3 >> 2 & 16711935;
	}

	private int mixAoBrightness(int par0, int par1, int par2, int par3, double par4, double par5, double par6, double par7) {
		int var0 = (int) ((double) (par0 >> 16 & 255) * par4 + (double) (par1 >> 16 & 255) * par5 + (double) (par2 >> 16 & 255) * par6 + (double) (par3 >> 16 & 255) * par7) & 255;
		int var1 = (int) ((double) (par0 & 255) * par4 + (double) (par1 & 255) * par5 + (double) (par2 & 255) * par6 + (double) (par3 & 255) * par7) & 255;
		return var0 << 16 | var1;
	}

	public void setRenderBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		renderMinX = minX;
		renderMaxX = maxX;
		renderMinY = minY;
		renderMaxY = maxY;
		renderMinZ = minZ;
		renderMaxZ = maxZ;
	}

	public void setRenderBoundsFromBlock(Block block) {
		renderMinX = block.getBlockBoundsMinX();
		renderMaxX = block.getBlockBoundsMaxX();
		renderMinY = block.getBlockBoundsMinY();
		renderMaxY = block.getBlockBoundsMaxY();
		renderMinZ = block.getBlockBoundsMinZ();
		renderMaxZ = block.getBlockBoundsMaxZ();
	}

	public Icon getBlockIcon(Block block, IBlockAccess blockAccess, int x, int y, int z, int side) {
		return getIconSafe(block.getBlockTexture(blockAccess, x, y, z, side));
	}

	public Icon getIconSafe(Icon icon) {
		return icon == null ? renor.renderEngine.getMissingIcon(0) : icon;
	}

	public void setRenderAllFaces(boolean flag) {
		renderAllFaces = flag;
	}

	public void setRenderFromInside(boolean flag) {
		renderFromInside = flag;
	}
}
