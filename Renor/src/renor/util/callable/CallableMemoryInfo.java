package renor.util.callable;

import java.util.concurrent.Callable;

public class CallableMemoryInfo implements Callable<Object> {

	public Object call() throws Exception {
		return getMemoryInfoAsString();
	}

	public String getMemoryInfoAsString() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long maxMemoryMB = maxMemory / 1024 / 1024;
		long totalMemoryMB = totalMemory / 1024 / 1024;
		long freeMemoryMB = freeMemory / 1024 / 1024;
		return freeMemory + " bytes (" + freeMemoryMB + " MB) / " + totalMemory + " bytes (" + totalMemoryMB + " MB) up to " + maxMemory + " bytes (" + maxMemoryMB + " MB)";
	}
}
