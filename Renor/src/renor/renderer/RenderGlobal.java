package renor.renderer;

import static org.lwjgl.opengl.ARBOcclusionQuery.glGenQueriesARB;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import renor.Renor;
import renor.aabb.AxisAlignedBB;
import renor.level.ILevelAccess;
import renor.level.Level;
import renor.level.block.Block;
import renor.level.entity.Entity;
import renor.level.entity.EntityFX;
import renor.level.entity.EntityLiving;
import renor.level.entity.EntityPlayer;
import renor.misc.EntitySorter;
import renor.misc.GLAllocation;
import renor.misc.ICamera;
import renor.misc.MovingObjectPosition;
import renor.util.MathHelper;
import renor.util.OpenGLCapsChecker;
import renor.util.OpenGLHelper;
import renor.util.RenderHelper;
import renor.util.Tessellator;
import renor.util.crashreport.CrashReport;
import renor.util.throwable.ReportedException;
import renor.vector.Vec3;

public class RenderGlobal implements ILevelAccess {
	private List<LevelRenderer> levelRenderersToUpdate = new ArrayList<LevelRenderer>();
	private List<LevelRenderer> glRenderLists = new ArrayList<LevelRenderer>();
	private IntBuffer glOcclusionQueryBase;
	IntBuffer occlusionResult = GLAllocation.createDirectIntBuffer(64);
	private Renor renor;
	private final RenderEngine renderEngine;
	private Level theLevel;
	private LevelRenderer[] levelRenderers;
	private LevelRenderer[] sortedLevelRenderers;
	private RenderList[] allRenderLists = new RenderList[] { new RenderList(), new RenderList(), new RenderList(), new RenderList() };
	private int renderDistanceChunks = -1;
	private int renderChunksWide;
	private int renderChunksTall;
	private int renderChunksDeep;
	private int glRenderListBase;
	private int displayListEntities;
	private int renderEntitiesStartupCounter = 2;
	private int countEntitiesTotal;
	private int countEntitiesRendered;
	private int countEntitiesHidden;
	private int glStarList;
	private int glSkyList;
	private int glSkyList2;
	private int minBlockX;
	private int minBlockY;
	private int minBlockZ;
	private int maxBlockX;
	private int maxBlockY;
	private int maxBlockZ;
	private int renderersLoaded;
	private int renderersBeingClipped;
	private int renderersBeingOccluded;
	private int renderersBeingRendered;
	private int renderersSkippingRenderPass;
	int prevChunkSortX = -999;
	int prevChunkSortY = -999;
	int prevChunkSortZ = -999;
	int frustrumCheckOffset;
	private int levelRenderersCheckIndex;
	double prevSortX = -9999.0;
	double prevSortY = -9999.0;
	double prevSortZ = -9999.0;
	double prevRenderSortX = -9999.0;
	double prevRenderSortY = -9999.0;
	double prevRenderSortZ = -9999.0;
	private boolean occlusionEnabled;
	private boolean displayListEntitiesDirty;

	public RenderGlobal(Renor renor, RenderEngine renderEngine) {
		this.renor = renor;
		this.renderEngine = renderEngine;

		byte v0 = 34;
		byte v1 = 16;
		glRenderListBase = GLAllocation.generateDisplayLists(v0 * v0 * v1 * 3);
		displayListEntitiesDirty = false;
		displayListEntities = GLAllocation.generateDisplayLists(1);
		occlusionEnabled = OpenGLCapsChecker.checkARBOcclusion();

		if (occlusionEnabled) {
			occlusionResult.clear();
			glOcclusionQueryBase = GLAllocation.createDirectIntBuffer(v0 * v0 * v1);
			glOcclusionQueryBase.clear();
			glOcclusionQueryBase.position(0);
			glOcclusionQueryBase.limit(v0 * v0 * v1);
			glGenQueriesARB(glOcclusionQueryBase);
		}

		glStarList = GLAllocation.generateDisplayLists(3);
		glPushMatrix();
		glNewList(glStarList, GL_COMPILE);
		renderStars();
		glEndList();
		glPopMatrix();
		Tessellator tess = Tessellator.instance;
		glSkyList = glStarList + 1;
		glNewList(glSkyList, GL_COMPILE);
		byte v2 = 64;
		int v3 = 256 / v2 + 2;
		float v4 = 16.0f;
		int x;
		int z;

		for (x = -v2 * v3; x <= v2 * v3; x += v2) {
			for (z = -v2 * v3; z <= v2 * v3; z += v2) {
				tess.startDrawingQuads();
				tess.addVertex((double) (x + 0), (double) v4, (double) (z + 0));
				tess.addVertex((double) (x + v2), (double) v4, (double) (z + 0));
				tess.addVertex((double) (x + v2), (double) v4, (double) (z + v2));
				tess.addVertex((double) (x + 0), (double) v4, (double) (z + v2));
				tess.draw();
			}
		}

		glEndList();
		glSkyList2 = glStarList + 2;
		glNewList(glSkyList2, GL_COMPILE);
		v4 = -16.0f;
		tess.startDrawingQuads();

		for (x = -v2 * v3; x <= v2 * v3; x += v2) {
			for (z = -v2 * v3; z <= v2 * v3; z += v2) {
				tess.addVertex((double) (x + v2), (double) v4, (double) (z + 0));
				tess.addVertex((double) (x + 0), (double) v4, (double) (z + 0));
				tess.addVertex((double) (x + 0), (double) v4, (double) (z + v2));
				tess.addVertex((double) (x + v2), (double) v4, (double) (z + v2));
			}
		}

		tess.draw();
		glEndList();
	}

