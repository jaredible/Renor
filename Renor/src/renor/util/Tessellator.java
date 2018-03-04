package renor.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;

import renor.misc.GLAllocation;

public class Tessellator {
	private ByteBuffer byteBuffer;
	private IntBuffer intBuffer;
	private FloatBuffer floatBuffer;
	private ShortBuffer shortBuffer;
	public static final Tessellator instance = new Tessellator(2097152);
	private int bufferSize;
	private int drawMode;
	private int vertexCount;
	private int rawBufferIndex;
	private int addedVertices;
	private int brightness;
	private int color;
	private int normal;
	private int[] rawBuffer;
	private double textureU;
	private double textureV;
	private double xOffset;
	private double yOffset;
	private double zOffset;
	private boolean isDrawing = false;
	private boolean hasTexture = false;
	private boolean hasBrightness = false;
	private boolean hasColor = false;
	private boolean hasNormals = false;
	private boolean isColorDisabled = false;

	private Tessellator(int bufferSize) {
		this.bufferSize = bufferSize;

		byteBuffer = GLAllocation.createDirectByteBuffer(bufferSize * 4);
		intBuffer = byteBuffer.asIntBuffer();
		floatBuffer = byteBuffer.asFloatBuffer();
		shortBuffer = byteBuffer.asShortBuffer();
		rawBuffer = new int[bufferSize];
	}

	public int draw() {
		if (!isDrawing) throw new IllegalStateException("Not tessellating!");
		else {
			isDrawing = false;

			if (vertexCount > 0) {
				intBuffer.clear();
				intBuffer.put(rawBuffer, 0, rawBufferIndex);
				byteBuffer.position(0);
				byteBuffer.limit(rawBufferIndex * 4);

				if (hasTexture) {
					floatBuffer.position(3);
					GL11.glTexCoordPointer(2, 32, floatBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (hasBrightness) {
					OpenGLHelper.setClientActiveTexture(OpenGLHelper.lightmapTexUnit);
					shortBuffer.position(14);
					GL11.glTexCoordPointer(2, 32, shortBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGLHelper.setClientActiveTexture(OpenGLHelper.defaultTexUnit);
				}

				if (hasColor) {
					byteBuffer.position(20);
					GL11.glColorPointer(4, true, 32, byteBuffer);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				}

				if (hasNormals) {
					byteBuffer.position(24);
					GL11.glNormalPointer(32, byteBuffer);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				}

				floatBuffer.position(0);
				GL11.glVertexPointer(3, 32, floatBuffer);
				GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				GL11.glDrawArrays(drawMode, 0, vertexCount);
				GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

				if (hasTexture) GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

				if (hasBrightness) {
					OpenGLHelper.setClientActiveTexture(OpenGLHelper.lightmapTexUnit);
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGLHelper.setClientActiveTexture(OpenGLHelper.defaultTexUnit);
				}

				if (hasColor) GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

				if (hasNormals) GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
			}

			int n = rawBufferIndex * 4;
			reset();
			return n;
		}
	}

	public TessellatorVertexState getVertexState(float par0, float par1, float par2) {
		return null;
	}

	public void setVertexState(TessellatorVertexState tessellatorVertexState) {
	}

	private void reset() {
		vertexCount = 0;
		byteBuffer.clear();
		rawBufferIndex = 0;
		addedVertices = 0;
	}

	public void startDrawing(int mode) {
		if (isDrawing) throw new IllegalStateException("Already tessellating!");

		isDrawing = true;
		reset();
		drawMode = mode;
		hasNormals = false;
		hasColor = false;
		hasTexture = false;
		hasBrightness = false;
		isColorDisabled = false;
	}

	public void startDrawingQuads() {
		startDrawing(7);
	}

	public void setColorRGBA(int red, int green, int blue, int alpha) {
		if (!isColorDisabled) {
			if (red < 0) red = 0;
			if (green < 0) green = 0;
			if (blue < 0) blue = 0;
			if (alpha < 0) alpha = 0;
			if (red > 255) red = 255;
			if (green > 255) green = 255;
			if (blue > 255) blue = 255;
			if (alpha > 255) alpha = 255;

			hasColor = true;

			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) color = alpha << 24 | blue << 16 | green << 8 | red;
			else color = red << 24 | green << 16 | blue << 8 | alpha;
		}
	}

	public void setColorRGBA_I(int rgb, int alpha) {
		int red = rgb >> 16 & 255;
		int green = rgb >> 8 & 255;
		int blue = rgb & 255;
		setColorRGBA(red, green, blue, alpha);
	}

	public void setColorRGBA_F(float red, float green, float blue, float alpha) {
		setColorRGBA((int) (red * 255.0f), (int) (green * 255.0f), (int) (blue * 255.0f), (int) (alpha * 255.0f));
	}

	public void setColorOpaque(int red, int green, int blue) {
		setColorRGBA(red, green, blue, 255);
	}

	public void setColorOpaque_I(int rgb) {
		int red = rgb >> 16 & 255;
		int green = rgb >> 8 & 255;
		int blue = rgb & 255;
		setColorOpaque(red, green, blue);
	}

	public void setColorOpaque_F(float red, float blue, float green) {
		setColorOpaque((int) (red * 255.0f), (int) (blue * 255.0f), (int) (green * 255.0f));
	}

	public void disableColor() {
		isColorDisabled = true;
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
		hasBrightness = true;
	}

	public void setNormal(float x, float y, float z) {
		hasNormals = true;
		byte xx = (byte) ((int) (x * 127.0f));
		byte yy = (byte) ((int) (y * 127.0f));
		byte zz = (byte) ((int) (z * 127.0f));
		normal = xx & 255 | (yy & 255) << 8 | (zz & 255) << 16;
	}

	public void addVertex(double x, double y, double z) {
		++addedVertices;

		if (hasTexture) {
			rawBuffer[rawBufferIndex + 3] = Float.floatToRawIntBits((float) textureU);
			rawBuffer[rawBufferIndex + 4] = Float.floatToRawIntBits((float) textureV);
		}

		if (hasBrightness) rawBuffer[rawBufferIndex + 7] = brightness;

		if (hasColor) rawBuffer[rawBufferIndex + 5] = color;

		if (hasNormals) rawBuffer[rawBufferIndex + 6] = normal;

		rawBuffer[rawBufferIndex + 0] = Float.floatToRawIntBits((float) (x + xOffset));
		rawBuffer[rawBufferIndex + 1] = Float.floatToRawIntBits((float) (y + yOffset));
		rawBuffer[rawBufferIndex + 2] = Float.floatToRawIntBits((float) (z + zOffset));
		++vertexCount;
		rawBufferIndex += 8;

		if (vertexCount % 4 == 0 && rawBufferIndex >= bufferSize - 32) {
			draw();
			isDrawing = true;
		}
	}

	public void setTextureUV(double u, double v) {
		hasTexture = true;
		textureU = u;
		textureV = v;
	}

	public void addVertexWithUV(double x, double y, double z, double u, double v) {
		setTextureUV(u, v);
		addVertex(x, y, z);
	}

	public void setTranslation(double x, double y, double z) {
		xOffset = x;
		yOffset = y;
		zOffset = z;
	}

	public void addTranslation(float x, float y, float z) {
		xOffset += x;
		yOffset += y;
		zOffset += z;
	}
}
