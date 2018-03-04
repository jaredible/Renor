package renor.util;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import renor.misc.GLAllocation;
import renor.vector.Vec3;

public class RenderHelper {
	private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
	private static final Vec3 a = Vec3.createVectorHelper(0.20000000298023224, 1.0, -0.699999988079071).normalize();
	private static final Vec3 b = Vec3.createVectorHelper(-0.20000000298023224, 1.0, 0.699999988079071).normalize();

	public static void disableStandardLighting() {
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_LIGHT0);
		GL11.glDisable(GL11.GL_LIGHT1);
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
	}

	public static void enableStandardLighting() {
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHT1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
		float ambient = 0.4f;
		float diffuse = 0.6f;
		float specular = 0.0f;
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, setColorBuffer(a.xCoord, a.yCoord, a.zCoord, 0.0));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, setColorBuffer(diffuse, diffuse, diffuse, 1.0f));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f));
		GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, setColorBuffer(specular, specular, specular, 1.0f));
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, setColorBuffer(b.xCoord, b.yCoord, b.zCoord, 0.0));
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, setColorBuffer(diffuse, diffuse, diffuse, 1.0f));
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f));
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, setColorBuffer(specular, specular, specular, 1.0f));
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, setColorBuffer(ambient, ambient, ambient, 1.0f));
	}

	public static void enableGUIStandardLighting() {
		GL11.glPushMatrix();
		GL11.glRotatef(-30.0f, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(165.0f, 1.0f, 0.0f, 0.0f);
		enableStandardLighting();
		GL11.glPopMatrix();
	}

	private static FloatBuffer setColorBuffer(float red, float green, float blue, float alpha) {
		colorBuffer.clear();
		colorBuffer.put(red).put(green).put(blue).put(alpha);
		colorBuffer.flip();
		return colorBuffer;
	}

	private static FloatBuffer setColorBuffer(double red, double green, double blue, double alpha) {
		return setColorBuffer((float) red, (float) green, (float) blue, (float) alpha);
	}
}
