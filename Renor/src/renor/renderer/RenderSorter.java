package renor.renderer;

import java.util.Comparator;

import renor.level.entity.EntityLiving;

public class RenderSorter implements Comparator<LevelRenderer> {
	private EntityLiving baseEntity;

	public RenderSorter(EntityLiving living) {
		baseEntity = living;
	}

	public int doCompare(LevelRenderer levelRenderer1, LevelRenderer levelRenderer2) {
		if (levelRenderer1.isInFrustrum && !levelRenderer2.isInFrustrum) return 1;
		else if (levelRenderer2.isInFrustrum && !levelRenderer1.isInFrustrum) return -1;
		else {
			double d1 = (double) levelRenderer1.distanceToEntitySquared(baseEntity);
			double d2 = (double) levelRenderer2.distanceToEntitySquared(baseEntity);
			return d1 < d2 ? 1 : (d1 > d2 ? -1 : (levelRenderer1.chunkIndex < levelRenderer2.chunkIndex ? 1 : -1));
		}
	}

	public int compare(LevelRenderer levelRenderer1, LevelRenderer levelRenderer2) {
		return doCompare(levelRenderer1, levelRenderer2);
	}
}
