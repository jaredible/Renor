package renor.renderer;

import static org.lwjgl.opengl.GL11.*;
import renor.Renor;
import renor.aabb.AxisAlignedBB;
import renor.level.Level;
import renor.level.block.Block;
import renor.level.chunk.Chunk;
import renor.level.chunk.ChunkCache;
import renor.level.entity.Entity;
import renor.level.entity.EntityLiving;
import renor.misc.ICamera;
import renor.util.MathHelper;
import renor.util.Tessellator;
import renor.util.TessellatorVertexState;

public class LevelRenderer {
	private static Tessellator tessellator = Tessellator.instance;
	public Level levelObj;
	public AxisAlignedBB rendererBoundingBox;
	private TessellatorVertexState vertexState;
	public static int chunksUpdated = 0;
	public int posX;
	public int posY;
	public int posZ;
	public int posXMinus;
	public int posYMinus;
	public int posZMinus;
	public int posXClip;
	public int posYClip;
	public int posZClip;
	public int posXPlus;
	public int posYPlus;
	public int posZPlus;
	public int chunkIndex;
	public int glRenderList = -1;
	private int bytesDrawn;
	public int glOcclusionQuery;
	public boolean needsUpdate;
	private boolean isInitialized = false;
	public boolean isInFrustrum = false;
	public boolean isVisible = true;
	public boolean isWaitingOnOcclusionQuery;
	public boolean isChunkLit;
	public boolean[] skipRenderPass = new boolean[2];

	public LevelRenderer(Level level, int x, int y, int z, int renderList) {
		levelObj = level;
		posX = -999;
		setPosition(x, y, z);
		glRenderList = renderList;

		vertexState = null;
		needsUpdate = false;
	}

	public void setPosition(int x, int y, int z) {
		if (x != posX || y != posY || z != posZ) {
			setDontDraw();
			posX = x;
			posY = y;
			posZ = z;
			posXPlus = x + 8;
			posYPlus = y + 8;
			posZPlus = z + 8;
			posXClip = x & 1023;
			posYClip = y;
			posZClip = z & 1023;
			posXMinus = x - posXClip;
			posYMinus = y - posYClip;
			posZMinus = z - posZClip;
			float n = 6.0f;
			rendererBoundingBox = AxisAlignedBB.getBoundingBox((double) ((float) x - n), (double) ((float) y - n), (double) ((float) z - n), (double) ((float) (x + 16) + n), (double) ((float) (y + 16) + n), (double) ((float) (z + 16) + n));
			glNewList(glRenderList + 2, GL_COMPILE);
			Render.renderAABB(AxisAlignedBB.getAABBPool().getAABB((double) ((float) posXClip - n), (double) ((float) posYClip - n), (double) ((float) posZClip - n), (double) ((float) (posXClip + 16) + n), (double) ((float) (posYClip + 16) + n), (double) ((float) (posZClip + 16) + n)));
			glEndList();
			markDirty();
		}
	}

