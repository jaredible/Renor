package renor.util.callable;

import java.util.concurrent.Callable;

import org.lwjgl.opengl.GL11;

public class CallableOpenGLInfo implements Callable<Object> {

	public Object call() throws Exception {
		return getOpenGLInfoAsString();
	}

	public String getOpenGLInfoAsString() {
		return GL11.glGetString(GL11.GL_RENDERER) + " GL version " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR);
	}
}
