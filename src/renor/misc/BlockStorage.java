package renor.misc;

public class BlockStorage {
	private NibbleArray blockMSBArray;
	private NibbleArray blockMetadataArray;
	private NibbleArray blockLightArray;
	private NibbleArray skyLightArray;
	private int yBase;
	private int blockRefCount;
	private byte[] blockLSBArray;

	public BlockStorage(int yBase) {
		this.yBase = yBase;

		blockLSBArray = new byte[4096];
		blockMSBArray = new NibbleArray(blockLSBArray.length, 4);
		blockMetadataArray = new NibbleArray(blockLSBArray.length, 4);
		blockLightArray = new NibbleArray(blockLSBArray.length, 4);
		skyLightArray = new NibbleArray(blockLSBArray.length, 4);
	}

	public int getBlockId(int x, int y, int z) {
		int n = blockLSBArray[y << 8 | z << 4 | x] & 255;
		return blockMSBArray != null ? blockMSBArray.get(x, y, z) << 8 | n : n;
	}

	public void setBlockId(int x, int y, int z, int value) {
		int oldValue = blockLSBArray[y << 8 | z << 4 | x];

		if (blockMSBArray != null) oldValue |= blockMSBArray.get(x, y, z) << 8;

		if (oldValue == 0 && value != 0) {
			++blockRefCount;

			// do other stuff here
		} else if (oldValue != 0 && value == 0) {
			--blockRefCount;

			// do other stuff here
		}

		blockLSBArray[y << 8 | z << 4 | x] = (byte) (value & 255);

		if (value > 255) {
			if (blockMSBArray == null) blockMSBArray = new NibbleArray(blockLSBArray.length, 4);

			blockMSBArray.set(x, y, z, (value & 3840) >> 8);
		} else if (blockMSBArray != null) blockMSBArray.set(x, y, z, 0);
	}

	public int getBlockMetadata(int x, int y, int z) {
		return blockMetadataArray.get(x, y, z);
	}

	public void setBlockMetadata(int x, int y, int z, int value) {
		blockMetadataArray.set(x, y, z, value);
	}

	public int getBlockLightValue(int x, int y, int z) {
		return blockLightArray.get(x, y, z);
	}

	public void setBlockLightValue(int x, int y, int z, int value) {
		blockLightArray.set(x, y, z, value);
	}

	public int getSkyLightValue(int x, int y, int z) {
		return skyLightArray.get(x, y, z);
	}

	public void setSkyLightValue(int x, int y, int z, int value) {
		skyLightArray.set(x, y, z, value);
	}

	public int getYLocation() {
		return yBase;
	}

	public boolean isEmpty() {
		return blockRefCount == 0;
	}
}
