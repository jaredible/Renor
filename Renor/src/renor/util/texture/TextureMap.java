package renor.util.texture;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import renor.level.block.Block;
import renor.renderer.RenderManager;

public class TextureMap implements IconRegister {
	private final String textureName;
	private final String basePath;
	private BufferedImage missingImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
	private final String textureExt;
	private final HashMap<String, TextureStitched> textureStitchedMap = new HashMap<String, TextureStitched>();
	private final int textureType;

	public TextureMap(int n, String name, String path, BufferedImage missingImage) {
		textureType = n;
		textureName = name;
		basePath = path;
		this.missingImage = missingImage;
		textureExt = ".png";
	}

	public void refreshTextures() {
		textureStitchedMap.clear();

		if (textureType == 0) {
			Block[] blocks = Block.blocksList;

			for (int i = 0; i < blocks.length; i++) {
				Block b = blocks[i];

				if (b != null) b.registerIcons(this);
			}

			RenderManager.instance.updateIcons(this);
		}

		textureStitchedMap.clear();
	}

	public Icon registerIcon(String name) {
		if (name == null) new RuntimeException("Don't register null!").printStackTrace();

		TextureStitched textureStitched = textureStitchedMap.get(name);

		if (textureStitched == null) {
			textureStitched = TextureStitched.makeTextureStitched(name);
			textureStitchedMap.put(name, textureStitched);
		}

		return textureStitched;
	}

	public Icon getMissingIcon() {
		return null;
	}
}
