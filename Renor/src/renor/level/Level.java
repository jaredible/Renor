package renor.level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import renor.Renor;
import renor.aabb.AxisAlignedBB;
import renor.level.block.Block;
import renor.level.block.IBlockAccess;
import renor.level.chunk.Chunk;
import renor.level.chunk.ChunkCoordIntPair;
import renor.level.chunk.IChunkProvider;
import renor.level.entity.Entity;
import renor.level.entity.EntityPlayer;
import renor.level.item.ItemStack;
import renor.level.material.Material;
import renor.misc.GameRules;
import renor.misc.MovingObjectPosition;
import renor.util.EnumLightValue;
import renor.util.Facing;
import renor.util.MathHelper;
import renor.util.crashreport.CrashReport;
import renor.util.logger.ILogAgent;
import renor.util.profiler.Profiler;
import renor.util.throwable.ReportedException;
import renor.vector.Vec3;
import renor.vector.Vec3Pool;

public abstract class Level implements IBlockAccess {
	public Random rand = new Random();
	public List<ILevelAccess> levelAccesses = new ArrayList<ILevelAccess>();
	public List<Entity> loadedEntityList = new ArrayList<Entity>();
	public List<Entity> unloadedEntityList = new ArrayList<Entity>();
	public List<EntityPlayer> playerEntities = new ArrayList<EntityPlayer>();
	protected Set<ChunkCoordIntPair> activeChunkSet = new HashSet<ChunkCoordIntPair>();
	private ArrayList<AxisAlignedBB> collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();
	public final LevelProvider provider;
	protected IChunkProvider chunkProvider;
	private final Vec3Pool vec3Pool = new Vec3Pool(300, 2000);
	public final Profiler theProfiler;
	private final ILogAgent levelLogAgent;
	private final Renor renor = Renor.getRenor();
	protected LevelInfo levelInfo;
	public int skylightSubtracted = 0;
	int[] lightUpdateBlockList;
	public boolean isClient;

	public Level(Profiler profiler, ILogAgent logAgent) {
		theProfiler = profiler;
		levelLogAgent = logAgent;

		provider = new LevelProvider();
		provider.registerLevel(this);
		chunkProvider = createChunkProvider();
		levelInfo = new LevelInfo();
		lightUpdateBlockList = new int[32768];
		isClient = false;

		calculateInitialSkylight();
	}

	protected abstract IChunkProvider createChunkProvider();

	public void updateEntities() {
		theProfiler.startSection("entities");
		// chunkX
		int cx;
		// chunkZ
		int cz;

		theProfiler.endStartSection("regular");

		for (int i = 0; i < loadedEntityList.size(); ++i) {
			Entity e = loadedEntityList.get(i);

			theProfiler.startSection("tick");

			if (!e.isDead) {
				try {
					updateEntity(e);
				} catch (Throwable e1) {
					CrashReport crashReport = CrashReport.makeCrashReport(e1, "Ticking entity");
					throw new ReportedException(crashReport);
				}
			}

			theProfiler.endStartSection("remove");

			if (e.isDead) {
				cx = e.chunkCoordX;
				cz = e.chunkCoordZ;

				if (e.addedToChunk && chunkExists(cx, cz)) getChunkFromChunkCoords(cx, cz).removeEntity(e);

				loadedEntityList.remove(i--);
			}

			theProfiler.endSection();
		}

		theProfiler.endSection();
		theProfiler.endSection();
	}

	public void updateEntity(Entity entity) {
		updateEntityWithOptionForce(entity, true);
	}

