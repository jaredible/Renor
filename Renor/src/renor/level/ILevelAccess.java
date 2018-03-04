package renor.level;

public abstract interface ILevelAccess {
	void markBlockForUpdate(int x, int y, int z);

	void markBlockForRenderUpdate(int x, int y, int z);

	void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

	void playSound(String name, float x, float y, float z, float volume, float pitch);

	void spawnParticle(String name, double x, double y, double z, double mx, double my, double mz);

	void onStaticEntitiesChanged();
}
