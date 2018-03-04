package renor.level;

public class LevelManager implements ILevelAccess {
	public void markBlockForUpdate(int x, int y, int z) {
	}

	public void markBlockForRenderUpdate(int x, int y, int z) {
	}

	public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
	}

	public void playSound(String name, float x, float y, float z, float volume, float pitch) {
	}

	public void spawnParticle(String name, double x, double y, double z, double mx, double my, double mz) {
	}

	public void onStaticEntitiesChanged() {
	}
}
