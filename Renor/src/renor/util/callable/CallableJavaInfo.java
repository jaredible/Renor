package renor.util.callable;

import java.util.concurrent.Callable;

public class CallableJavaInfo implements Callable<Object> {

	public Object call() throws Exception {
		return getJavaInfoAsString();
	}

	public String getJavaInfoAsString() {
		return System.getProperty("java.version") + ", " + System.getProperty("java.vendor");
	}
}