	public void updateRenderer(EntityLiving living) {
		if (needsUpdate) {
			needsUpdate = false;
			int x = posX;
			int y = posY;
			int z = posZ;
			int xx = posX + 16;
			int yy = posY + 16;
			int zz = posZ + 16;

			for (int i = 0; i < 2; ++i)
				skipRenderPass[i] = true;

			Chunk.isLit = false;
			Renor renor = Renor.getRenor();
			// living2
			EntityLiving l = renor.renderViewEntity;
			int xp = MathHelper.floor_double(l.posX);
			int yp = MathHelper.floor_double(l.posY);
			int zp = MathHelper.floor_double(l.posZ);
			byte r = 1;
			// chunkCache
			ChunkCache cc = new ChunkCache(levelObj, x - r, y - r, z - r, xx + r, yy + r, zz + r, r);

			if (!cc.extendedLevelsInChunkCache()) {
				++chunksUpdated;
				// renderBlocks
				RenderBlocks rb = new RenderBlocks(cc);
				bytesDrawn = 0;
				vertexState = null;

				for (int i = 0; i < 2; ++i) {
					boolean flag = false;
					boolean rendered = false;
					boolean compiled = false;

					for (int yyy = y; yyy < yy; ++yyy) {
						for (int zzz = z; zzz < zz; ++zzz) {
							for (int xxx = x; xxx < xx; ++xxx) {
								// blockId
								int id = cc.getBlockId(xxx, yyy, zzz);

								if (id > 0) {
									if (!compiled) {
										compiled = true;
										preRenderBlocks(i);
									}

									// block
									Block b = Block.blocksList[id];

									if (b != null) {
										// renderBlockPass
										int p = b.getRenderBlockPass();

										if (p > i) flag = true;
										else if (p == i) {
											rendered |= rb.renderBlockByRenderType(b, xxx, yyy, zzz);

											if (b.getRenderType() == 0 && xxx == xp && yyy == yp && zzz == zp) {
												rb.setRenderFromInside(true);
												rb.setRenderAllFaces(true);
												rb.renderBlockByRenderType(b, xxx, yyy, zzz);
												rb.setRenderFromInside(false);
												rb.setRenderAllFaces(false);
											}
										}
									}
								}
							}
						}
					}

					if (rendered) skipRenderPass[i] = false;

					if (compiled) postRenderBlocks(i, living);
					else rendered = false;

					if (!flag) break;
				}
			}

			isChunkLit = Chunk.isLit;
			isInitialized = true;
		}
	}

	private void preRenderBlocks(int renderPass) {
		glNewList(glRenderList + renderPass, GL_COMPILE);
		glPushMatrix();
		setupGLTranslation();
		float n = 1.000001f;
		glTranslatef(-8.0f, -8.0f, -8.0f);
		glScalef(n, n, n);
		glTranslatef(8.0f, 8.0f, 8.0f);
		tessellator.startDrawingQuads();
		tessellator.setTranslation((double) (-posX), (double) (-posY), (double) (-posZ));
	}

	private void postRenderBlocks(int renderPass, EntityLiving living) {
		if (renderPass == 1 && !skipRenderPass[renderPass]) vertexState = tessellator.getVertexState((float) living.posX, (float) living.posY, (float) living.posZ);

		bytesDrawn += tessellator.draw();
		glPopMatrix();
		glEndList();
		tessellator.setTranslation(0.0, 0.0, 0.0);
	}

	public void updateRenderSort(EntityLiving living) {
		if (vertexState != null && !skipRenderPass[1]) {
			preRenderBlocks(1);
			tessellator.setVertexState(vertexState);
			postRenderBlocks(1, living);
		}
	}

	private void setupGLTranslation() {
		glTranslatef((float) posXClip, (float) posYClip, (float) posZClip);
	}

	public void markDirty() {
		needsUpdate = true;
	}

	public boolean skipAllRenderPasses() {
		return !isInitialized ? false : skipRenderPass[0] && skipRenderPass[1];
	}

	public void callOcclusionQueryList() {
		glCallList(glRenderList + 2);
	}

	public void updateInFrustrum(ICamera camera) {
		isInFrustrum = camera.isBoundingBoxInFrustum(rendererBoundingBox);
	}

	public void setDontDraw() {
		for (int i = 0; i < 2; ++i)
			skipRenderPass[i] = true;

		isInitialized = false;
		isInFrustrum = false;
		vertexState = null;
	}

	public void stopRendering() {
		setDontDraw();
		levelObj = null;
	}

	public int getGLCallListForPass(int renderPass) {
		return !isInFrustrum ? -1 : (!skipRenderPass[renderPass] ? glRenderList + renderPass : -1);
	}

	public float distanceToEntitySquared(Entity entity) {
		float x = (float) (entity.posX - (double) posXPlus);
		float y = (float) (entity.posY - (double) posYPlus);
		float z = (float) (entity.posZ - (double) posZPlus);
		return x * x + y * y + z * z;
	}
}
