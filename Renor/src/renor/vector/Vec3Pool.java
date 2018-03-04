package renor.vector;

import java.util.ArrayList;
import java.util.List;

public class Vec3Pool {
	private final List<Vec3> vec3Cache = new ArrayList<Vec3>();
	private final int truncateArrayResetThreshold;
	private final int minimumSize;
	private int nextFreeSpace = 0;
	private int maximumSizeSinceLastTruncation = 0;
	private int resetCount = 0;

	public Vec3Pool(int par1, int par2) {
		truncateArrayResetThreshold = par1;
		minimumSize = par2;
	}

	public Vec3 getVecFromPool(double xCoord, double yCoord, double zCoord) {
		if (b()) return new Vec3(this, xCoord, yCoord, zCoord);
		else {
			Vec3 vec3;
			if (nextFreeSpace >= vec3Cache.size()) {
				vec3 = new Vec3(this, xCoord, yCoord, zCoord);
				vec3Cache.add(vec3);
			} else {
				vec3 = vec3Cache.get(nextFreeSpace);
				vec3.setComponents(xCoord, yCoord, zCoord);
			}

			++nextFreeSpace;
			return vec3;
		}
	}

	public void clear() {
		if (!b()) {
			if (nextFreeSpace > maximumSizeSinceLastTruncation) maximumSizeSinceLastTruncation = nextFreeSpace;

			if (resetCount++ == truncateArrayResetThreshold) {
				int n = Math.max(maximumSizeSinceLastTruncation, vec3Cache.size() - minimumSize);

				while (vec3Cache.size() > n)
					vec3Cache.remove(n);

				maximumSizeSinceLastTruncation = 0;
				resetCount = 0;
			}

			nextFreeSpace = 0;
		}
	}

	public void clearAndFreeCache() {
		if (!b()) {
			nextFreeSpace = 0;
			vec3Cache.clear();
		}
	}

	public int getPoolSize() {
		return vec3Cache.size();
	}

	public int a() {
		return nextFreeSpace;
	}

	private boolean b() {
		return minimumSize < 0 || truncateArrayResetThreshold < 0;
	}
}
