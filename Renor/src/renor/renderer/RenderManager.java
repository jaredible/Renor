package renor.renderer;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import renor.aabb.AxisAlignedBB;
import renor.level.Level;
import renor.level.entity.Entity;
import renor.level.entity.EntityItem;
import renor.level.entity.EntityLiving;
import renor.level.entity.EntityPlayer;
import renor.level.entity.EntityZombie;
import renor.misc.GameSettings;
import renor.util.OpenGLHelper;
import renor.util.crashreport.CrashReport;
import renor.util.texture.IconRegister;
import renor.util.throwable.ReportedException;

public class RenderManager {
	private Map<Class<? extends Entity>, Render> entityRenderMap = new HashMap<Class<? extends Entity>, Render>();
	public static RenderManager instance = new RenderManager();
	public RenderEngine renderEngine;
	public Level levelObj;
	public GameSettings options;
	public static double renderPosX;
	public static double renderPosY;
	public static double renderPosZ;
	public double viewerPosX;
	public double viewerPosY;
	public double viewerPosZ;
	public float entityViewX;
	public float entityViewY;
	public static boolean a;

	private RenderManager() {
		entityRenderMap.put(Entity.class, new RenderEntity());
		entityRenderMap.put(EntityLiving.class, new RenderLiving());
		entityRenderMap.put(EntityPlayer.class, new RenderPlayer());
		entityRenderMap.put(EntityZombie.class, new RenderZombie());
		entityRenderMap.put(EntityItem.class, new RenderItem());

		Iterator<Render> entityRenderMapIterator = entityRenderMap.values().iterator();

		while (entityRenderMapIterator.hasNext()) {
			Render r = entityRenderMapIterator.next();
			r.setRenderManager(this);
		}
	}

	public Render getEntityClassRenderObject(Class<? extends Entity> entityClass) {
		Render render = entityRenderMap.get(entityClass);

		if (render == null && entityClass != Entity.class) {
			render = getEntityClassRenderObject((Class<? extends Entity>) entityClass.getSuperclass());
			entityRenderMap.put(entityClass, render);
		}

		return render;
	}

	public Render getEntityRenderObject(Entity entity) {
		return getEntityClassRenderObject(entity.getClass());
	}

	public boolean renderEntity(Entity entity, float partialTickTime, boolean flag) {
		if (entity.ticksExisted == 0) {
			entity.lastTickPosX = entity.posX;
			entity.lastTickPosY = entity.posY;
			entity.lastTickPosZ = entity.posZ;
		}

		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTickTime;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTickTime;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTickTime;
		float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTickTime;
		int col = entity.getBrightnessForRender();
		int u = col % 65536;
		int v = col / 65536;
		OpenGLHelper.setLightmapTextureCoords(OpenGLHelper.lightmapTexUnit, u / 1.0f, v / 1.0f);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		return renderEntityWithPosYaw(entity, x - renderPosX, y - renderPosY, z - renderPosZ, yaw, partialTickTime, flag);
	}

	public boolean renderEntity(Entity entity, float partialTickTime) {
		return renderEntity(entity, partialTickTime, false);
	}

	public boolean renderEntityWithPosYaw(Entity entity, double x, double y, double z, float yaw, float partialTickTime, boolean flag) {
		Render render = null;

		try {
			render = getEntityRenderObject(entity);

			if (render != null && renderEngine != null) {
				if (!render.a() || flag) {
					try {
						render.doRender(entity, x, y, z, yaw, partialTickTime);
					} catch (Throwable e) {
						throw new ReportedException(CrashReport.makeCrashReport(e, "Rendering entity in level"));
					}

					if (a && !entity.isInvisible() && !flag) {
						try {
							renderEntityHitbox(entity, x, y, z, yaw, partialTickTime);
						} catch (Throwable e) {
							throw new ReportedException(CrashReport.makeCrashReport(e, "Rendering entity hitbox in level"));
						}
					}
				}
			} else if (renderEngine != null) return false;

			return true;
		} catch (Throwable e) {
			CrashReport crashReport = CrashReport.makeCrashReport(e, "Rendering entity in level");
			throw new ReportedException(crashReport);
		}
	}

	private void renderEntityHitbox(Entity entity, double x, double y, double z, float yaw, float partialTickTime) {
		glDepthMask(false);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_LIGHTING);
		glDisable(GL_CULL_FACE);
		glDisable(GL_BLEND);
		float r = entity.width / 2.0f;
		AxisAlignedBB box = AxisAlignedBB.getAABBPool().getAABB(x - (double) r, y, z - (double) r, x + (double) r, y + (double) entity.height, z + (double) r);
		RenderGlobal.drawOutlinedBoundingBox(box, 16777215);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_LIGHTING);
		glEnable(GL_CULL_FACE);
		glDisable(GL_BLEND);
		glDepthMask(true);
	}

	public void cachaActiveRenderInfo(Level level, RenderEngine renderEngine, FontRenderer fontRenderer, EntityLiving renderViewEntity, Entity pointedEntity, GameSettings gameSettings, float partialTickTime) {
		levelObj = level;
		this.renderEngine = renderEngine;
		options = gameSettings;

		entityViewX = renderViewEntity.prevRotationPitch + (renderViewEntity.rotationPitch - renderViewEntity.prevRotationPitch) * partialTickTime;
		entityViewY = renderViewEntity.prevRotationYaw + (renderViewEntity.rotationYaw - renderViewEntity.prevRotationYaw) * partialTickTime;

		if (gameSettings.thirdPersonView == 2) entityViewY += 180.0f;

		viewerPosX = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * (double) partialTickTime;
		viewerPosY = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * (double) partialTickTime;
		viewerPosZ = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * (double) partialTickTime;
	}

	public void setLevel(Level level) {
		levelObj = level;
	}

	public void updateIcons(IconRegister iconRegister) {
		Iterator<Render> entityRenderMapIterator = entityRenderMap.values().iterator();

		while (entityRenderMapIterator.hasNext()) {
			Render r = entityRenderMapIterator.next();
			r.updateIcons(iconRegister);
		}
	}
}
