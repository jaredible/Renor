package renor.aabb;

import java.util.ArrayList;
import java.util.List;

public class AABBPool {
	private final List<AxisAlignedBB> listAABB = new ArrayList<AxisAlignedBB>();
	private final int maxNumCleans;
	private final int numEntriesToRemove;
	private int nextPoolIndex;
	private int maxPoolIndex;
	private int numCleans;

	public AABBPool(int par0, int par1) {
		maxNumCleans = par0;
		numEntriesToRemove = par1;
	}

	public AxisAlignedBB getAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		AxisAlignedBB aabb;

		if (nextPoolIndex >= listAABB.size()) {
			aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
			listAABB.add(aabb);
		} else {
			aabb = listAABB.get(nextPoolIndex);
			aabb.setBounds(minX, minY, minZ, maxX, maxY, maxZ);
		}

		++nextPoolIndex;
		return aabb;
	}

	public void cleanPool() {
		if (nextPoolIndex > maxPoolIndex) maxPoolIndex = nextPoolIndex;

		if (numCleans++ == maxNumCleans) {
			int n = Math.max(maxPoolIndex, listAABB.size() - numEntriesToRemove);

			while (listAABB.size() > n)
				listAABB.remove(n);

			maxPoolIndex = 0;
			numCleans = 0;
		}

		nextPoolIndex = 0;
	}

	public void clearPool() {
		nextPoolIndex = 0;
		listAABB.clear();
	}

	public int getlistAABBsize() {
		return listAABB.size();
	}

	public int getnextPoolIndex() {
		return nextPoolIndex;
	}
}
