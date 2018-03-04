package renor.util.callable;

import java.util.concurrent.Callable;

import org.lwjgl.Sys;

public class CallableLWJGLVersion implements Callable<Object> {

	public Object call() throws Exception {
		return getType();
	}

	public String getType() {
		return Sys.getVersion();
	}
}
