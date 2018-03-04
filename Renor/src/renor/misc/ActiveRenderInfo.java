package renor.misc;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import renor.level.entity.EntityPlayer;
import renor.util.MathHelper;

public class ActiveRenderInfo {
	private static IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
	private static FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
	private static FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
	private static FloatBuffer objectCoords = GLAllocation.createDirectFloatBuffer(3);
	public static float objectX = 0.0f;
	public static float objectY = 0.0f;
	public static float objectZ = 0.0f;
	public static float rotationX;
	public static float rotationXZ;
	public static float rotationZ;
	public static float rotationYZ;
	public static float rotationXY;

	public static void updateRenderInfo(EntityPlayer player, boolean par0) {
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
		float x = (float) ((viewport.get(0) + viewport.get(2)) / 2);
		float y = (float) ((viewport.get(1) + viewport.get(3)) / 2);
		GLU.gluUnProject(x, y, 0.0f, modelview, projection, viewport, objectCoords);
		objectX = objectCoords.get(0);
		objectY = objectCoords.get(1);
		objectZ = objectCoords.get(2);
		int n = par0 ? 1 : 0;
		float yaw = player.rotationYaw;
		float pitch = player.rotationPitch;
		rotationX = MathHelper.cos(yaw * (float) Math.PI / 180.0f) * (float) (1 - n * 2);
		rotationZ = MathHelper.sin(yaw * (float) Math.PI / 180.0f) * (float) (1 - n * 2);
		rotationYZ = -rotationZ * MathHelper.sin(pitch * (float) Math.PI / 180.0f) * (float) (1 - n * 2);
		rotationXY = rotationX * MathHelper.sin(pitch * (float) Math.PI / 180.0f) * (float) (1 - n * 2);
		rotationXZ = MathHelper.cos(pitch * (float) Math.PI / 180.0f);
	}
}
