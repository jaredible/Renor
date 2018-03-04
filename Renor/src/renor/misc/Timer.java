package renor.misc;

import renor.Renor;

public class Timer {
	private long lastSyncSysClock;
	private long lastSyncHRClock;
	private long a;
	public int elapsedTicks;
	private float ticksPerSecond;
	public float renderPartialTicks;
	public float timerSpeed = 1.0f;
	public float elapsedPartialTicks = 0.0f;
	private double lastHRTime;
	private double timeSyncAdjustment = 1.0;

	public Timer(float ticksPerSecond) {
		this.ticksPerSecond = ticksPerSecond;
		lastSyncSysClock = Renor.getSystemTime();
		lastSyncHRClock = System.nanoTime() / 1000000;
	}

	public void updateTimer() {
		long v0 = Renor.getSystemTime();
		long v1 = v0 - lastSyncSysClock;
		long v2 = System.nanoTime() / 1000000;
		double v3 = (double) v2 / 1000.0;

		if (v1 <= 1000 && v1 >= 0) {
			a += v1;

			if (a > 1000) {
				long v4 = v2 - lastSyncHRClock;
				double v5 = (double) a / (double) v4;
				timeSyncAdjustment += (v5 - timeSyncAdjustment) * 0.20000000298023224;
				lastSyncHRClock = v2;
				a = 0;
			}

			if (a < 0) lastSyncHRClock = v2;
		} else lastHRTime = v3;

		lastSyncSysClock = v0;
		double v4 = (v3 - lastHRTime) * timeSyncAdjustment;
		lastHRTime = v3;

		if (v4 < 0.0) v4 = 0.0;
		if (v4 > 1.0) v4 = 1.0;

		elapsedPartialTicks = (float) ((double) elapsedPartialTicks + v4 * (double) timerSpeed * (double) ticksPerSecond);
		elapsedTicks = ((int) elapsedPartialTicks);
		elapsedPartialTicks -= (float) elapsedTicks;

		if (elapsedTicks > 10) elapsedTicks = 10;

		renderPartialTicks = elapsedPartialTicks;
	}
}
