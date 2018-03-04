package renor.util.callable;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class CallableJVMFlags implements Callable<Object> {

	public Object call() throws Exception {
		return getJVMFlagsAsString();
	}

	public String getJVMFlagsAsString() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		List<String> beanArgs = bean.getInputArguments();
		int i = 0;
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<String> stringIterator = beanArgs.iterator();

		while (stringIterator.hasNext()) {
			String line = stringIterator.next();

			if (line.startsWith("-X")) {
				if (i++ > 0) stringBuilder.append(" ");

				stringBuilder.append(line);
			}
		}

		return String.format("%d total; %s", new Object[] { Integer.valueOf(i), stringBuilder.toString() });
	}
}
