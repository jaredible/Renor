package renor.renderer;

import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;

import renor.misc.GLAllocation;

public class RenderList {
	private IntBuffer glLists = GLAllocation.createDirectIntBuffer(65536);
	public int renderChunkX;
	public int renderChunkY;
	public int renderChunkZ;
	private double cameraX;
	private double cameraY;
	private double cameraZ;
	private boolean valid = false;
	private boolean bufferFlipped = false;

	public void setupRenderList(int p0, int p1, int p2, double p3, double p4, double p5) {
		valid = true;
		glLists.clear();
		renderChunkX = p0;
		renderChunkY = p1;
		renderChunkZ = p2;
		cameraX = p3;
		cameraY = p4;
		cameraZ = p5;
	}

	public boolean rendersChunk(int x, int y, int z) {
		return !valid ? false : x == renderChunkX && y == renderChunkY && z == renderChunkZ;
	}

	public void addGLRenderList(int n) {
		glLists.put(n);

		if (glLists.remaining() == 0) callLists();
	}

	public void callLists() {
		if (valid) {
			if (!bufferFlipped) {
				glLists.flip();
				bufferFlipped = true;
			}

			if (glLists.remaining() > 0) {
				glPushMatrix();
				glTranslatef((float) ((double) renderChunkX - cameraX), (float) ((double) renderChunkY - cameraY), (float) ((double) renderChunkZ - cameraZ));
				glCallLists(glLists);
				glPopMatrix();
			}
		}
	}

	public void resetList() {
		valid = false;
		bufferFlipped = false;
	}
}