	public void updateEntityWithOptionForce(Entity entity, boolean forceUpdate) {
		int x = MathHelper.floor_double(entity.posX / 16.0);
		int z = MathHelper.floor_double(entity.posZ / 16.0);
		byte r = 32;

		if (!forceUpdate || checkChunksExist(x - r, 0, z - r, x + r, 0, z + r)) {
			entity.lastTickPosX = entity.posX;
			entity.lastTickPosY = entity.posY;
			entity.lastTickPosZ = entity.posZ;
			entity.prevRotationYaw = entity.rotationYaw;
			entity.prevRotationPitch = entity.rotationPitch;

			if (forceUpdate && entity.addedToChunk) {
				++entity.ticksExisted;
				entity.onUpdate();
			}

			theProfiler.startSection("chunkCheck");

			if (Double.isNaN(entity.posX) || Double.isInfinite(entity.posX)) entity.posX = entity.lastTickPosX;
			if (Double.isNaN(entity.posY) || Double.isInfinite(entity.posY)) entity.posY = entity.lastTickPosY;
			if (Double.isNaN(entity.posZ) || Double.isInfinite(entity.posZ)) entity.posZ = entity.lastTickPosZ;
			if (Double.isNaN((double) entity.rotationYaw) || Double.isInfinite((double) entity.rotationYaw)) entity.rotationYaw = entity.prevRotationYaw;
			if (Double.isNaN((double) entity.rotationPitch) || Double.isInfinite((double) entity.rotationPitch)) entity.rotationPitch = entity.prevRotationPitch;

			int xx = MathHelper.floor_double(entity.posX / 16.0);
			int yy = MathHelper.floor_double(entity.posY / 16.0);
			int zz = MathHelper.floor_double(entity.posZ / 16.0);

			if (!entity.addedToChunk || entity.chunkCoordX != xx || entity.chunkCoordY != yy || entity.chunkCoordZ != zz) {
				if (entity.addedToChunk && chunkExists(entity.chunkCoordX, entity.chunkCoordZ)) getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(entity, entity.chunkCoordY);

				if (chunkExists(xx, zz)) {
					entity.addedToChunk = true;
					getChunkFromChunkCoords(xx, zz).addEntity(entity);
				} else entity.addedToChunk = false;
			}

			theProfiler.endSection();
		}
	}

	public void playSound(double x, double y, double z, String name, float volume, float pitch) {
	}

	public void spawnParticle(String name, double x, double y, double z, double mx, double my, double mz) {
		for (int i = 0; i < levelAccesses.size(); ++i)
			levelAccesses.get(i).spawnParticle(name, x, y, z, mx, my, mz);
	}

