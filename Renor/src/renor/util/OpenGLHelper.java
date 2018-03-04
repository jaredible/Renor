package renor.util;

import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

public class OpenGLHelper {
	public static int defaultTexUnit;
	public static int lightmapTexUnit;
	private static boolean useMultitextureARB = false;
	private static boolean openGL14 = false;

	public static void initializeTextures() {
		useMultitextureARB = GLContext.getCapabilities().GL_ARB_multitexture && !GLContext.getCapabilities().OpenGL13;

		boolean test = false;
		if (test) {
			if (useMultitextureARB) {
				defaultTexUnit = ARBMultitexture.GL_TEXTURE0_ARB;
				lightmapTexUnit = ARBMultitexture.GL_TEXTURE1_ARB;
			} else {
				defaultTexUnit = GL13.GL_TEXTURE0;
				lightmapTexUnit = GL13.GL_TEXTURE1;
			}
		} else {
			if (useMultitextureARB) {
				defaultTexUnit = 33984;
				lightmapTexUnit = 33985;
			} else {
				defaultTexUnit = 33984;
				lightmapTexUnit = 33985;
			}
		}

		openGL14 = GLContext.getCapabilities().OpenGL14;
	}

	public static void setActiveTexture(int texture) {
		if (useMultitextureARB) ARBMultitexture.glActiveTextureARB(texture);
		else GL13.glActiveTexture(texture);
	}

	public static void setClientActiveTexture(int texture) {
		if (useMultitextureARB) ARBMultitexture.glClientActiveTextureARB(texture);
		else GL13.glClientActiveTexture(texture);
	}

	public static void setLightmapTextureCoords(int target, float s, float t) {
		if (useMultitextureARB) ARBMultitexture.glMultiTexCoord2fARB(target, s, t);
		else GL13.glMultiTexCoord2f(target, s, t);
	}

	public static void glBlendFunc(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
		if (openGL14) GL14.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
		else GL11.glBlendFunc(sfactorRGB, dfactorRGB);
	}
}