	private void renderStars() {
		Random rand = new Random(10842);
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();

		for (int i = 0; i < 1500; ++i) {
			double v0 = (double) (rand.nextFloat() * 2.0 - 1.0);
			double v1 = (double) (rand.nextFloat() * 2.0 - 1.0);
			double v2 = (double) (rand.nextFloat() * 2.0 - 1.0);
			double v3 = 0.15 + rand.nextFloat() * 0.1;
			double v4 = v0 * v0 + v1 * v1 + v2 * v2;

			if (v4 < 1.0 && v4 > 0.01) {
				v4 = 1.0 / Math.sqrt(v4);
				v0 *= v4;
				v1 *= v4;
				v2 *= v4;
				double v5 = v0 * 100.0;
				double v6 = v1 * 100.0;
				double v7 = v2 * 100.0;
				double v8 = Math.atan2(v0, v2);
				double v9 = Math.sin(v8);
				double v10 = Math.cos(v8);
				double v11 = Math.atan2(Math.sqrt(v0 * v0 + v2 * v2), v1);
				double v12 = Math.sin(v11);
				double v13 = Math.cos(v11);
				double v14 = rand.nextDouble() * Math.PI * 2.0;
				double v15 = Math.sin(v14);
				double v16 = Math.cos(v14);

				for (int j = 0; j < 4; ++j) {
					double v17 = 0.0;
					double v18 = (double) ((j & 2) - 1) * v3;
					double v19 = (double) ((j + 1 & 2) - 1) * v3;
					double v20 = v18 * v16 - v19 * v15;
					double v21 = v19 * v16 + v18 * v15;
					double v22 = v20 * v12 + v17 * v13;
					double v23 = v17 * v12 - v20 * v13;
					double v24 = v23 * v9 - v21 * v10;
					double v25 = v21 * v9 + v23 * v10;
					tess.addVertex(v5 + v24, v6 + v22, v7 + v25);
				}
			}
		}

		tess.draw();
	}

	public void setLevelAndLoadRenderers(Level level) {
		if (theLevel != null) theLevel.removeLevelAccess(this);

		prevSortX = -9999.0;
		prevSortY = -9999.0;
		prevSortZ = -9999.0;
		prevRenderSortX = -9999.0;
		prevRenderSortY = -9999.0;
		prevRenderSortZ = -9999.0;
		prevChunkSortX = -999;
		prevChunkSortY = -999;
		prevChunkSortZ = -999;
		RenderManager.instance.setLevel(level);
		theLevel = level;

		if (level != null) {
			level.addLevelAccess(this);
			loadRenderers();
		}
	}

