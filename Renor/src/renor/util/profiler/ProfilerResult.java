package renor.util.profiler;

public final class ProfilerResult implements Comparable<ProfilerResult> {
	public String string;
	public double a;
	public double b;

	public ProfilerResult(String string, double a, double b) {
		this.string = string;
		this.a = a;
		this.b = b;
	}

	public int compareTo(ProfilerResult profilerResult) {
		return doCompare(profilerResult);
	}

	public int doCompare(ProfilerResult profilerResult) {
		return profilerResult.a > a ? 1 : profilerResult.a < a ? -1 : profilerResult.string.compareTo(string);
	}

	public int getColor() {
		return (string.hashCode() & 0xaaaaaa) + 0x444444;
	}
}
