package renor.util.texture;

public class TextureStitched implements Icon {
	private final String textureName;
	protected Texture textureSheet;
	protected int originX;
	protected int originY;
	private float minU;
	private float maxU;
	private float minV;
	private float maxV;

	public static TextureStitched makeTextureStitched(String name) {
		return new TextureStitched(name);
	}

	protected TextureStitched(String name) {
		textureName = name;
	}

	public void init(Texture texture, int originX, int originY, int width, int height) {
		textureSheet = texture;
		this.originX = originX;
		this.originY = originY;
		float var0 = 0.01f / texture.getWidth();
		float var1 = 0.01f / texture.getHeight();
		minU = originX / texture.getWidth() + var0;
		maxU = (originX + width) / texture.getWidth() - var0;
		minV = originY / texture.getHeight() + var1;
		maxV = (originY + height) / texture.getHeight() - var1;
	}

	public int getOriginX() {
		return originX;
	}

	public int getOriginY() {
		return originY;
	}

	public float getMinU() {
		return minU;
	}

	public float getMaxU() {
		return maxU;
	}

	public float getInterpolatedU(double n) {
		float var0 = maxU - minU;
		return minU + var0 * ((float) n / 16.0f);
	}

	public float getMinV() {
		return minV;
	}

	public float getMaxV() {
		return maxV;
	}

	public float getInterpolatedV(double n) {
		float var0 = maxV - minV;
		return minV + var0 * ((float) n / 16.0f);
	}

	public String getIconName() {
		return textureName;
	}

	public int getSheetWidth() {
		return textureSheet.getWidth();
	}

	public int getSheetHeight() {
		return textureSheet.getHeight();
	}
}
