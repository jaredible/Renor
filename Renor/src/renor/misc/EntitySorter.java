package renor.misc;

import java.util.Comparator;

import renor.level.entity.Entity;
import renor.renderer.LevelRenderer;

public class EntitySorter implements Comparator<LevelRenderer> {
	private double entityPosX;
	private double entityPosY;
	private double entityPosZ;

	public EntitySorter(Entity entity) {
		entityPosX = -entity.posX;
		entityPosY = -entity.posY;
		entityPosZ = -entity.posZ;
	}

	public int sortByDistanceToEntity(LevelRenderer levelRenderer1, LevelRenderer levelRenderer2) {
		double x1 = (double) levelRenderer1.posXPlus + entityPosX;
		double y1 = (double) levelRenderer1.posYPlus + entityPosY;
		double z1 = (double) levelRenderer1.posZPlus + entityPosZ;
		double x2 = (double) levelRenderer2.posXPlus + entityPosX;
		double y2 = (double) levelRenderer2.posYPlus + entityPosY;
		double z2 = (double) levelRenderer2.posZPlus + entityPosZ;
		return (int) ((x1 * x1 + y1 * y1 + z1 * z1 - (x2 * x2 + y2 * y2 + z2 * z2)) * 1024.0);
	}

	public int compare(LevelRenderer levelRenderer1, LevelRenderer levelRenderer2) {
		return sortByDistanceToEntity(levelRenderer1, levelRenderer2);
	}
}
