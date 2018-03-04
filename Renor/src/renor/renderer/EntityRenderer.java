package renor.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.Project.gluPerspective;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GLContext;

import renor.Renor;
import renor.aabb.AxisAlignedBB;
import renor.level.LevelClient;
import renor.level.entity.Entity;
import renor.level.entity.EntityLiving;
import renor.level.entity.EntityPlayer;
import renor.level.material.Material;
import renor.misc.ActiveRenderInfo;
import renor.misc.Frustum;
import renor.misc.GLAllocation;
import renor.misc.MouseFilter;
import renor.misc.MovingObjectPosition;
import renor.misc.ScaledResolution;
import renor.util.ClippingHelperImpl;
import renor.util.MathHelper;
import renor.util.OpenGLHelper;
import renor.util.RenderHelper;
import renor.util.crashreport.CrashReport;
import renor.util.throwable.ReportedException;
import renor.vector.Vec3;

public class EntityRenderer {
	FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
	private Renor renor;
	private ItemRenderer itemRenderer;
	private MouseFilter mouseFilterXAxis = new MouseFilter();
	private MouseFilter mouseFilterYAxis = new MouseFilter();
	private Entity pointedEntity = null;
	private long prevFrameTime = Renor.getSystemTime();
	private long renderEndNanoTime = 0;
	public static int anaglyphField;
	private int rendererUpdateCount;
	public int lightmapTexture;
	private int[] lightmapColors;
	private double cameraZoom = 1.0;
	private double cameraYaw = 0.0;
	private double cameraPitch = 0.0;
	private float farPlaneDistance = 0.0f;
	private float smoothCamYaw;
	private float smoothCamPitch;
	private float smoothCamFilterX;
	private float smoothCamFilterY;
	private float smoothCamPartialTicks;
	float lightFlickerX = 0.0f;
	float lightFlickerDX = 0.0f;
	float lightFlickerY = 0.0f;
	float lightFlickerDY = 0.0f;
	private float fovModifierHand;
	private float prevFovModifierHand;
	private float fovMultiplierTemp;
	private float thirdPersonDistance = 4.0f;
	private float thirdPersonDistanceTemp = 4.0f;
	public float fogColorRed;
	public float fogColorGreen;
	public float fogColorBlue;
	private float fogBrightness;
	private float prevFogBrightness;
	public static boolean anaglyphEnabled;
	private boolean lightmapUpdateNeeded;

