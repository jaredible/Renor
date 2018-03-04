package renor.util;

import org.lwjgl.opengl.GLContext;

public class OpenGLCapsChecker {

	public static boolean checkARBOcclusion() {
		return GLContext.getCapabilities().GL_ARB_occlusion_query;
	}
}
