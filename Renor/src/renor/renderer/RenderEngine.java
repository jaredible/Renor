package renor.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

import renor.Renor;
import renor.misc.GLAllocation;
import renor.misc.GameSettings;
import renor.util.IntHashMap;
import renor.util.texture.Icon;
import renor.util.texture.TextureMap;

public class RenderEngine {
	private HashMap<String, Integer> textureMap = new HashMap<String, Integer>();
	private IntBuffer imageData = GLAllocation.createDirectIntBuffer(4194304);
	private BufferedImage missingTextureImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
	private GameSettings options;
	private IntHashMap textureNameToImageMap = new IntHashMap();
	private final TextureMap textureMapBlocks;
	private int boundTexture;

	public RenderEngine(GameSettings gameSettings) {
		options = gameSettings;

		Graphics g = missingTextureImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 64, 64);
		g.setColor(Color.BLACK);
		int t = 10;
		int i = 0;

		while (t < 64) {
			String s = i++ % 2 == 0 ? "missing" : "texture";
			g.drawString(s, 1, t);
			t += g.getFont().getSize();

			if (i % 2 == 0) t += 5;
		}

		g.dispose();
		textureMapBlocks = new TextureMap(0, "terrain", "textures/blocks", missingTextureImage);
	}

	public void bindTexture(String name) {
		bindTexture(getTexture(name));
	}

	private void bindTexture(int textureName) {
		if (textureName != boundTexture) {
			glBindTexture(GL_TEXTURE_2D, textureName);
			boundTexture = textureName;
		}
	}

	public void resetBoundTexture() {
		boundTexture = -1;
	}

	private int getTexture(String name) {
		if (name.equals("/terrain.png")) {
			// do other stuff here
			return 0;
		} else if (name.equals("/gui/items.png")) {
			// do other stuff here
			return 0;
		} else {
			// value
			Integer v = textureMap.get(name);

			if (v != null) return v.intValue();
			else {
				String tempName = name;

				try {
					int textureName = GLAllocation.generateTextureNames();
					boolean blur = name.startsWith("%blur%");

					if (blur) name = name.substring(6);

					boolean clamp = name.startsWith("%clamp%");

					if (clamp) name = name.substring(7);

					InputStream stream = RenderEngine.class.getResourceAsStream(name);

					if (stream == null) setupTextureExt(missingTextureImage, textureName, blur, clamp);
					else setupTextureExt(readTextureImage(stream), textureName, blur, clamp);

					textureMap.put(tempName, Integer.valueOf(textureName));
					return textureName;
				} catch (Exception e) {
					e.printStackTrace();
					int textureName = GLAllocation.generateTextureNames();
					setupTexture(missingTextureImage, textureName);
					textureMap.put(name, Integer.valueOf(textureName));
					return textureName;
				}
			}
		}
	}

	public int allocateAndSetupTexture(BufferedImage image) {
		int textureName = GLAllocation.generateTextureNames();
		setupTexture(image, textureName);
		textureNameToImageMap.addKey(textureName, image);
		return textureName;
	}

	public void setupTexture(BufferedImage textureImage, int textureName) {
		setupTextureExt(textureImage, textureName, false, false);
	}

	public void setupTextureExt(BufferedImage textureImage, int textureName, boolean blur, boolean clamp) {
		bindTexture(textureName);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		if (blur) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		}

		if (clamp) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		}

		int width = textureImage.getWidth();
		int height = textureImage.getHeight();
		int[] data = new int[width * height];
		textureImage.getRGB(0, 0, width, height, data, 0, width);

		if (options.anaglyph) data = colorToAnaglyph(data);

		imageData.clear();
		imageData.put(data);
		imageData.position(0).limit(data.length);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, imageData);
	}

	private int[] colorToAnaglyph(int[] data) {
		int[] v0 = new int[data.length];

		for (int i = 0; i < data.length; ++i) {
			int a = data[i] >> 24 & 255;
			int r = data[i] >> 16 & 255;
			int g = data[i] >> 8 & 255;
			int b = data[i] & 255;
			int ar = (r * 30 + g * 59 + b * 11) / 100;
			int ag = (r * 30 + g * 70) / 100;
			int ab = (r * 30 + b * 70) / 100;
			v0[i] = a << 24 | ar << 16 | ag << 8 | ab;
		}

		return v0;
	}

	public void createTextureFromBytes(int[] data, int width, int height, int textureName) {
		bindTexture(textureName);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		if (options.anaglyph) data = colorToAnaglyph(data);

		imageData.clear();
		imageData.put(data);
		imageData.position(0).limit(data.length);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, imageData);
	}

	public void refreshTextures() {
		refreshTextureMaps();
		Iterator<?> texturesIterator = textureNameToImageMap.getKeySet().iterator();
		BufferedImage textureImage;

		while (texturesIterator.hasNext()) {
			int n = ((Integer) texturesIterator.next()).intValue();
			textureImage = (BufferedImage) textureNameToImageMap.lookup(n);
			setupTexture(textureImage, n);
		}

		texturesIterator = textureMap.keySet().iterator();
		String name;

		while (texturesIterator.hasNext()) {
			name = (String) texturesIterator.next();

			try {
				int n = textureMap.get(name).intValue();
				boolean blur = name.startsWith("%blur%");

				if (blur) name = name.substring(6);

				boolean clamp = name.startsWith("%clamp%");

				if (clamp) name = name.substring(7);

				BufferedImage image = readTextureImage(RenderEngine.class.getResourceAsStream(name));
				setupTextureExt(image, n, blur, clamp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Renor.getRenor().fontRenderer.readFontData();
	}

	public void refreshTextureMaps() {
		textureMapBlocks.refreshTextures();
	}

	private BufferedImage readTextureImage(InputStream inputStream) throws IOException {
		BufferedImage result = ImageIO.read(inputStream);
		inputStream.close();
		return result;
	}

	public Icon getMissingIcon(int n) {
		switch (n) {
			case 0:
				return textureMapBlocks.getMissingIcon();
			case 1:
			default:
				return null;
		}
	}
}
