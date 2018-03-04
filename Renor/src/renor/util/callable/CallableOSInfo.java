package renor.util.callable;

import java.util.concurrent.Callable;

public class CallableOSInfo implements Callable<Object> {

	public Object call() throws Exception {
		return getOSAsString();
	}

	public String getOSAsString() {
		return System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version");
	}
}
