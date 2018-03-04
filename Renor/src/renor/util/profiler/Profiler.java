package renor.util.profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profiler {
	private final List<String> sectionList = new ArrayList<String>();
	private final List<Long> timestampList = new ArrayList<Long>();
	private final Map<String, Long> profilingMap = new HashMap<String, Long>();
	private String profilingSection = "";
	public boolean profilingEnabled = false;

	public void startSection(String sectionName) {
		if (profilingEnabled) {
			if (profilingSection.length() > 0) profilingSection = profilingSection + ".";

			profilingSection = profilingSection + sectionName;
			sectionList.add(sectionName);
			timestampList.add(Long.valueOf(System.nanoTime()));
		}
	}

	public void endSection() {
		if (profilingEnabled) {
			// nanoTime
			long nt = System.nanoTime();
			// time
			long t = timestampList.remove(timestampList.size() - 1).longValue();
			sectionList.remove(sectionList.size() - 1);
			// passedNanoTime
			long n = nt - t;

			if (profilingMap.containsKey(profilingSection)) profilingMap.put(profilingSection, Long.valueOf(profilingMap.get(profilingSection).longValue() + n));
			else profilingMap.put(profilingSection, Long.valueOf(n));

			if (n > 100000000) System.err.println("Something's taking too long! '" + profilingSection + "' took approximately " + (double) n / 1000000.0 + " ms");

			profilingSection = !sectionList.isEmpty() ? sectionList.get(sectionList.size() - 1) : "";
		}
	}

	public void endStartSection(String sectionName) {
		endSection();
		startSection(sectionName);
	}

	public void clearProfiling() {
		profilingMap.clear();
		profilingSection = "";
		sectionList.clear();
	}

	public List<ProfilerResult> getProfilingData(String string) {
		if (!profilingEnabled) return null;
		else {
			// do other stuff in here
			return null;
		}
	}
}