	public boolean spawnEntityInLevel(Entity entity) {
		int x = MathHelper.floor_double(entity.posX / 16.0);
		int z = MathHelper.floor_double(entity.posZ / 16.0);
		boolean forceSpawn = false;

		if (entity instanceof EntityPlayer) forceSpawn = true;

		if (!forceSpawn && !chunkExists(x, z)) return false;
		else {
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				playerEntities.add(player);
			}

			getChunkFromChunkCoords(x, z).addEntity(entity);
			loadedEntityList.add(entity);
			return true;
		}
	}

	public void removeEntity(Entity entity) {
		entity.setDead();
	}

	public void addLevelAccess(ILevelAccess levelAccess) {
		levelAccesses.add(levelAccess);
	}

	public void removeLevelAccess(ILevelAccess levelAccess) {
		levelAccesses.remove(levelAccess);
	}

	public Vec3 getSkyColor(Entity entity, float partialTickTime) {
		float a = getCelestialAngle(partialTickTime);
		float n = MathHelper.cos(a * (float) Math.PI * 2.0f) * 2.0f + 0.5f;

		if (n < 0.0f) n = 0.0f;

		if (n > 1.0f) n = 1.0f;

		int col = -8804353;
		float r = (float) (col >> 16 & 255) / 255.0f;
		float g = (float) (col >> 8 & 255) / 255.0f;
		float b = (float) (col & 255) / 255.0f;
		r *= n;
		g *= n;
		b *= n;

		return getLevelVec3Pool().getVecFromPool((double) r, (double) g, (double) b);
	}

	public Vec3 getFogColor(float partialTickTime) {
		float a = getCelestialAngle(partialTickTime);
		return provider.getFogColor(a, partialTickTime);
	}

	public List<Entity> getLoadedEntityList() {
		return loadedEntityList;
	}

	public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entity, AxisAlignedBB axisAlignedBB) {
		collidingBoundingBoxes.clear();
		int minX = MathHelper.floor_double(axisAlignedBB.minX);
		int maxX = MathHelper.floor_double(axisAlignedBB.maxX + 1.0);
		int minY = MathHelper.floor_double(axisAlignedBB.minY);
		int maxY = MathHelper.floor_double(axisAlignedBB.maxY + 1.0);
		int minZ = MathHelper.floor_double(axisAlignedBB.minZ);
		int maxZ = MathHelper.floor_double(axisAlignedBB.maxZ + 1.0);

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				if (blockExists(x, 64, z)) {
					for (int y = minY - 1; y < maxY; ++y) {
						// block
						Block b;

						if (x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000) b = Block.blocksList[getBlockId(x, y, z)];
						else b = Block.grass;

						if (b != null) b.addCollisionBoxesToList(this, x, y, z, axisAlignedBB, collidingBoundingBoxes);
					}
				}
			}
		}

		return collidingBoundingBoxes;
	}

	public List<AxisAlignedBB> getCollidingBlockBounds(AxisAlignedBB axisAlignedBB) {
		collidingBoundingBoxes.clear();
		int minX = MathHelper.floor_double(axisAlignedBB.minX);
		int maxX = MathHelper.floor_double(axisAlignedBB.maxX + 1.0);
		int minY = MathHelper.floor_double(axisAlignedBB.minY);
		int maxY = MathHelper.floor_double(axisAlignedBB.maxY + 1.0);
		int minZ = MathHelper.floor_double(axisAlignedBB.minZ);
		int maxZ = MathHelper.floor_double(axisAlignedBB.maxZ + 1.0);

		for (int x = minX; x < maxX; ++x) {
			for (int z = minZ; z < maxZ; ++z) {
				if (blockExists(x, 64, z)) {
					for (int y = minY - 1; y < maxY; ++y) {
						// block
						Block b;

						if (x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000) b = Block.blocksList[getBlockId(x, y, z)];
						else b = Block.grass;

						if (b != null) b.addCollisionBoxesToList(this, x, y, z, axisAlignedBB, collidingBoundingBoxes);
					}
				}
			}
		}

		return collidingBoundingBoxes;
	}

	public String getDebugLoadedEntities() {
		return "All: " + loadedEntityList.size();
	}

	public String getProviderName() {
		return chunkProvider.makeString();
	}

	public int getBlockId(int x, int y, int z) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (y < 0) return 0;
			else if (y >= 256) return 0;
			else {
				// chunk
				Chunk c;

				try {
					c = getChunkFromChunkCoords(x >> 4, z >> 4);
					return c.getBlockId(x & 15, y, z & 15);
				} catch (Throwable e) {
					CrashReport crashReport = CrashReport.makeCrashReport(e, "Exception getting block type in level");
					throw new ReportedException(crashReport);
				}
			}
		} else return 0;
	}

	public int getBlockMetadata(int x, int y, int z) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (y < 0) return 0;
			else if (y >= 256) return 0;
			else {
				// chunk
				Chunk c = getChunkFromChunkCoords(x >> 4, z >> 4);
				x &= 15;
				z &= 15;
				return c.getBlockMetadata(x, y, z);
			}
		} else return 0;
	}

	public Material getBlockMaterial(int x, int y, int z) {
		// blockId
		int id = getBlockId(x, y, z);
		return id == 0 ? Material.air : Block.blocksList[id].blockMaterial;
	}

	public boolean isAirBlock(int x, int y, int z) {
		return getBlockId(x, y, z) == 0;
	}

	public float getBrightness(int x, int y, int z, int opacity) {
		int n = getBlockLightValue(x, y, z);

		if (n < opacity) n = opacity;

		return provider.lightBrightnessTable[n];
	}

	public float getLightBrightness(int x, int y, int z) {
		return provider.lightBrightnessTable[getBlockLightValue(x, y, z)];
	}

	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int opacity) {
		int v0 = getSkyBlockTypeBrightness(EnumLightValue.Sky, x, y, z);
		int v1 = getSkyBlockTypeBrightness(EnumLightValue.Block, x, y, z);

		if (v1 < opacity) v1 = opacity;

		return v0 << 20 | v1 << 4;
	}

	public boolean isBlockOpaque(int x, int y, int z) {
		// block
		Block b = Block.blocksList[getBlockId(x, y, z)];
		return b == null ? false : b.isOpaque();
	}

	public int getHeight() {
		return 256;
	}

	public Vec3Pool getLevelVec3Pool() {
		return vec3Pool;
	}

	public int getSkyBlockTypeBrightness(EnumLightValue enumLightValue, int x, int y, int z) {
		if (y < 0) y = 0;

		if (y >= 256) return enumLightValue.defaultLightValue;
		else if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			int xx = x >> 4;
			int zz = z >> 4;

			if (!chunkExists(xx, zz)) return enumLightValue.defaultLightValue;
			else if (Block.useNeighborBrightness[getBlockId(x, y, z)]) {
				int var0 = getSavedLightValue(enumLightValue, x, y + 1, z);
				int var1 = getSavedLightValue(enumLightValue, x + 1, y, z);
				int var2 = getSavedLightValue(enumLightValue, x - 1, y, z);
				int var3 = getSavedLightValue(enumLightValue, x, y, z + 1);
				int var4 = getSavedLightValue(enumLightValue, x, y, z - 1);

				if (var1 > var0) var0 = var1;

				if (var2 > var0) var0 = var2;

				if (var3 > var0) var0 = var3;

				if (var4 > var0) var0 = var4;

				return var0;
			} else {
				// chunk
				Chunk c = getChunkFromChunkCoords(xx, zz);
				return c.getSavedLightValue(enumLightValue, x & 15, y, z & 15);
			}
		} else return enumLightValue.defaultLightValue;
	}

	public Chunk getChunkFromChunkCoords(int x, int z) {
		return chunkProvider.provideChunk(x, z);
	}

	public Chunk getChunkFromBlockCoords(int x, int z) {
		return getChunkFromChunkCoords(x >> 4, z >> 4);
	}

	public void markBlockForUpdate(int x, int y, int z) {
		for (int i = 0; i < levelAccesses.size(); ++i)
			levelAccesses.get(i).markBlockForUpdate(x, y, z);
	}

	public void markBlockForRenderUpdate(int x, int y, int z) {
		for (int i = 0; i < levelAccesses.size(); ++i)
			levelAccesses.get(i).markBlockForRenderUpdate(x, y, z);
	}

	public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		for (int i = 0; i < levelAccesses.size(); ++i)
			levelAccesses.get(i).markBlockRangeForRenderUpdate(minX, minY, minZ, maxX, maxY, maxZ);
	}

	protected boolean chunkExists(int x, int z) {
		return chunkProvider.chunkExists(x, z);
	}

	public boolean checkChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (maxY >= 0 && minY < 256) {
			minX >>= 4;
			minZ >>= 4;
			maxX >>= 4;
			maxZ >>= 4;

			for (int x = minX; x <= maxX; ++x)
				for (int z = minZ; z <= maxZ; ++z)
					if (!chunkExists(x, z)) return false;

			return true;
		} else return false;
	}

	public boolean blockExists(int x, int y, int z) {
		return y >= 0 && y < 256 ? chunkExists(x >> 4, z >> 4) : false;
	}

	public boolean doChunksNearChunkExist(int x, int y, int z, int r) {
		return checkChunksExist(x - r, y - r, z - r, x + r, y + r, z + r);
	}

	public int getBlockLightValue(int x, int y, int z) {
		return getBlockLightValue_do(x, y, z, true);
	}

	public int getBlockLightValue_do(int x, int y, int z, boolean flag) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (flag) {
				// blockId
				int id = getBlockId(x, y, z);

				if (Block.useNeighborBrightness[id]) {
					int v0 = getBlockLightValue_do(x, y + 1, z, false);
					int v1 = getBlockLightValue_do(x + 1, y, z, false);
					int v2 = getBlockLightValue_do(x - 1, y, z, false);
					int v3 = getBlockLightValue_do(x, y, z + 1, false);
					int v4 = getBlockLightValue_do(x, y, z - 1, false);

					if (v1 > v0) v0 = v1;

					if (v2 > v0) v0 = v2;

					if (v3 > v0) v0 = v3;

					if (v4 > v0) v0 = v4;

					return v0;
				}
			}

			if (y < 0) return 0;
			else {
				if (y >= 256) y = 255;

				// chunk
				Chunk c = getChunkFromChunkCoords(x >> 4, z >> 4);
				x &= 15;
				z &= 15;
				return c.getBlockLightValue(x, y, z, skylightSubtracted);
			}
		} else return 15;
	}

	public void tick() {
	}

	protected void tickBlocksAndAmbiance() {
		setActivePlayerChunksAndCheckLight();
	}

	// TODO check this!
	protected void setActivePlayerChunksAndCheckLight() {
		// activeChunkSet.clear();
		theProfiler.startSection("buildList");
		int v0;
		// player
		EntityPlayer p;
		int v1;
		int v2;

		for (v0 = 0; v0 < playerEntities.size(); ++v0) {
			p = playerEntities.get(v0);
			v1 = MathHelper.floor_double(p.posX / 16.0);
			v2 = MathHelper.floor_double(p.posZ / 16.0);
			// byte r = 7;

			// for (int x = -r; x <= r; ++x)
			// for (int z = -r; z <= r; ++z)
			// activeChunkSet.add(new ChunkCoordIntPair(x + v1, z + v2));
		}

		theProfiler.endStartSection("playerCheckLight");

		if (!playerEntities.isEmpty()) {
			v0 = rand.nextInt(playerEntities.size());
			p = playerEntities.get(v0);
			v1 = MathHelper.floor_double(p.posX) + rand.nextInt(11) - 5;
			v2 = MathHelper.floor_double(p.posY) + rand.nextInt(11) - 5;
			int var3 = MathHelper.floor_double(p.posZ) + rand.nextInt(11) - 5;
			updateAllLightTypes(v1, v2, var3);
		}

		theProfiler.endSection();
	}

	public ILogAgent getLevelLogAgent() {
		return levelLogAgent;
	}

	public boolean setBlock(int x, int y, int z, int id, int metadata, int flag) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (y < 0) return false;
			else if (y >= 256) return false;
			else {
				// chunk
				Chunk c = getChunkFromChunkCoords(x >> 4, z >> 4);

				boolean v0 = c.setBlockIdWithMetadata(x & 15, y, z & 15, id, metadata);
				theProfiler.startSection("checkLight");
				updateAllLightTypes(x, y, z);
				theProfiler.endSection();

				if (v0) {
					if ((flag & 2) != 0 && (!isClient || (flag & 4) == 0)) markBlockForUpdate(x, y, z);

					// do other stuff here
				}

				return v0;
			}
		} else return false;
	}

	public boolean setBlockToAir(int x, int y, int z) {
		return setBlock(x, y, z, 0, 0, 3);
	}

	public MovingObjectPosition rayTraceBlocks(Vec3 vec1, Vec3 vec2) {
		return rayTraceBlocks_do_do(vec1, vec2, false, false);
	}

	public MovingObjectPosition rayTraceBlocks_do(Vec3 vec1, Vec3 vec2, boolean flag) {
		return rayTraceBlocks_do_do(vec1, vec2, flag, false);
	}

	// TODO check this!
	public MovingObjectPosition rayTraceBlocks_do_do(Vec3 vec1, Vec3 vec2, boolean p0, boolean p1) {
		if (!Double.isNaN(vec1.xCoord) && !Double.isNaN(vec1.yCoord) && !Double.isNaN(vec1.zCoord)) {
			if (!Double.isNaN(vec2.xCoord) && !Double.isNaN(vec2.yCoord) && !Double.isNaN(vec2.zCoord)) {
				int x2 = MathHelper.floor_double(vec2.xCoord);
				int y2 = MathHelper.floor_double(vec2.yCoord);
				int z2 = MathHelper.floor_double(vec2.zCoord);
				int x1 = MathHelper.floor_double(vec1.xCoord);
				int y1 = MathHelper.floor_double(vec1.yCoord);
				int z1 = MathHelper.floor_double(vec1.zCoord);
				// blockId
				int id = getBlockId(x1, y1, z1);
				// metadata
				int md = getBlockMetadata(x1, y1, z1);
				// block
				Block b = Block.blocksList[id];

				if ((!p1 || b == null || b.getCollisionBoundingBoxFromPool(this, x1, y1, z1) != null) && id > 0) {
					MovingObjectPosition v0 = b.collisionRayTrace(this, x1, y1, z1, vec1, vec2);

					if (v0 != null) return v0;
				}

				id = 200;

				while (id-- >= 0) {
					if (Double.isNaN(vec1.xCoord) || Double.isNaN(vec1.yCoord) || Double.isNaN(vec1.zCoord)) return null;

					if (x1 == x2 && y1 == y2 && z1 == z2) return null;

					boolean v0 = true;
					boolean v1 = true;
					boolean v2 = true;
					double v3 = 999.0;
					double v4 = 999.0;
					double v5 = 999.0;

					if (x2 > x1) v3 = (double) x1 + 1.0;
					else if (x2 < x1) v3 = (double) x1 + 0.0;
					else v0 = false;

					if (y2 > y1) v4 = (double) y1 + 1.0;
					else if (y2 < y1) v4 = (double) y1 + 0.0;
					else v1 = false;

					if (z2 > z1) v5 = (double) z1 + 1.0;
					else if (z2 < z1) v5 = (double) z1 + 0.0;
					else v2 = false;

					double v6 = 999.0;
					double v7 = 999.0;
					double v8 = 999.0;
					double v9 = vec2.xCoord - vec1.xCoord;
					double v10 = vec2.yCoord - vec1.yCoord;
					double v11 = vec2.zCoord - vec1.zCoord;

					if (v0) v6 = (v3 - vec1.xCoord) / v9;

					if (v1) v7 = (v4 - vec1.yCoord) / v10;

					if (v2) v8 = (v5 - vec1.zCoord) / v11;

					boolean v12 = false;
					byte side;

					if (v6 < v7 && v6 < v8) {
						if (x2 > x1) side = 4;
						else side = 5;

						vec1.xCoord = v3;
						vec1.yCoord += v10 * v6;
						vec1.zCoord += v11 * v6;
					} else if (v7 < v8) {
						if (y2 > y1) side = 0;
						else side = 1;

						vec1.xCoord += v9 * v7;
						vec1.yCoord = v4;
						vec1.zCoord += v11 * v7;
					} else {
						if (z2 > z1) side = 2;
						else side = 3;

						vec1.xCoord += v9 * v8;
						vec1.yCoord += v10 * v8;
						vec1.zCoord = v5;
					}

					Vec3 v13 = getLevelVec3Pool().getVecFromPool(vec1.xCoord, vec1.yCoord, vec1.zCoord);
					x1 = (int) (v13.xCoord = (double) MathHelper.floor_double(vec1.xCoord));

					if (side == 5) {
						--x1;
						++v13.xCoord;
					}

					y1 = (int) (v13.yCoord = (double) MathHelper.floor_double(vec1.yCoord));

					if (side == 1) {
						--y1;
						++v13.yCoord;
					}

					z1 = (int) (v13.zCoord = (double) MathHelper.floor_double(vec1.zCoord));

					if (side == 3) {
						--z1;
						++v13.zCoord;
					}

					int v14 = getBlockId(x1, y1, z1);
					int v15 = getBlockMetadata(x1, y1, z1);
					Block v16 = Block.blocksList[v14];

					if ((!p1 || v16 == null || v16.getCollisionBoundingBoxFromPool(this, x1, y1, z1) != null) && v14 > 0) {
						MovingObjectPosition v17 = v16.collisionRayTrace(this, x1, y1, z1, vec1, vec2);

						if (v17 != null) return v17;
					}
				}

				return null;
			} else return null;
		} else return null;
	}

	private int computeLightValue(int x, int y, int z, EnumLightValue enumLightValue) {
		if (enumLightValue == EnumLightValue.Sky && canBlockSeeTheSky(x, y, z)) return 15;
		else {
			int id = getBlockId(x, y, z);
			// lightValue
			int lv = enumLightValue == EnumLightValue.Sky ? 0 : Block.lightValue[id];
			int opacity = Block.lightOpacity[id];

			if (opacity >= 15 && Block.lightValue[id] > 0) opacity = 1;

			if (opacity < 1) opacity = 1;

			if (opacity >= 15) return 0;
			else if (lv >= 14) return lv;
			else {
				for (int i = 0; i < 6; ++i) {
					int xo = x + Facing.offsetsXForSide[i];
					int yo = y + Facing.offsetsYForSide[i];
					int zo = z + Facing.offsetsZForSide[i];
					// savedLightValue
					int slv = getSavedLightValue(enumLightValue, xo, yo, zo) - opacity;

					if (slv > lv) lv = slv;

					if (lv >= 14) return lv;
				}

				return lv;
			}
		}
	}

	public void markBlocksDirtyVertical(int x, int z, int minY, int maxY) {
		int y;

		if (minY > maxY) {
			y = maxY;
			maxY = minY;
			minY = y;
		}

		for (y = minY; y <= maxY; ++y)
			updateLightByType(EnumLightValue.Sky, x, y, z);

		markBlockRangeForRenderUpdate(x, minY, z, x, maxY, z);
	}

	public void updateLightByType(EnumLightValue enumLightValue, int x, int y, int z) {
		if (doChunksNearChunkExist(x, y, z, 17)) {
			int v0 = 0;
			int v1 = 0;
			theProfiler.startSection("getBrightness");
			int v2 = getSavedLightValue(enumLightValue, x, y, z);
			int v3 = computeLightValue(x, y, z, enumLightValue);
			int v4;
			int xx;
			int yy;
			int zz;
			int v5;
			int v6;
			int v7;
			int v8;
			int v9;

			if (v3 > v2) {
				lightUpdateBlockList[v1++] = 133152;
			} else if (v3 < v2) {
				lightUpdateBlockList[v1++] = 133152 | v2 << 18;

				while (v0 < v1) {
					v4 = lightUpdateBlockList[v0++];
					xx = (v4 & 63) - 32 + x;
					yy = (v4 >> 6 & 63) - 32 + y;
					zz = (v4 >> 12 & 63) - 32 + z;
					v5 = v4 >> 18 & 15;
					v6 = getSavedLightValue(enumLightValue, xx, yy, zz);

					if (v6 == v5) {
						setLightValue(enumLightValue, xx, yy, zz, 0);

						if (v5 > 0) {
							v7 = MathHelper.abs_int(xx - x);
							v9 = MathHelper.abs_int(yy - y);
							v8 = MathHelper.abs_int(zz - z);

							if (v7 + v9 + v8 < 17) {
								for (int side = 0; side < 6; ++side) {
									int xo = xx + Facing.offsetsXForSide[side];
									int yo = yy + Facing.offsetsYForSide[side];
									int zo = zz + Facing.offsetsZForSide[side];
									int v10 = Math.max(1, Block.lightOpacity[getBlockId(xo, yo, zo)]);
									v6 = getSavedLightValue(enumLightValue, xo, yo, zo);

									if (v6 == v5 - v10 && v1 < lightUpdateBlockList.length) lightUpdateBlockList[v1++] = xo - x + 32 | yo - y + 32 << 6 | zo - z + 32 << 12 | v5 - v10 << 18;
								}
							}
						}
					}
				}

				v0 = 0;
			}

			theProfiler.endStartSection("checkedPosition < toCheckCount");

			while (v0 < v1) {
				v4 = lightUpdateBlockList[v0++];
				xx = (v4 & 63) - 32 + x;
				yy = (v4 >> 6 & 63) - 32 + y;
				zz = (v4 >> 12 & 63) - 32 + z;
				v5 = getSavedLightValue(enumLightValue, xx, yy, zz);
				v6 = computeLightValue(xx, yy, zz, enumLightValue);

				if (v6 != v5) {
					setLightValue(enumLightValue, xx, yy, zz, v6);

					if (v6 > v5) {
						v7 = Math.abs(xx - x);
						v9 = Math.abs(yy - y);
						v8 = Math.abs(zz - z);
						boolean v10 = v1 < lightUpdateBlockList.length - 6;

						if (v7 + v9 + v8 < 17 && v10) {
							if (getSavedLightValue(enumLightValue, xx - 1, yy, zz) < v6) lightUpdateBlockList[v1++] = xx - 1 - x + 32 + (yy - y + 32 << 6) + (zz - z + 32 << 12);

							if (getSavedLightValue(enumLightValue, xx + 1, yy, zz) < v6) lightUpdateBlockList[v1++] = xx + 1 - x + 32 + (yy - y + 32 << 6) + (zz - z + 32 << 12);

							if (getSavedLightValue(enumLightValue, xx, yy - 1, zz) < v6) lightUpdateBlockList[v1++] = xx - x + 32 + (yy - 1 - y + 32 << 6) + (zz - z + 32 << 12);

							if (getSavedLightValue(enumLightValue, xx, yy + 1, zz) < v6) lightUpdateBlockList[v1++] = xx - x + 32 + (yy + 1 - y + 32 << 6) + (zz - z + 32 << 12);

							if (getSavedLightValue(enumLightValue, xx, yy, zz - 1) < v6) lightUpdateBlockList[v1++] = xx - x + 32 + (yy - y + 32 << 6) + (zz - 1 - z + 32 << 12);

							if (getSavedLightValue(enumLightValue, xx, yy, zz + 1) < v6) lightUpdateBlockList[v1++] = xx - x + 32 + (yy - y + 32 << 6) + (zz + 1 - z + 32 << 12);
						}
					}
				}
			}

			theProfiler.endSection();
		}
	}

	public void updateAllLightTypes(int x, int y, int z) {
		updateLightByType(EnumLightValue.Sky, x, y, z);
		updateLightByType(EnumLightValue.Block, x, y, z);
	}

	public boolean canBlockSeeTheSky(int x, int y, int z) {
		return getChunkFromChunkCoords(x >> 4, z >> 4).canBlockSeeTheSky(x & 15, y, z & 15);
	}

	public int getSavedLightValue(EnumLightValue enumLightValue, int x, int y, int z) {
		if (y < 0) y = 0;

		if (y >= 256) y = 255;

		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			int cx = x >> 4;
			int cz = z >> 4;

			if (!chunkExists(cx, cz)) return enumLightValue.defaultLightValue;
			else {
				// chunk
				Chunk c = getChunkFromChunkCoords(cx, cz);
				return c.getSavedLightValue(enumLightValue, x & 15, y, z & 15);
			}
		} else return enumLightValue.defaultLightValue;
	}

	public void setLightValue(EnumLightValue enumLightValue, int x, int y, int z, int lightValue) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (y >= 0) {
				if (y < 256) {
					if (chunkExists(x >> 4, z >> 4)) {
						// chunk
						Chunk c = getChunkFromChunkCoords(x >> 4, z >> 4);
						c.setLightValue(enumLightValue, x & 15, y, z & 15, lightValue);

						for (int i = 0; i < levelAccesses.size(); ++i)
							levelAccesses.get(i).markBlockForRenderUpdate(x, y, z);
					}
				}
			}
		}
	}

	public int getHeightValue(int x, int z) {
		if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
			if (!chunkExists(x >> 4, z >> 4)) return 0;
			else {
				// chunk
				Chunk c = getChunkFromChunkCoords(x >> 4, z >> 4);
				return c.getHeightValue(x & 15, z & 15);
			}
		} else return 0;
	}

	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB axisAlignedBB) {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		int minX = MathHelper.floor_double((axisAlignedBB.minX - 2.0) / 16.0);
		int maxX = MathHelper.floor_double((axisAlignedBB.maxX + 2.0) / 16.0);
		int minZ = MathHelper.floor_double((axisAlignedBB.minZ - 2.0) / 16.0);
		int maxZ = MathHelper.floor_double((axisAlignedBB.maxZ + 2.0) / 16.0);

		for (int x = minX; x <= maxX; ++x)
			for (int z = minZ; z <= maxZ; ++z)
				if (chunkExists(x, z)) getChunkFromChunkCoords(x, z).getEntitiesWithinAABBForEntity(entity, axisAlignedBB, entities);

		return entities;
	}

	public boolean checkNoEntityCollision(AxisAlignedBB axisAlignedBB, Entity entity) {
		List<Entity> entities = getEntitiesWithinAABBExcludingEntity(null, axisAlignedBB);

		for (int i = 0; i < entities.size(); ++i) {
			Entity e = entities.get(i);

			if (!e.isDead && e.preventEntitySpawning && e != entity) return false;
		}

		return true;
	}

	public boolean checkNoEntityCollision(AxisAlignedBB axisAlignedBB) {
		return checkNoEntityCollision(axisAlignedBB, null);
	}

	// TODO fix this!!!
	public boolean canPlaceBlockOnSide(int id, int x, int y, int z, boolean flag, int side, Entity entity, ItemStack itemStack) {
		if (true) return true;

		int oldId = getBlockId(x, y, z);
		Block oldBlock = Block.blocksList[oldId];
		Block newBlock = Block.blocksList[id];
		// boundingBoxes
		AxisAlignedBB bbs = newBlock.getCollisionBoundingBoxFromPool(this, x, y, z);

		if (flag) bbs = null;

		if (bbs != null && !checkNoEntityCollision(bbs, entity)) return false;
		else {
			if (oldBlock != null && oldBlock.blockMaterial.isReplaceable()) bbs = null;

			return id > 0 && oldBlock == null && newBlock.canPlaceBlockOnSide(this, x, y, z, side, itemStack);
		}
	}

	public boolean a(int x, int y, int z) {
		// blockId
		int id = getBlockId(x, y, z);

		if (id != 0 && Block.blocksList[id] != null) {
			AxisAlignedBB aabb = Block.blocksList[id].getCollisionBoundingBoxFromPool(this, x, y, z);
			return aabb != null && aabb.getAverageEdgeLength() >= 1.0;
		} else return false;
	}

	public boolean isBlockNormalCube(int x, int y, int z) {
		return Block.isNormalCube(getBlockId(x, y, z));
	}

	public float getCelestialAngle(float partialTickTime) {
		return provider.calculateCelestialAngle(levelInfo.getLevelTime(), partialTickTime);
	}

	public float getCelestialAngleRadians(float partialTickTime) {
		// angle
		float a = getCelestialAngle(partialTickTime);
		return a * (float) Math.PI * 2.0f;
	}

	public float getSunBrightness(float partialTickTime) {
		float a = getCelestialAngle(partialTickTime);
		float v0 = 1.0f - (MathHelper.cos(a * (float) Math.PI * 2.0f) * 2.0f + 0.2f);

		if (v0 < 0.0f) v0 = 0.0f;

		if (v0 > 1.0f) v0 = 1.0f;

		v0 = 1.0f - v0;
		return v0 * 0.8f + 0.2f;
	}

	// TODO horizon height
	public double getHorizon() {
		return 63.0;
	}

	public float getStarBrightness(float partialTickTime) {
		float a = getCelestialAngle(partialTickTime);
		float v0 = 1.0f - (MathHelper.cos(a * (float) Math.PI * 2.0f) * 2.0f + 0.25f);

		if (v0 < 0.0f) v0 = 0.0f;

		if (v0 > 1.0f) v0 = 1.0f;

		return v0 * v0 * 0.5f;
	}

	public int calculateSkylightSubtracted(float partialTickTime) {
		float a = getCelestialAngle(partialTickTime);
		float v0 = 1.0f - (MathHelper.cos(a * (float) Math.PI * 2.0f) * 2.0f + 0.5f);

		if (v0 < 0.0f) v0 = 0.0f;

		if (v0 > 1.0f) v0 = 1.0f;

		v0 = 1.0f - v0;
		v0 = 1.0f - v0;
		return (int) (v0 * 11.0f);
	}

	public void calculateInitialSkylight() {
		int n = calculateSkylightSubtracted(1.0f);

		if (n != skylightSubtracted) skylightSubtracted = n;
	}

	public long getTotalLevelTime() {
		return levelInfo.getTotatLevelTime();
	}

	public void incrementTotalLevelTime(long n) {
		levelInfo.incrementTotalLevelTime(n);
	}

	public long getLevelTime() {
		return levelInfo.getLevelTime();
	}

	public void setLevelTime(long n) {
		levelInfo.setLevelTime(n);
	}

	public void setEntityState(Entity entity, byte status) {
	}

	public GameRules getGameRules() {
		return levelInfo.getGameRulesInstance();
	}
}