	public void loadRenderers() {
		if (theLevel != null) {
			renderDistanceChunks = renor.gameSettings.renderDistanceChunks;
			int n;

			if (levelRenderers != null) {
				for (n = 0; n < levelRenderers.length; ++n)
					levelRenderers[n].stopRendering();
			}

			n = renderDistanceChunks * 2 + 1;
			renderChunksWide = n;
			renderChunksTall = 16;
			renderChunksDeep = n;
			levelRenderers = new LevelRenderer[renderChunksWide * renderChunksTall * renderChunksDeep];
			sortedLevelRenderers = new LevelRenderer[renderChunksWide * renderChunksTall * renderChunksDeep];
			int renderList = 0;
			int chunkIndex = 0;
			minBlockX = 0;
			minBlockY = 0;
			minBlockZ = 0;
			maxBlockX = renderChunksWide;
			maxBlockY = renderChunksTall;
			maxBlockZ = renderChunksDeep;
			int i;

			for (i = 0; i < levelRenderersToUpdate.size(); ++i)
				levelRenderersToUpdate.get(i).needsUpdate = false;

			levelRenderersToUpdate.clear();

			for (i = 0; i < renderChunksWide; ++i) {
				for (int y = 0; y < renderChunksTall; ++y) {
					for (int z = 0; z < renderChunksDeep; ++z) {
						levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i] = new LevelRenderer(theLevel, i * 16, y * 16, z * 16, glRenderListBase + renderList);

						if (occlusionEnabled) levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i].glOcclusionQuery = glOcclusionQueryBase.get(chunkIndex);

						levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i].isWaitingOnOcclusionQuery = false;
						levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i].isVisible = true;
						levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i].isInFrustrum = true;
						levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i].chunkIndex = chunkIndex++;
						levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i].markDirty();
						sortedLevelRenderers[(z * renderChunksTall + y) * renderChunksWide + i] = levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i];
						levelRenderersToUpdate.add(levelRenderers[(z * renderChunksTall + y) * renderChunksWide + i]);
						renderList += 3;
					}
				}
			}

			if (theLevel != null) {
				EntityLiving living = renor.renderViewEntity;

				if (living != null) {
					markRenderersForNewPosition(MathHelper.floor_double(living.posX), MathHelper.floor_double(living.posY), MathHelper.floor_double(living.posZ));
					Arrays.sort(sortedLevelRenderers, new EntitySorter(living));
				}
			}

			renderEntitiesStartupCounter = 2;
		}
	}

	private void markRenderersForNewPosition(int x, int y, int z) {
		x -= 8;
		y -= 8;
		z -= 8;
		minBlockX = Integer.MAX_VALUE;
		minBlockY = Integer.MAX_VALUE;
		minBlockZ = Integer.MAX_VALUE;
		maxBlockX = Integer.MIN_VALUE;
		maxBlockY = Integer.MIN_VALUE;
		maxBlockZ = Integer.MIN_VALUE;
		int v0 = renderChunksWide * 16;
		int v1 = v0 / 2;

		for (int xx = 0; xx < renderChunksWide; ++xx) {
			int xxx = xx * 16;
			int v2 = xxx + v1 - x;

			if (v2 < 0) v2 -= v0 - 1;

			v2 /= v0;
			xxx -= v2 * v0;

			if (xxx < minBlockX) minBlockX = xxx;

			if (xxx > maxBlockX) maxBlockX = xxx;

			for (int zz = 0; zz < renderChunksDeep; ++zz) {
				int zzz = zz * 16;
				int v3 = zzz + v1 - z;

				if (v3 < 0) v3 -= v0 - 1;

				v3 /= v0;
				zzz -= v3 * v0;

				if (zzz < minBlockZ) minBlockZ = zzz;

				if (zzz > maxBlockZ) maxBlockZ = zzz;

				for (int yy = 0; yy < renderChunksTall; ++yy) {
					int yyy = yy * 16;

					if (yyy < minBlockY) minBlockY = yyy;

					if (yyy > maxBlockY) maxBlockY = yyy;

					// levelRenderer
					LevelRenderer lr = levelRenderers[(zz * renderChunksTall + yy) * renderChunksWide + xx];
					// neededUpdate
					boolean v4 = lr.needsUpdate;
					lr.setPosition(xxx, yyy, zzz);

					if (!v4 && lr.needsUpdate) levelRenderersToUpdate.add(lr);
				}
			}
		}
	}

	public int sortAndRender(EntityLiving living, int renderPass, double partialTickTime) {
		theLevel.theProfiler.startSection("sortchunks");

		for (int i = 0; i < 10; ++i) {
			levelRenderersCheckIndex = (levelRenderersCheckIndex + 1) % levelRenderers.length;
			// levelRenderer
			LevelRenderer lr = levelRenderers[levelRenderersCheckIndex];

			if (lr.needsUpdate && !levelRenderersToUpdate.contains(lr)) levelRenderersToUpdate.add(lr);
		}

		if (renor.gameSettings.renderDistanceChunks != renderDistanceChunks) loadRenderers();

		if (renderPass == 0) {
			renderersLoaded = 0;
			renderersBeingClipped = 0;
			renderersBeingOccluded = 0;
			renderersBeingRendered = 0;
			renderersSkippingRenderPass = 0;
		}

		double dx = living.posX - prevSortX;
		double dy = living.posY - prevSortY;
		double dz = living.posZ - prevSortZ;

		if (prevChunkSortX != living.chunkCoordX || prevChunkSortY != living.chunkCoordY || prevChunkSortZ != living.chunkCoordZ || dx * dx + dy * dy + dz * dz > 16.0) {
			prevSortX = living.posX;
			prevSortY = living.posY;
			prevSortZ = living.posZ;
			prevChunkSortX = living.chunkCoordX;
			prevChunkSortY = living.chunkCoordY;
			prevChunkSortZ = living.chunkCoordZ;
			markRenderersForNewPosition(MathHelper.floor_double(living.posX), MathHelper.floor_double(living.posY), MathHelper.floor_double(living.posZ));
			Arrays.sort(sortedLevelRenderers, new EntitySorter(living));
		}

		double dxx = living.posX - prevRenderSortX;
		double dyy = living.posY - prevRenderSortY;
		double dzz = living.posZ - prevRenderSortZ;
		int n;

		if (dxx * dxx + dyy * dyy + dzz * dzz > 1.0) {
			prevRenderSortX = living.posX;
			prevRenderSortY = living.posY;
			prevRenderSortZ = living.posZ;

			for (n = 0; n < 27; ++n)
				sortedLevelRenderers[n].updateRenderSort(living);
		}

		RenderHelper.disableStandardLighting();

		theLevel.theProfiler.endStartSection("render");
		n = renderSortedRenderers(0, sortedLevelRenderers.length, renderPass, partialTickTime);

		theLevel.theProfiler.endSection();
		return n;
	}

	private int renderSortedRenderers(int startRenderer, int numRenderers, int renderPass, double partialTickTime) {
		glRenderLists.clear();
		int n = 0;
		int start = startRenderer;
		int end = numRenderers;
		byte inc = 1;

		if (renderPass == 1) {
			start = sortedLevelRenderers.length - 1 - startRenderer;
			end = sortedLevelRenderers.length - 1 - numRenderers;
			inc = -1;
		}

		for (int i = start; i != end; i += inc) {
			if (renderPass == 0) {
				++renderersLoaded;

				if (sortedLevelRenderers[i].skipRenderPass[renderPass]) ++renderersSkippingRenderPass;
				else if (!sortedLevelRenderers[i].isInFrustrum) ++renderersBeingClipped;
				else if (occlusionEnabled && !sortedLevelRenderers[i].isVisible) ++renderersBeingOccluded;
				else ++renderersBeingRendered;
			}

			if (!sortedLevelRenderers[i].skipRenderPass[renderPass] && sortedLevelRenderers[i].isInFrustrum && (!occlusionEnabled || sortedLevelRenderers[i].isVisible)) {
				int list = sortedLevelRenderers[i].getGLCallListForPass(renderPass);

				if (list >= 0) {
					glRenderLists.add(sortedLevelRenderers[i]);
					++n;
				}
			}
		}

		// living
		EntityLiving l = renor.renderViewEntity;
		double x = l.lastTickPosX + (l.posX - l.lastTickPosX) * partialTickTime;
		double y = l.lastTickPosY + (l.posY - l.lastTickPosY) * partialTickTime;
		double z = l.lastTickPosZ + (l.posZ - l.lastTickPosZ) * partialTickTime;
		int v0 = 0;
		int i;

		for (i = 0; i < allRenderLists.length; ++i)
			allRenderLists[i].resetList();

		int v1;
		int j;

		for (i = 0; i < glRenderLists.size(); ++i) {
			// levelRenderer
			LevelRenderer lr = glRenderLists.get(i);
			v1 = -1;

			for (j = 0; j < v0; ++j)
				if (allRenderLists[j].rendersChunk(lr.posXMinus, lr.posYMinus, lr.posZMinus)) v1 = j;

			if (v1 < 0) {
				v1 = v0++;
				allRenderLists[v1].setupRenderList(lr.posXMinus, lr.posYMinus, lr.posZMinus, x, y, z);
			}

			allRenderLists[v1].addGLRenderList(lr.getGLCallListForPass(renderPass));
		}

		i = MathHelper.floor_double(x);
		int v2 = MathHelper.floor_double(z);
		v1 = i - (i & 1023);
		j = v2 - (v2 & 1023);
		// sort renderLists here, parameters (x, z) = (v1, j)
		renderAllRenderLists(renderPass, partialTickTime);
		return n;
	}

	public void renderAllRenderLists(int renderPass, double partialTickTime) {
		renor.entityRenderer.enableLightmap(partialTickTime);

		for (int i = 0; i < allRenderLists.length; ++i)
			allRenderLists[i].callLists();

		renor.entityRenderer.disableLightmap(partialTickTime);
	}

	public void renderEntities(EntityLiving living, ICamera camera, float partialTickTime) {
		if (renderEntitiesStartupCounter > 0) --renderEntitiesStartupCounter;
		else {
			double x = living.prevPosX + (living.posX - living.prevPosX) * (double) partialTickTime;
			double y = living.prevPosY + (living.posY - living.prevPosY) * (double) partialTickTime;
			double z = living.prevPosZ + (living.posZ - living.prevPosZ) * (double) partialTickTime;
			theLevel.theProfiler.startSection("prepare");
			RenderManager.instance.cachaActiveRenderInfo(theLevel, renderEngine, renor.fontRenderer, renor.renderViewEntity, renor.pointedEntity, renor.gameSettings, partialTickTime);
			countEntitiesTotal = 0;
			countEntitiesRendered = 0;
			countEntitiesHidden = 0;
			// living2
			EntityLiving l = renor.renderViewEntity;
			double xx = l.lastTickPosX + (l.posX - l.lastTickPosX) * (double) partialTickTime;
			double yy = l.lastTickPosY + (l.posY - l.lastTickPosY) * (double) partialTickTime;
			double zz = l.lastTickPosZ + (l.posZ - l.lastTickPosZ) * (double) partialTickTime;
			theLevel.theProfiler.endStartSection("staticentities");

			if (displayListEntitiesDirty) {
				RenderManager.renderPosX = 0;
				RenderManager.renderPosY = 0;
				RenderManager.renderPosZ = 0;
				rebuildDisplayListEntities();
			}

			glMatrixMode(GL_MODELVIEW);
			glPushMatrix();
			glTranslated(-xx, -yy, -zz);
			glCallList(displayListEntities);
			glPopMatrix();
			RenderManager.renderPosX = xx;
			RenderManager.renderPosY = yy;
			RenderManager.renderPosZ = zz;

			// loadedEntities
			List<Entity> le = theLevel.getLoadedEntityList();
			countEntitiesTotal = le.size();
			int i = 0;
			// entity
			Entity e;

			renor.entityRenderer.enableLightmap((double) partialTickTime);
			theLevel.theProfiler.endStartSection("entities");

			for (i = 0; i < le.size(); ++i) {
				e = le.get(i);
				boolean v0 = e.isInRangeToRender3D(x, y, z) && (e.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(e.boundingBox));

				if (v0 && (e != renor.renderViewEntity || renor.gameSettings.thirdPersonView != 0) && theLevel.blockExists(MathHelper.floor_double(e.posX), 0, MathHelper.floor_double(e.posZ))) {
					++countEntitiesRendered;
					RenderManager.instance.renderEntity(e, partialTickTime);
				}
			}

			theLevel.theProfiler.endSection();
			renor.entityRenderer.disableLightmap((double) partialTickTime);
		}
	}

	public void rebuildDisplayListEntities() {
		theLevel.theProfiler.startSection("staticentityrebuild");
		glPushMatrix();
		glNewList(displayListEntities, GL_COMPILE);
		// loadedEntities
		List<Entity> le = theLevel.getLoadedEntityList();
		displayListEntitiesDirty = false;

		for (int i = 0; i < le.size(); ++i) {
			Entity e = le.get(i);

			if (RenderManager.instance.getEntityRenderObject(e).a()) displayListEntitiesDirty = displayListEntitiesDirty || !RenderManager.instance.renderEntity(e, 0.0f, true);
		}

		glEndList();
		glPopMatrix();
		theLevel.theProfiler.endSection();
	}

	public String getDebugRenderers() {
		return "C: " + renderersBeingRendered + "/" + renderersLoaded + ". F: " + renderersBeingClipped + ", O: " + renderersBeingOccluded + ", E: " + renderersSkippingRenderPass;
	}

	public String getDebugEntities() {
		return "E: " + countEntitiesRendered + "/" + countEntitiesTotal + ". B: " + countEntitiesHidden + ", I: " + (countEntitiesTotal - countEntitiesHidden - countEntitiesRendered);
	}

	public void renderSky(float partialTickTime) {
		boolean test = true;
		if (test) {
			glDisable(GL_FOG);
			glDisable(GL_ALPHA_TEST);
			glEnable(GL_BLEND);
			OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
			RenderHelper.disableStandardLighting();
			glDepthMask(false);
			renderEngine.bindTexture("/tunnel.png");
			Tessellator tess = Tessellator.instance;

			for (int i = 0; i < 6; ++i) {
				glPushMatrix();

				if (i == 1) glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
				if (i == 2) glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
				if (i == 3) glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
				if (i == 4) glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
				if (i == 5) glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);

				tess.startDrawingQuads();
				tess.setColorOpaque_I(2631720);
				tess.addVertexWithUV(-100.0, -100.0, -100.0, 0.0, 0.0);
				tess.addVertexWithUV(-100.0, -100.0, 100.0, 0.0, 16.0);
				tess.addVertexWithUV(100.0, -100.0, 100.0, 16.0, 16.0);
				tess.addVertexWithUV(100.0, -100.0, -100.0, 16.0, 0.0);
				tess.draw();
				glPopMatrix();
			}

			glDepthMask(true);
			glEnable(GL_TEXTURE_2D);
			glEnable(GL_ALPHA_TEST);
		} else {
			// TODO fix sky
			glDisable(GL_TEXTURE_2D);
			Vec3 sc = this.theLevel.getSkyColor(renor.renderViewEntity, partialTickTime);
			float sr = (float) sc.xCoord;
			float sg = (float) sc.yCoord;
			float sb = (float) sc.zCoord;
			float n;

			if (renor.gameSettings.anaglyph) {
				float ar = (sr * 30.0f + sg * 59.0f + sb * 11.0f) / 100.0f;
				float ag = (sr * 30.0f + sg * 70.0f) / 100.0f;
				n = (sr * 30.0f + sb * 70.0f) / 100.0f;
				sr = ar;
				sg = ag;
				sb = n;
			}

			glColor3f(sr, sg, sb);
			Tessellator tess = Tessellator.instance;
			glDepthMask(false);
			glEnable(GL_FOG);
			glColor3f(sr, sg, sb);
			glCallList(glSkyList);
			glDisable(GL_FOG);
			glDisable(GL_ALPHA_TEST);
			glEnable(GL_BLEND);
			OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
			RenderHelper.disableStandardLighting();
			float[] colors = theLevel.provider.calcSunriseSunsetColors(theLevel.getCelestialAngle(partialTickTime), partialTickTime);
			float v0;
			float v1;
			float v2;
			float v3;

			if (colors != null) {
				glDisable(GL_TEXTURE_2D);
				glShadeModel(GL_SMOOTH);
				glPushMatrix();
				glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
				glRotatef(MathHelper.sin(theLevel.getCelestialAngleRadians(partialTickTime)) < 0.0f ? 180.0f : 0.0f, 0.0f, 0.0f, 1.0f);
				glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
				n = colors[0];
				v0 = colors[1];
				v1 = colors[2];
				float v4;

				if (renor.gameSettings.anaglyph) {
					v2 = (n * 30.0f + v0 * 59.0f + v1 * 11.0f) / 100.0f;
					v3 = (n * 30.0f + v0 * 70.0f) / 100.0f;
					v4 = (n * 30.0f + v1 * 70.0f) / 100.0f;
					n = v2;
					v0 = v3;
					v1 = v4;
				}

				tess.startDrawing(6);
				tess.setColorRGBA_F(n, v0, v1, colors[3]);
				tess.addVertex(0.0, 100.0, 0.0);
				byte v5 = 16;
				tess.setColorRGBA_F(colors[0], colors[1], colors[2], 0.0f);

				for (int i = 0; i <= v5; ++i) {
					v4 = (float) i * (float) Math.PI * 2.0f / (float) v5;
					float v6 = MathHelper.sin(v4);
					float v7 = MathHelper.cos(v4);
					tess.addVertex((double) (v6 * 120.0f), (double) (v7 * 120.0f), (double) (-v7 * 40.0f * colors[3]));
				}

				tess.draw();
				glPopMatrix();
				glShadeModel(GL_FLAT);
			}

			glEnable(GL_TEXTURE_2D);
			OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ZERO);
			glPushMatrix();
			n = 1.0f - 0.0f;
			v0 = 0.0f;
			v1 = 0.0f;
			v2 = 0.0f;
			glColor4f(1.0f, 1.0f, 1.0f, n);
			glTranslatef(v0, v1, v2);
			glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
			glRotatef(theLevel.getCelestialAngle(partialTickTime) * 360.0f, 1.0f, 0.0f, 0.0f);
			v3 = 30.0f;
			renderEngine.bindTexture("hi");
			tess.startDrawingQuads();
			tess.addVertexWithUV((double) (-v3), 100.0, (double) (-v3), 0.0, 0.0);
			tess.addVertexWithUV((double) v3, 100.0, (double) (-v3), 1.0, 0.0);
			tess.addVertexWithUV((double) v3, 100.0, (double) v3, 1.0, 1.0);
			tess.addVertexWithUV((double) (-v3), 100.0, (double) v3, 0.0, 1.0);
			tess.draw();
			v3 = 20.0f;
			renderEngine.bindTexture("hi");
			int v4 = 0;
			int v5 = v4 % 4;
			int v6 = v4 / 4 % 2;
			float v7 = (float) (v5 + 0) / 4.0f;
			float v8 = (float) (v6 + 0) / 2.0f;
			float v9 = (float) (v5 + 1) / 4.0f;
			float v10 = (float) (v6 + 1) / 2.0f;
			tess.startDrawingQuads();
			tess.addVertexWithUV((double) (-v3), -100.0, (double) v3, (double) v9, (double) v10);
			tess.addVertexWithUV((double) v3, -100.0, (double) v3, (double) v7, (double) v10);
			tess.addVertexWithUV((double) v3, -100.0, (double) (-v3), (double) v7, (double) v8);
			tess.addVertexWithUV((double) (-v3), -100.0, (double) (-v3), (double) v9, (double) v8);
			tess.draw();
			glDisable(GL_TEXTURE_2D);
			float v11 = theLevel.getStarBrightness(partialTickTime) * n;

			if (v11 > 0.0f) {
				glColor4f(v11, v11, v11, v11);
				glCallList(glStarList);
			}

			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			glDisable(GL_BLEND);
			glEnable(GL_ALPHA_TEST);
			glEnable(GL_FOG);
			glPopMatrix();
			glDisable(GL_TEXTURE_2D);
			glColor3f(0.0f, 0.0f, 0.0f);
			double v12 = renor.thePlayer.getPosition(partialTickTime).yCoord - theLevel.getHorizon();

			if (v12 < 0.0) {
				glPushMatrix();
				glTranslatef(0.0f, 12.0f, 0.0f);
				glCallList(glSkyList2);
				glPopMatrix();
				v1 = 1.0f;
				v2 = -((float) (v12 + 65.0));
				v3 = -v1;
				tess.startDrawingQuads();
				tess.setColorRGBA_I(0, 255);
				tess.addVertex((double) (-v1), (double) v2, (double) v1);
				tess.addVertex((double) v1, (double) v2, (double) v1);
				tess.addVertex((double) v1, (double) v3, (double) v1);
				tess.addVertex((double) (-v1), (double) v3, (double) v1);
				tess.addVertex((double) (-v1), (double) v3, (double) (-v1));
				tess.addVertex((double) v1, (double) v3, (double) (-v1));
				tess.addVertex((double) v1, (double) v2, (double) (-v1));
				tess.addVertex((double) (-v1), (double) v2, (double) (-v1));
				tess.addVertex((double) v1, (double) v3, (double) (-v1));
				tess.addVertex((double) v1, (double) v3, (double) v1);
				tess.addVertex((double) v1, (double) v2, (double) v1);
				tess.addVertex((double) v1, (double) v2, (double) (-v1));
				tess.addVertex((double) (-v1), (double) v2, (double) (-v1));
				tess.addVertex((double) (-v1), (double) v2, (double) v1);
				tess.addVertex((double) (-v1), (double) v3, (double) v1);
				tess.addVertex((double) (-v1), (double) v3, (double) (-v1));
				tess.addVertex((double) (-v1), (double) v3, (double) (-v1));
				tess.addVertex((double) (-v1), (double) v3, (double) v1);
				tess.addVertex((double) v1, (double) v3, (double) v1);
				tess.addVertex((double) v1, (double) v3, (double) (-v1));
				tess.draw();
			}

			if (theLevel.provider.isSkyColored()) glColor3f(sr * 0.2f + 0.04f, sg * 0.2f + 0.04f, v11 * 0.6f + 0.1f);
			else glColor3f(sr, sg, sb);

			glPushMatrix();
			glTranslatef(0.0f, -((float) (v12 - 16.0)), 0.0f);
			glCallList(glSkyList2);
			glPopMatrix();
			glEnable(GL_TEXTURE_2D);
			glDepthMask(true);
		}
	}

	// check on this
	public boolean updateRenderers(EntityLiving living, boolean flag) {
		byte n = 2;
		RenderSorter rs = new RenderSorter(living);
		LevelRenderer[] v0 = new LevelRenderer[n];
		ArrayList<LevelRenderer> v1 = null;
		int v2 = levelRenderersToUpdate.size();
		int v3 = 0;
		theLevel.theProfiler.startSection("nearChunksSearch");
		int i;
		LevelRenderer lr;
		int j;
		int v4;
		loop:

		for (i = 0; i < v2; ++i) {
			lr = levelRenderersToUpdate.get(i);

			if (lr != null) {
				if (!flag) {
					if (lr.distanceToEntitySquared(living) > 272.0f) {
						for (j = 0; j < n && (v0[j] == null || rs.doCompare(v0[j], lr) <= 0); ++j) {
						}

						--j;

						if (j > 0) {
							v4 = j;

							while (true) {
								--v4;

								if (v4 == 0) {
									v0[j] = lr;
									continue loop;
								}

								v0[v4 - 1] = v0[v4];
							}
						}

						continue;
					}
				} else if (!lr.isInFrustrum) continue;

				if (v1 == null) v1 = new ArrayList<LevelRenderer>();

				++v3;
				v1.add(lr);
				levelRenderersToUpdate.set(i, null);
			}
		}

		theLevel.theProfiler.endStartSection("sort");

		if (v1 != null) {
			if (v1.size() > 1) Collections.sort(v1, rs);

			for (i = v1.size() - 1; i >= 0; --i) {
				lr = v1.get(i);
				lr.updateRenderer(living);
				lr.needsUpdate = false;
			}
		}

		theLevel.theProfiler.endSection();
		i = 0;
		theLevel.theProfiler.startSection("rebuild");
		int k;

		for (k = n - 1; k >= 0; --k) {
			LevelRenderer v5 = v0[k];

			if (v5 != null) {
				if (!v5.isInFrustrum && k != n - 1) {
					v0[k] = null;
					v0[0] = null;
					break;
				}

				v0[k].updateRenderer(living);
				v0[k].needsUpdate = false;
				++i;
			}
		}

		theLevel.theProfiler.endStartSection("cleanup");
		k = 0;
		j = 0;

		for (v4 = levelRenderersToUpdate.size(); k != v4; ++k) {
			LevelRenderer v5 = levelRenderersToUpdate.get(k);

			if (v5 != null) {
				boolean v6 = false;

				for (int var7 = 0; var7 < n && !v6; ++var7)
					if (v5 == v0[var7]) v6 = true;

				if (!v6) {
					if (j != k) levelRenderersToUpdate.set(j, v5);

					++j;
				}
			}
		}

		theLevel.theProfiler.endStartSection("trim");

		while (true) {
			--k;

			if (k < j) {
				theLevel.theProfiler.endSection();
				return v2 == v3 + i;
			}

			levelRenderersToUpdate.remove(k);
		}
	}

	public void clipRenderersByFrustrum(ICamera camera) {
		for (int i = 0; i < levelRenderers.length; ++i)
			if (!levelRenderers[i].skipAllRenderPasses() && (!levelRenderers[i].isInFrustrum || (i + frustrumCheckOffset & 15) == 0)) levelRenderers[i].updateInFrustrum(camera);

		++frustrumCheckOffset;
	}

	public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPosition, float partialTickTime) {
		if (movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			glEnable(GL_BLEND);
			OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
			glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
			glLineWidth(2.0f);
			glDisable(GL_TEXTURE_2D);
			glDepthMask(false);
			// expand
			float n = 0.002f;
			// blockId
			int id = theLevel.getBlockId(movingObjectPosition.blockX, movingObjectPosition.blockY, movingObjectPosition.blockZ);

			if (id > 0) {
				Block.blocksList[id].setBlockBoundsBasedOnState(theLevel, movingObjectPosition.blockX, movingObjectPosition.blockY, movingObjectPosition.blockZ);
				double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTickTime;
				double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTickTime;
				double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTickTime;
				drawOutlinedBoundingBox(Block.blocksList[id].getSelectedBoundingBoxFromPool(theLevel, movingObjectPosition.blockX, movingObjectPosition.blockY, movingObjectPosition.blockZ).expand((double) n, (double) n, (double) n).getOffsetBoundingBox(-x, -y, -z), -1);
			}

			glDepthMask(true);
			glEnable(GL_TEXTURE_2D);
			glDisable(GL_BLEND);
		}
	}

	public static void drawOutlinedBoundingBox(AxisAlignedBB axisAlignedBB, int color) {
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(3);

		if (color != -1) tess.setColorOpaque_I(color);

		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.draw();
		tess.startDrawing(3);

		if (color != -1) tess.setColorOpaque_I(color);

		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.draw();
		tess.startDrawing(1);

		if (color != -1) tess.setColorOpaque_I(color);

		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
		tess.addVertex(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
		tess.draw();
	}

	public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		int x0 = MathHelper.bucketInt(minX, 16);
		int y0 = MathHelper.bucketInt(minY, 16);
		int z0 = MathHelper.bucketInt(minZ, 16);
		int x1 = MathHelper.bucketInt(maxX, 16);
		int y1 = MathHelper.bucketInt(maxY, 16);
		int z1 = MathHelper.bucketInt(maxZ, 16);

		for (int x = x0; x <= x1; ++x) {
			int xx = x % renderChunksWide;

			if (xx < 0) xx += renderChunksWide;

			for (int y = y0; y <= y1; ++y) {
				int yy = y % renderChunksTall;

				if (yy < 0) yy += renderChunksTall;

				for (int z = z0; z <= z1; ++z) {
					int zz = z % renderChunksDeep;

					if (zz < 0) zz += renderChunksDeep;

					int i = (zz * renderChunksTall + yy) * renderChunksWide + xx;
					// levelRenderer
					LevelRenderer lr = levelRenderers[i];

					if (lr != null && !lr.needsUpdate) {
						levelRenderersToUpdate.add(lr);
						lr.markDirty();
					}
				}
			}
		}
	}

	public void markBlockForUpdate(int x, int y, int z) {
		markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
	}

	public void markBlockForRenderUpdate(int x, int y, int z) {
		markBlocksForUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
	}

	public void markBlockRangeForRenderUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		markBlocksForUpdate(minX - 1, minY - 1, minZ - 1, maxX + 1, maxY + 1, maxZ + 1);
	}

	public void playSound(String name, float x, float y, float z, float volume, float pitch) {
	}

	public void spawnParticle(String name, double x, double y, double z, double mx, double my, double mz) {
		try {
			doSpawnParticle(name, x, y, z, mx, my, mz);
		} catch (Throwable e) {
			CrashReport crashReport = CrashReport.makeCrashReport(e, "Exception while adding particle");
			throw new ReportedException(crashReport);
		}
	}

	public void onStaticEntitiesChanged() {
		displayListEntitiesDirty = true;
	}

	public EntityFX doSpawnParticle(String name, double x, double y, double z, double mx, double my, double mz) {
		if (renor != null && renor.renderViewEntity != null && renor.effectRenderer != null) {

			EntityFX fx = null;

			if (name.equals("test")) fx = new EntityFX(theLevel);

			if (fx != null) renor.effectRenderer.addEffect(fx);

			return fx;
		}

		return null;
	}

	public void deleteAllDisplayLists() {
		GLAllocation.deleteDisplayLists(glRenderListBase);
	}
}
