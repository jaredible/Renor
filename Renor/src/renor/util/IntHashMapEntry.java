package renor.util;

class IntHashMapEntry {
	public Object valueEntry;
	public IntHashMapEntry nextEntry;
	public final int hashEntry;
	public final int slotHash;

	public IntHashMapEntry(int slotHash, int hashEntry, Object valueEntry, IntHashMapEntry par4IntHashMapEntry) {
		this.slotHash = slotHash;
		this.hashEntry = hashEntry;
		this.valueEntry = valueEntry;
		nextEntry = par4IntHashMapEntry;
	}

	public final int getHash() {
		return hashEntry;
	}

	public final Object getValue() {
		return valueEntry;
	}

	public final boolean equals(Object par1Obj) {
		if (!(par1Obj instanceof IntHashMapEntry)) return false;

		IntHashMapEntry var2 = (IntHashMapEntry) par1Obj;
		Integer var3 = Integer.valueOf(getHash());
		Integer var4 = Integer.valueOf(var2.getHash());

		if (var3 == var4 || (var3 != null) && (var3.equals(var4))) {
			Object var5 = getValue();
			Object var6 = var2.getValue();

			if ((var5 == var6) || ((var5 != null) && (var5.equals(var6)))) return true;
		}

		return false;
	}

	public final int hashCode() {
		return IntHashMap.getHash(hashEntry);
	}

	public final String toString() {
		return getHash() + "=" + getValue();
	}
}
