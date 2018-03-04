package renor.util.texture;

import java.util.HashMap;

import renor.Renor;

public class TextureManager {
	private final HashMap<Integer, Texture> texturesMap = new HashMap<Integer, Texture>();
	private static TextureManager instance;

	public static void init() {
		instance = new TextureManager();
	}

	public static TextureManager getInstance() {
		return instance;
	}

	public void registerTexture(Texture texture) {
		if (texturesMap.containsValue(texture)) Renor.getRenor().getLogAgent().logWarning("TextureManager.registerTexture called, but this texture has already been registered. Ignoring.");
		else texturesMap.put(Integer.valueOf(texture.getGLTextureId()), texture);
	}

	public Texture makeTexture() {
		Texture texture = new Texture();
		registerTexture(texture);
		return texture;
	}
}
