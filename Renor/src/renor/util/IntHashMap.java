package renor.util;

import java.util.HashSet;
import java.util.Set;

public class IntHashMap {
	private Set<Integer> keySet = new HashSet<Integer>();
	private transient IntHashMapEntry[] slots = new IntHashMapEntry[16];
	private transient volatile int versionStamp;
	private transient int count;
	private final float growFactor = 0.75f;
	private int threshold = 12;

	private static int computeHash(int n) {
		n ^= n >>> 20 ^ n >>> 12;
		return n ^ n >>> 7 ^ n >>> 4;
	}

	private static int getSlotIndex(int par0, int par1) {
		return par0 & par1 - 1;
	}

	public Object lookup(int par1) {
		int var2 = computeHash(par1);

		for (IntHashMapEntry var3 = slots[getSlotIndex(var2, slots.length)]; var3 != null; var3 = var3.nextEntry)
			if (var3.hashEntry == par1) return var3.valueEntry;

		return null;
	}

	public boolean containsItem(int par1) {
		return lookupEntry(par1) != null;
	}

	final IntHashMapEntry lookupEntry(int par1) {
		int var2 = computeHash(par1);

		for (IntHashMapEntry var3 = slots[getSlotIndex(var2, slots.length)]; var3 != null; var3 = var3.nextEntry)
			if (var3.hashEntry == par1) return var3;

		return null;
	}

	public void addKey(int par1, Object par2Obj) {
		keySet.add(Integer.valueOf(par1));
		int var3 = computeHash(par1);
		int var4 = getSlotIndex(var3, slots.length);

		for (IntHashMapEntry var5 = slots[var4]; var5 != null; var5 = var5.nextEntry)
			if (var5.hashEntry == par1) {
				var5.valueEntry = par2Obj;
				return;
			}

		versionStamp++;
		insert(var3, par1, par2Obj, var4);
	}

	private void grow(int par1) {
		IntHashMapEntry[] var2 = slots;
		int var3 = var2.length;

		if (var3 == 1073741824) threshold = Integer.MAX_VALUE;
		else {
			IntHashMapEntry[] var4 = new IntHashMapEntry[par1];
			copyTo(var4);
			slots = var4;
			getClass();
			threshold = ((int) (par1 * 0.75f));
		}
	}

	private void copyTo(IntHashMapEntry[] par1ArrayOfIntHashMapEntry) {
		IntHashMapEntry[] var2 = slots;
		int var3 = par1ArrayOfIntHashMapEntry.length;

		for (int var4 = 0; var4 < var2.length; var4++) {
			IntHashMapEntry var5 = var2[var4];

			if (var5 != null) {
				var2[var4] = null;
				IntHashMapEntry var6;
				do {
					var6 = var5.nextEntry;
					int var7 = getSlotIndex(var5.slotHash, var3);
					var5.nextEntry = par1ArrayOfIntHashMapEntry[var7];
					par1ArrayOfIntHashMapEntry[var7] = var5;
					var5 = var6;
				} while (var6 != null);
			}
		}
	}

	public Object removeObject(int par1) {
		keySet.remove(Integer.valueOf(par1));
		IntHashMapEntry var2 = removeEntry(par1);
		return var2 == null ? null : var2.valueEntry;
	}

	final IntHashMapEntry removeEntry(int par1) {
		int var2 = computeHash(par1);
		int var3 = getSlotIndex(var2, slots.length);
		IntHashMapEntry var4 = slots[var3];
		IntHashMapEntry var5;
		IntHashMapEntry var6;
		for (var5 = var4; var5 != null; var5 = var6) {
			var6 = var5.nextEntry;

			if (var5.hashEntry == par1) {
				versionStamp++;
				count--;

				if (var4 == var5) slots[var3] = var6;
				else var4.nextEntry = var6;

				return var5;
			}

			var4 = var5;
		}

		return var5;
	}

	public void clearMap() {
		versionStamp++;
		IntHashMapEntry[] var1 = slots;

		for (int var2 = 0; var2 < var1.length; var2++)
			var1[var2] = null;

		count = 0;
	}

	private void insert(int par1, int par2, Object par3Obj, int par4) {
		IntHashMapEntry var5 = slots[par4];
		slots[par4] = new IntHashMapEntry(par1, par2, par3Obj, var5);

		if (count++ >= threshold) grow(slots.length * 2);
	}

	public Set<Integer> getKeySet() {
		return keySet;
	}

	static int getHash(int n) {
		return computeHash(n);
	}
}