	public EntityRenderer(Renor renor) {
		this.renor = renor;

		itemRenderer = new ItemRenderer(renor);
		lightmapTexture = renor.renderEngine.allocateAndSetupTexture(new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB));
		lightmapColors = new int[256];
	}

	public void updateRenderer() {
		updateFOVModifierHand();
		updateLightFlicker();
		prevFogBrightness = fogBrightness;
		thirdPersonDistanceTemp = thirdPersonDistance;
		float v0;
		float v1;

		if (renor.gameSettings.smoothCamera) {
			v0 = renor.gameSettings.mouseSensitivity * 0.6f + 0.2f;
			v1 = v0 * v0 * v0 * 8.0f;
			smoothCamFilterX = mouseFilterXAxis.smooth(smoothCamYaw, 0.05f * v1);
			smoothCamFilterY = mouseFilterYAxis.smooth(smoothCamPitch, 0.05f * v1);
			smoothCamPartialTicks = 0.0f;
			smoothCamYaw = 0.0f;
			smoothCamPitch = 0.0f;
		}

		if (renor.renderViewEntity == null) renor.renderViewEntity = renor.thePlayer;

		v0 = renor.theLevel.getLightBrightness(MathHelper.floor_double(renor.renderViewEntity.posX), MathHelper.floor_double(renor.renderViewEntity.posY), MathHelper.floor_double(renor.renderViewEntity.posZ));
		v1 = (float) (renor.gameSettings.renderDistanceChunks / 16);
		float v2 = v0 * (1.0f - v1) + v1;
		fogBrightness += (v2 - fogBrightness) * 0.1f;
		++rendererUpdateCount;
		itemRenderer.updateEquippedItem();
	}

	// TODO check this!
	public void getMouseOver(float partialTickTime) {
		if (renor.renderViewEntity != null) {
			if (renor.theLevel != null) {
				renor.pointedEntity = null;
				// renderDistance
				double rd = renor.playerController.getBlockReachDistance();
				renor.objectMouseOver = renor.renderViewEntity.rayTrace(rd, partialTickTime);
				// tempRenderDistance
				double trd = rd;
				// position
				Vec3 p = renor.renderViewEntity.getPosition(partialTickTime);

				if (renor.playerController.extendedReach()) {
					rd = 6.0;
					trd = 6.0;
				} else {
					if (rd > 3.0) trd = 3.0;

					rd = trd;
				}

				if (renor.objectMouseOver != null) trd = renor.objectMouseOver.hitVec.distanceTo(p);

				Vec3 look = renor.renderViewEntity.getLook(partialTickTime);
				Vec3 v0 = p.addVector(look.xCoord * rd, look.yCoord * rd, look.zCoord * rd);
				pointedEntity = null;
				float r = 1.0f;
				// entities
				List<Entity> es = renor.theLevel.getEntitiesWithinAABBExcludingEntity(renor.renderViewEntity, renor.renderViewEntity.boundingBox.addCoord(look.xCoord * rd, look.yCoord * rd, look.zCoord * rd).expand((double) r, (double) r, (double) r));
				double v1 = trd;

				for (int i = 0; i < es.size(); ++i) {
					Entity e = es.get(i);

					if (e.canBeCollidedWith()) {
						// border
						float b = e.getCollisionBorderSize();
						AxisAlignedBB v2 = e.boundingBox.expand((double) b, (double) b, (double) b);
						MovingObjectPosition v3 = v2.calculateIntercept(p, v0);

						if (v2.isVecInside(p)) {
							if (0.0 < v1 || v1 == 0.0) {
								pointedEntity = e;
								v1 = 0.0;
							}
						} else if (v3 != null) {
							double v4 = p.distanceTo(v3.hitVec);

							if (v4 < v1 || v1 == 0.0) {
								pointedEntity = e;
								v1 = v4;
							}
						}
					}
				}

				if (pointedEntity != null && (v1 < trd || renor.objectMouseOver == null)) {
					renor.objectMouseOver = new MovingObjectPosition(pointedEntity);

					if (pointedEntity instanceof EntityLiving) renor.pointedEntity = pointedEntity;
				}
			}
		}
	}

	private void updateFOVModifierHand() {
		// player
		EntityPlayer p = (EntityPlayer) renor.renderViewEntity;
		fovMultiplierTemp = p.getFOVMultiplier();
		prevFovModifierHand = fovModifierHand;
		fovModifierHand += (fovMultiplierTemp - fovModifierHand) * 0.5f;

		if (fovModifierHand < 0.1f) fovModifierHand = 0.1f;
		if (fovModifierHand > 1.5f) fovModifierHand = 1.5f;
	}

	private float getFOVModifier(float partialTickTime, boolean flag) {
		// player
		EntityPlayer p = (EntityPlayer) renor.renderViewEntity;
		float fov = 70.0f;

		if (flag) {
			fov += renor.gameSettings.fov * 40.0f;
			fov *= prevFovModifierHand + (fovModifierHand - prevFovModifierHand) * partialTickTime;
		}

		if (p.getHealth() <= 0.0f) {
			// time
			float t = (float) p.deathTime + partialTickTime;
			fov /= (1.0f - 500.0f / (t + 500.0f)) * 2.0f + 1.0f;
		}

		return fov;
	}

	private void hurtCameraEffect(float partialTickTime) {
		// living
		EntityLiving l = renor.renderViewEntity;
		float v0 = (float) l.hurtTime - partialTickTime;
		float v1;

		if (l.getHealth() <= 0.0f) {
			v1 = (float) l.deathTime + partialTickTime;
			glRotatef(40.0f - 8000.0f / (v1 + 200.0f), 0.0f, 0.0f, 1.0f);
		}

		if (v0 >= 0.0f) {
			v0 /= (float) l.maxHurtTime;
			v0 = MathHelper.sin(v0 * v0 * v0 * v0 * (float) Math.PI);
			v1 = l.attackedAtYaw;
			glRotatef(-v1, 0.0f, 1.0f, 0.0f);
			glRotatef(-v0 * 14.0f, 0.0f, 0.0f, 1.0f);
			glRotatef(v1, 0.0f, 1.0f, 0.0f);
		}
	}

	private void setupViewBobbing(float partialTickTime) {
		if (renor.renderViewEntity instanceof EntityPlayer) {
			// player
			EntityPlayer p = (EntityPlayer) renor.renderViewEntity;
			float d = p.distanceWalkedModified - p.prevDistanceWalkedModified;
			float dw = -(p.distanceWalkedModified + d * partialTickTime);
			float yaw = p.prevCameraYaw + (p.cameraYaw - p.prevCameraYaw) * partialTickTime;
			float pitch = p.prevCameraPitch + (p.cameraPitch - p.prevCameraPitch) * partialTickTime;
			glTranslatef(MathHelper.sin(dw * (float) Math.PI) * yaw * 0.5f, -Math.abs(MathHelper.cos(dw * (float) Math.PI) * yaw), 0.0f);
			glRotatef(MathHelper.sin(dw * (float) Math.PI) * yaw * 3.0f, 0.0f, 0.0f, 1.0f);
			glRotatef(Math.abs(MathHelper.cos(dw * (float) Math.PI - 0.2f) * yaw) * 5.0f, 1.0f, 0.0f, 0.0f);
			glRotatef(pitch, 1.0f, 0.0f, 0.0f);
		}
	}

	private void orientCamera(float partialTickTime) {
		// living
		EntityLiving l = renor.renderViewEntity;
		float yOffset = l.yOffset - 1.62f;
		double x = l.prevPosX + (l.posX - l.prevPosX) * (double) partialTickTime;
		double y = l.prevPosY + (l.posY - l.prevPosY) * (double) partialTickTime - (double) yOffset;
		double z = l.prevPosZ + (l.posZ - l.prevPosZ) * (double) partialTickTime;

		if (renor.gameSettings.thirdPersonView > 0) {
			// distance
			double d = (double) (thirdPersonDistanceTemp + (thirdPersonDistance - thirdPersonDistanceTemp) * partialTickTime);
			float yaw = l.rotationYaw;
			float pitch = l.rotationPitch;

			if (renor.gameSettings.thirdPersonView == 2) pitch += 180.0f;

			double xx = (double) (-MathHelper.sin(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI)) * d;
			double yy = (double) (MathHelper.cos(yaw / 180.0f * (float) Math.PI) * MathHelper.cos(pitch / 180.0f * (float) Math.PI)) * d;
			double zz = (double) (-MathHelper.sin(pitch / 180.0f * (float) Math.PI)) * d;

			for (int i = 0; i < 8; ++i) {
				float xo = (float) ((i & 1) * 2 - 1);
				float yo = (float) ((i >> 1 & 1) * 2 - 1);
				float zo = (float) ((i >> 2 & 1) * 2 - 1);
				xo *= 0.1f;
				yo *= 0.1f;
				zo *= 0.1f;
				MovingObjectPosition v0 = renor.theLevel.rayTraceBlocks(renor.theLevel.getLevelVec3Pool().getVecFromPool(x + (double) xo, y + (double) yo, z + (double) zo), renor.theLevel.getLevelVec3Pool().getVecFromPool(x - xx + (double) xo + (double) zo, y - zz + (double) yo, z - yy + (double) zo));

				if (v0 != null) {
					double dd = v0.hitVec.distanceTo(renor.theLevel.getLevelVec3Pool().getVecFromPool(x, y, z));

					if (dd < d) d = dd;
				}
			}

			if (renor.gameSettings.thirdPersonView == 2) glRotatef(180.0f, 0.0f, 1.0f, 0.0f);

			glRotatef(l.rotationPitch - pitch, 1.0f, 0.0f, 0.0f);
			glRotatef(l.rotationYaw - yaw, 0.0f, 1.0f, 0.0f);
			glTranslatef(0.0f, 0.0f, (float) (-d));
			glRotatef(yaw - l.rotationYaw, 0.0f, 1.0f, 0.0f);
			glRotatef(pitch - l.rotationPitch, 1.0f, 0.0f, 0.0f);
		} else glTranslatef(0.0f, 0.0f, -0.1f);

		glRotatef(l.prevRotationPitch + (l.rotationPitch - l.prevRotationPitch) * partialTickTime, 1.0f, 0.0f, 0.0f);
		glRotatef(l.prevRotationYaw + (l.rotationYaw - l.prevRotationYaw) * partialTickTime + 180.0f, 0.0f, 1.0f, 0.0f);

		glTranslatef(0.0f, yOffset, 0.0f);
	}

	private void setupCameraTransform(float partialTickTime, int flag) {
		farPlaneDistance = (float) (renor.gameSettings.renderDistanceChunks * 16);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float n = 0.07f;

		if (renor.gameSettings.anaglyph) glTranslatef((float) (-(flag * 2 - 1)) * n, 0.0f, 0.0f);

		if (cameraZoom != 1.0) {
			glTranslated((float) cameraYaw, (float) (-cameraPitch), 0.0);
			glScaled(cameraZoom, cameraZoom, 1.0);
		}

		gluPerspective(getFOVModifier(partialTickTime, true), (float) renor.displayWidth / (float) renor.displayHeight, 0.05f, farPlaneDistance * 2.0f);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		if (renor.gameSettings.anaglyph) glTranslatef((float) (flag * 2 - 1) * 0.1f, 0.0f, 0.0f);

		hurtCameraEffect(partialTickTime);

		if (renor.gameSettings.viewBobbing) setupViewBobbing(partialTickTime);

		orientCamera(partialTickTime);
	}

	private void renderHand(float partialTickTime, int flag) {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float n = 0.07f;

		if (renor.gameSettings.anaglyph) glTranslatef((float) (-(flag * 2 - 1)) * n, 0.0f, 0.0f);

		if (cameraZoom != 1.0) {
			glTranslated((float) cameraYaw, (float) (-cameraPitch), 0.0);
			glScaled(cameraZoom, cameraZoom, 1.0);
		}

		gluPerspective(getFOVModifier(partialTickTime, false), (float) renor.displayWidth / (float) renor.displayHeight, 0.05f, farPlaneDistance * 2.0f);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		if (renor.gameSettings.anaglyph) glTranslatef((float) (flag * 2 - 1) * 0.1f, 0.0f, 0.0f);

		glPushMatrix();
		hurtCameraEffect(partialTickTime);

		if (renor.gameSettings.viewBobbing) setupViewBobbing(partialTickTime);

		if (renor.gameSettings.thirdPersonView == 0 && !renor.gameSettings.hideGUI) {
			enableLightmap((double) partialTickTime);
			itemRenderer.renderItemInFirstPerson(partialTickTime);
			disableLightmap((double) partialTickTime);
		}

		glPopMatrix();

		if (renor.gameSettings.viewBobbing) setupViewBobbing(partialTickTime);
	}

	public void enableLightmap(double partialTickTime) {
		OpenGLHelper.setActiveTexture(OpenGLHelper.lightmapTexUnit);
		glMatrixMode(GL_TEXTURE);
		glLoadIdentity();
		float n = 0.00390625f;
		glScalef(n, n, n);
		glTranslatef(8.0f, 8.0f, 8.0f);
		glMatrixMode(GL_MODELVIEW);
		glBindTexture(GL_TEXTURE_2D, lightmapTexture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		glEnable(GL_TEXTURE_2D);
		renor.renderEngine.resetBoundTexture();
		OpenGLHelper.setActiveTexture(OpenGLHelper.defaultTexUnit);
	}

	public void disableLightmap(double partialTickTime) {
		OpenGLHelper.setActiveTexture(OpenGLHelper.lightmapTexUnit);
		glDisable(GL_TEXTURE_2D);
		OpenGLHelper.setActiveTexture(OpenGLHelper.defaultTexUnit);
	}

	public void updateLightFlicker() {
		lightFlickerDX = (float) ((double) lightFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
		lightFlickerDY = (float) ((double) lightFlickerDY + (Math.random() - Math.random()) * Math.random() * Math.random());
		lightFlickerDX = (float) ((double) lightFlickerDX * 0.9);
		lightFlickerDY = (float) ((double) lightFlickerDY * 0.9);
		lightFlickerX += (lightFlickerDX - lightFlickerX) * 1.0f;
		lightFlickerY += (lightFlickerDY - lightFlickerY) * 1.0f;
		lightmapUpdateNeeded = true;
	}

	public void updateLightmap(float partialTickTime) {
		// levelClient
		LevelClient lc = renor.theLevel;

		if (lc != null) {
			for (int i = 0; i < 256; ++i) {
				float v0 = lc.getSunBrightness(1.0f) * 0.95f + 0.05f;
				float h = lc.provider.lightBrightnessTable[i % 16] * (lightFlickerX * 0.1f + 1.5f);
				float v = lc.provider.lightBrightnessTable[i / 16] * v0;
				float v1 = v * (lc.getSunBrightness(1.0f) * 0.65f + 0.35f);
				float v2 = v * (lc.getSunBrightness(1.0f) * 0.65f + 0.35f);
				float v3 = h * ((h * 0.6f + 0.4f) * 0.6f + 0.4f);
				float v4 = h * (h * h * 0.6f + 0.4f);
				float r = v1 + h;
				float g = v2 + v3;
				float b = v + v4;
				r = r * 0.96f + 0.03f;
				g = g * 0.96f + 0.03f;
				b = b * 0.96f + 0.03f;
				float v5;

				boolean test = false;
				if (test) {
					r = 0.22f + h * 0.75f;
					g = 0.28f + v3 * 0.75f;
					b = 0.25f + v4 * 0.75f;
				}

				float v6;

				if (r > 1.0f) r = 1.0f;
				if (g > 1.0f) g = 1.0f;
				if (b > 1.0f) b = 1.0f;

				v5 = renor.gameSettings.gamma;
				v6 = 1.0f - r;
				float v7 = 1.0f - g;
				float v8 = 1.0f - b;
				v6 = 1.0f - v6 * v6 * v6 * v6;
				v7 = 1.0f - v7 * v7 * v7 * v7;
				v8 = 1.0f - v8 * v8 * v8 * v8;
				r = r * (1.0f - v5) + v6 * v5;
				g = g * (1.0f - v5) + v7 * v5;
				b = b * (1.0f - v5) + v8 * v5;
				r = r * 0.96f + 0.03f;
				g = g * 0.96f + 0.03f;
				b = b * 0.96f + 0.03f;

				if (r < 0.0f) r = 0.0f;
				if (g < 0.0f) g = 0.0f;
				if (b < 0.0f) b = 0.0f;
				if (r > 1.0f) r = 1.0f;
				if (g > 1.0f) g = 1.0f;
				if (b > 1.0f) b = 1.0f;

				short alpha = 255;
				int red = (int) (r * 255.0f);
				int green = (int) (g * 255.0f);
				int blue = (int) (b * 255.0f);
				lightmapColors[i] = alpha << 24 | red << 16 | green << 8 | blue;
			}

			renor.renderEngine.createTextureFromBytes(lightmapColors, 16, 16, lightmapTexture);
			lightmapUpdateNeeded = false;
		}
	}

	public void updateCameraAndRender(float partialTickTime) {
		renor.theProfiler.startSection("lightTex");

		if (lightmapUpdateNeeded) updateLightmap(partialTickTime);

		renor.theProfiler.endSection();
		boolean active = Display.isActive();

		if (!active && renor.gameSettings.pauseOnLostFocus && (!renor.gameSettings.touchscreen || !Mouse.isButtonDown(1))) {
			if (Renor.getSystemTime() - prevFrameTime > 500) renor.displayIngameMenu();
		} else prevFrameTime = Renor.getSystemTime();

		renor.theProfiler.startSection("mouse");

		if (renor.inGameHasFocus && active) {
			renor.mouseHelper.mouseXYChange();
			float inc = renor.gameSettings.mouseSensitivity * 0.6f + 0.2f;
			float l = inc * inc * inc * 8.0f;
			float yaw = (float) renor.mouseHelper.deltaX * l;
			float pitch = (float) renor.mouseHelper.deltaY * l;
			byte invert = 1;

			if (renor.gameSettings.invertMouse) invert = -1;

			if (renor.gameSettings.smoothCamera) {
				smoothCamYaw += yaw;
				smoothCamPitch += pitch;
				// passedTickTime
				float pt = partialTickTime - smoothCamPartialTicks;
				smoothCamPartialTicks = partialTickTime;
				yaw = smoothCamFilterX * pt;
				pitch = smoothCamFilterY * pt;
				renor.thePlayer.setAngles(yaw, pitch * (float) invert);
			} else renor.thePlayer.setAngles(yaw, pitch * (float) invert);
		}

		renor.theProfiler.endSection();

		if (!renor.skipRenderLevel) {
			anaglyphEnabled = renor.gameSettings.anaglyph;
			final ScaledResolution res = new ScaledResolution(renor.gameSettings, renor.displayWidth, renor.displayHeight);
			int w = res.getScaledWidth();
			int h = res.getScaledHeight();
			final int mx = Mouse.getX() * w / renor.displayWidth;
			final int my = h - Mouse.getY() * h / renor.displayHeight - 1;
			// frameRateLimit
			int fr = renor.gameSettings.limitFramerate;

			if (renor.theLevel != null) {
				renor.theProfiler.startSection("level");

				if (renor.isFrameRateLimitBelowMax()) renderLevel(partialTickTime, renderEndNanoTime + (long) (1000000000 / fr));
				else renderLevel(partialTickTime, 0);

				renderEndNanoTime = System.nanoTime();
				renor.theProfiler.endStartSection("gui");

				if (!renor.gameSettings.hideGUI || renor.currentScreen != null) {
					glAlphaFunc(GL_GREATER, 0.1f);
					renor.guiIngame.renderGameOverlay(partialTickTime, mx, my);
				}

				renor.theProfiler.endSection();
			} else {
				glViewport(0, 0, renor.displayWidth, renor.displayHeight);
				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();
				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();
				setupOverlayRendering();
				renderEndNanoTime = System.nanoTime();
			}

			if (renor.currentScreen != null) {
				glClear(GL_DEPTH_BUFFER_BIT);

				try {
					renor.currentScreen.drawScreen(mx, my, partialTickTime);
				} catch (Throwable e) {
					CrashReport crashReport = CrashReport.makeCrashReport(e, "Rendering screen");
					throw new ReportedException(crashReport);
				}
			}
		}
	}

	public void renderLevel(float partialTickTime, long nanoTime) {
		renor.theProfiler.startSection("lightTex");

		if (lightmapUpdateNeeded) updateLightmap(partialTickTime);

		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_ALPHA_TEST);
		glAlphaFunc(GL_GREATER, 0.5f);

		if (renor.renderViewEntity == null) renor.renderViewEntity = renor.thePlayer;

		renor.theProfiler.endStartSection("pick");
		getMouseOver(partialTickTime);
		// living
		EntityLiving l = renor.renderViewEntity;
		// renderGlobal
		RenderGlobal rg = renor.renderGlobal;
		// effectRenderer
		EffectRenderer ef = renor.effectRenderer;
		double x = l.lastTickPosX + (l.posX - l.lastTickPosX) * (double) partialTickTime;
		double y = l.lastTickPosY + (l.posY - l.lastTickPosY) * (double) partialTickTime;
		double z = l.lastTickPosZ + (l.posZ - l.lastTickPosZ) * (double) partialTickTime;
		renor.theProfiler.endStartSection("center");

		for (int i = 0; i < 2; ++i) {
			if (renor.gameSettings.anaglyph) {
				anaglyphField = i;

				if (anaglyphField == 0) glColorMask(false, true, true, false);
				else glColorMask(true, false, false, false);
			}

			renor.theProfiler.endStartSection("clear");
			glViewport(0, 0, renor.displayWidth, renor.displayHeight);
			updateFogColor(partialTickTime);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glEnable(GL_CULL_FACE);
			renor.theProfiler.endStartSection("camera");
			setupCameraTransform(partialTickTime, i);
			ActiveRenderInfo.updateRenderInfo(renor.thePlayer, renor.gameSettings.thirdPersonView == 2);
			renor.theProfiler.endStartSection("frustum");
			ClippingHelperImpl.getInstance();

			if (renor.gameSettings.renderDistanceChunks >= 4) {
				setupFog(-1, partialTickTime);
				renor.theProfiler.endStartSection("sky");
				rg.renderSky(partialTickTime);
			}

			glEnable(GL_FOG);
			setupFog(0, partialTickTime);

			if (renor.gameSettings.ambientOcclusion) glShadeModel(GL_SMOOTH);

			renor.theProfiler.endStartSection("culling");
			// frustum
			Frustum f = new Frustum();
			f.setPosition(x, y, z);
			renor.renderGlobal.clipRenderersByFrustrum(f);

			if (i == 0) {
				renor.theProfiler.endStartSection("updatechunks");

				while (!renor.renderGlobal.updateRenderers(l, false) && nanoTime != 0) {
					// passedNanoTime
					long n = nanoTime - System.nanoTime();

					if (n < 0 || n > 1000000000) break;
				}
			}

			renor.theProfiler.endStartSection("prepareterrain");
			setupFog(0, partialTickTime);
			glEnable(GL_FOG);
			renor.renderEngine.bindTexture("/textures/blocks/grass.png");
			RenderHelper.disableStandardLighting();
			renor.theProfiler.endStartSection("terrain");
			glMatrixMode(GL_MODELVIEW);
			glPushMatrix();
			rg.sortAndRender(l, 0, (double) partialTickTime);
			glShadeModel(GL_FLAT);
			// player
			EntityPlayer p;

			glMatrixMode(GL_MODELVIEW);
			glPopMatrix();
			glPushMatrix();
			RenderHelper.enableStandardLighting();
			renor.theProfiler.endStartSection("entities");
			rg.renderEntities(l, f, partialTickTime);
			enableLightmap((double) partialTickTime);
			renor.theProfiler.endStartSection("litParticles");
			ef.renderLitParticles(l, partialTickTime);
			RenderHelper.disableStandardLighting();
			setupFog(0, partialTickTime);
			renor.theProfiler.endStartSection("particles");
			ef.renderParticles(l, partialTickTime);
			disableLightmap((double) partialTickTime);
			glMatrixMode(GL_MODELVIEW);
			glPopMatrix();
			glPushMatrix();

			if (renor.objectMouseOver != null && l.isInsideOfMaterial(Material.ice) && l instanceof EntityPlayer && !renor.gameSettings.hideGUI) {
				p = (EntityPlayer) l;
				glDisable(GL_ALPHA_TEST);
				renor.theProfiler.endStartSection("outline");
				rg.drawSelectionBox(p, renor.objectMouseOver, partialTickTime);
				glEnable(GL_ALPHA_TEST);
			}

			glMatrixMode(GL_MODELVIEW);
			glPopMatrix();

			if (cameraZoom == 1.0 && l instanceof EntityPlayer && !renor.gameSettings.hideGUI && renor.objectMouseOver != null && !l.isInsideOfMaterial(Material.ice)) {
				p = (EntityPlayer) l;
				glDisable(GL_ALPHA_TEST);
				renor.theProfiler.endStartSection("outline");
				rg.drawSelectionBox(p, renor.objectMouseOver, partialTickTime);
				glEnable(GL_ALPHA_TEST);
			}

			glDepthMask(true);
			glDisable(GL_BLEND);
			glEnable(GL_CULL_FACE);
			OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
			glAlphaFunc(GL_GREATER, 0.1f);
			setupFog(0, partialTickTime);
			glEnable(GL_BLEND);
			glDepthMask(false);
			renor.renderEngine.bindTexture("/textures/blocks/ice.png");

			boolean test = false;
			if (test) {
				renor.theProfiler.endStartSection("water");

				if (renor.gameSettings.ambientOcclusion) glShadeModel(GL_SMOOTH);

				glEnable(GL_BLEND);
				OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

				if (renor.gameSettings.anaglyph) {
					if (anaglyphField == 0) glColorMask(false, true, true, true);
					else glColorMask(true, false, false, true);

					rg.sortAndRender(l, 1, (double) partialTickTime);
				} else rg.sortAndRender(l, 1, (double) partialTickTime);

				glDisable(GL_BLEND);
				glShadeModel(GL_FLAT);
			} else {
				renor.theProfiler.endStartSection("water");
				rg.sortAndRender(l, 1, (double) partialTickTime);
			}

			glDepthMask(true);
			glEnable(GL_CULL_FACE);
			glDisable(GL_BLEND);
			glDisable(GL_FOG);

			renor.theProfiler.endStartSection("hand");

			if (cameraZoom == 1.0) {
				glClear(GL_DEPTH_BUFFER_BIT);
				renderHand(partialTickTime, i);
			}

			if (!renor.gameSettings.anaglyph) {
				renor.theProfiler.endSection();
				return;
			}
		}

		glColorMask(true, true, true, false);
		renor.theProfiler.endSection();
	}

	public void setupOverlayRendering() {
		ScaledResolution res = new ScaledResolution(renor.gameSettings, renor.displayWidth, renor.displayHeight);
		glClear(GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0.0, res.getScaledWidth_double(), res.getScaledHeight_double(), 0.0, 1000.0, 3000.0);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glTranslatef(0.0f, 0.0f, -2000.0f);
	}

	private void updateFogColor(float partialTickTime) {
		// levelClient
		LevelClient lc = renor.theLevel;
		// living
		EntityLiving l = renor.renderViewEntity;
		float d = 0.25f + 0.75f * (float) renor.gameSettings.renderDistanceChunks / 16.0f;
		d = 1.0f - (float) Math.pow((double) d, 0.25);
		// skyColor
		Vec3 sc = lc.getSkyColor(l, partialTickTime);
		float sr = (float) sc.xCoord;
		float sg = (float) sc.yCoord;
		float sb = (float) sc.zCoord;
		// fogColor
		Vec3 fc = lc.getFogColor(partialTickTime);
		float fr = (float) fc.xCoord;
		float fg = (float) fc.yCoord;
		float fb = (float) fc.zCoord;
		fogColorRed = fr;
		fogColorGreen = fg;
		fogColorBlue = fb;

		if (renor.gameSettings.renderDistanceChunks >= 4) {
			Vec3 v0 = MathHelper.sin(lc.getCelestialAngleRadians(partialTickTime)) > 0.0f ? lc.getLevelVec3Pool().getVecFromPool(-1.0, 0.0, 0.0) : lc.getLevelVec3Pool().getVecFromPool(1.0, 0.0, 0.0);
			float v1 = (float) l.getLook(partialTickTime).dotProduct(v0);

			if (v1 < 0.0f) v1 = 0.0f;

			if (v1 > 0.0f) {
				float[] colors = lc.provider.calcSunriseSunsetColors(lc.getCelestialAngle(partialTickTime), partialTickTime);

				if (colors != null) {
					v1 *= colors[3];
					fogColorRed = fogColorRed * (1.0f - v1) + colors[0] * v1;
					fogColorGreen = fogColorGreen * (1.0f - v1) + colors[1] * v1;
					fogColorBlue = fogColorBlue * (1.0f - v1) + colors[2] * v1;
				}
			}
		}

		fogColorRed += (sr - fogColorRed) * d;
		fogColorGreen += (sg - fogColorGreen) * d;
		fogColorBlue += (sb - fogColorBlue) * d;

		float br = prevFogBrightness + (fogBrightness - prevFogBrightness) * partialTickTime;
		fogColorRed *= br;
		fogColorGreen *= br;
		fogColorBlue *= br;
		double yy = (l.lastTickPosY + (l.posY - l.lastTickPosY) * (double) partialTickTime) * 1.0;

		if (yy < 1.0) {
			if (yy < 0.0) yy = 0.0;

			yy *= yy;
			fogColorRed = (float) ((double) fogColorRed * yy);
			fogColorGreen = (float) ((double) fogColorGreen * yy);
			fogColorBlue = (float) ((double) fogColorBlue * yy);
		}

		if (renor.gameSettings.anaglyph) {
			float ar = (fogColorRed * 30.0f + fogColorGreen * 59.0f + fogColorBlue * 11.0f) / 100.0f;
			float ag = (fogColorRed * 30.0f + fogColorGreen * 70.0f) / 100.0f;
			float ab = (fogColorRed * 30.0f + fogColorBlue * 70.0f) / 100.0f;
			fogColorRed = ar;
			fogColorGreen = ag;
			fogColorBlue = ab;
		}

		glClearColor(fogColorRed, fogColorGreen, fogColorBlue, 0.0f);
	}

	private void setupFog(int mode, float partialTickTime) {
		glFog(GL_FOG_COLOR, setFogColorBuffer(fogColorRed, fogColorGreen, fogColorBlue, 1.0f));
		glNormal3f(0.0f, -1.0f, 0.0f);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float d;

		d = farPlaneDistance;

		glFogi(GL_FOG_MODE, GL_LINEAR);

		// TODO change fog distances?
		if (mode < 0) {
			glFogf(GL_FOG_START, 0.0f);
			glFogf(GL_FOG_END, d);
			// GL11.glFogf(GL11.GL_FOG_END, 0.0f);
			// GL11.glFogf(GL11.GL_FOG_END, d * 0.8f);
		} else {
			glFogf(GL_FOG_START, d * 0.75f);
			glFogf(GL_FOG_END, d);
			// GL11.glFogf(GL11.GL_FOG_START, d * 0.25f);
			// GL11.glFogf(GL11.GL_FOG_END, d);
		}

		if (GLContext.getCapabilities().GL_NV_fog_distance) glFogi(34138, 34139);
		// if (GLContext.getCapabilities().GL_NV_fog_distance)
		// GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV,
		// NVFogDistance.GL_EYE_RADIAL_NV);

		glEnable(GL_COLOR_MATERIAL);
		glColorMaterial(GL_FRONT, GL_AMBIENT);
	}

	private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha) {
		fogColorBuffer.clear();
		fogColorBuffer.put(red).put(green).put(blue).put(alpha);
		fogColorBuffer.flip();
		return fogColorBuffer;
	}
}
