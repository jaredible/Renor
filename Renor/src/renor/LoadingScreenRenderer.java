package renor;

import static org.lwjgl.opengl.GL11.*;
import renor.misc.IProgressUpdate;
import renor.misc.ScaledResolution;
import renor.util.OpenGLHelper;
import renor.util.Tessellator;
import renor.util.throwable.RenorError;

public class LoadingScreenRenderer implements IProgressUpdate {
	private String currentDisplayedText = "";
	private String progressMessage;
	private Renor renor;
	private long systemTime = Renor.getSystemTime();
	private boolean a;

	public LoadingScreenRenderer(Renor renor) {
		this.renor = renor;
	}

	public void setupOverlayRendering(String message) {
		currentDisplayedText = message;

		if (!renor.running) {
			if (!a) throw new RenorError();
		} else {
			ScaledResolution res = new ScaledResolution(renor.gameSettings, renor.displayWidth, renor.displayHeight);
			glClear(GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(0.0, res.getScaledWidth_double(), res.getScaledHeight_double(), 0.0, 100.0, 300.0);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glTranslatef(0.0f, 0.0f, -200.0f);
		}
	}

	public void displayProgressMessage(String message) {
		a = true;
		setupOverlayRendering(message);
	}

	public void resetProgressAndMessage(String message) {
		a = false;
		setupOverlayRendering(message);
	}

	public void resetProgressAndWorkingMessage(String message) {
		if (!renor.running) {
			if (!a) throw new RenorError();
		} else {
			systemTime = 0;
			progressMessage = message;
			setLoadingProgress(-1);
			systemTime = 0;
		}
	}

	public void setLoadingProgress(int progress) {
		if (!renor.running) {
			if (!a) throw new RenorError();
		} else {
			// currentSystemTime
			long cst = Renor.getSystemTime();

			if (cst - systemTime >= 100) {
				systemTime = cst;
				ScaledResolution res = new ScaledResolution(renor.gameSettings, renor.displayWidth, renor.displayHeight);
				int w = res.getScaledWidth();
				int h = res.getScaledHeight();
				glClear(GL_DEPTH_BUFFER_BIT);
				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();
				glOrtho(0.0, res.getScaledWidth_double(), res.getScaledHeight_double(), 0.0, 100.0, 300.0);
				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();
				glTranslatef(0.0f, 0.0f, -200.0f);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				Tessellator tess = Tessellator.instance;
				renor.renderEngine.bindTexture("/textures/blocks/grass.png");
				float n = 32.0f;
				tess.startDrawingQuads();
				tess.setColorOpaque_I(4210752);
				tess.addVertexWithUV(0.0, (double) h, 0.0, 0.0, (double) ((float) h / n));
				tess.addVertexWithUV((double) w, (double) h, 0.0, (double) ((float) w / n), (double) ((float) h / n));
				tess.addVertexWithUV((double) w, 0.0, 0.0, (double) ((float) w / n), 0.0);
				tess.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 0.0);
				tess.draw();

				if (progress >= 0) {
					byte max = 100;
					byte hh = 2;
					int x = w / 2 - max / 2;
					int y = h / 2 + 16;
					glDisable(GL_TEXTURE_2D);
					tess.startDrawingQuads();
					tess.setColorOpaque_I(8421504);
					tess.addVertex((double) x, (double) y, 0.0);
					tess.addVertex((double) x, (double) (y + hh), 0.0);
					tess.addVertex((double) (x + max), (double) (y + hh), 0.0);
					tess.addVertex((double) (x + max), (double) y, 0.0);
					tess.setColorOpaque_I(8454016);
					tess.addVertex((double) x, (double) y, 0.0);
					tess.addVertex((double) x, (double) (y + hh), 0.0);
					tess.addVertex((double) (x + progress), (double) (y + hh), 0.0);
					tess.addVertex((double) (x + progress), (double) y, 0.0);
					tess.draw();
					glEnable(GL_TEXTURE_2D);
				}

				glEnable(GL_BLEND);
				OpenGLHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
				renor.fontRenderer.drawStringWithShadow(currentDisplayedText, (w - renor.fontRenderer.getStringWidth(currentDisplayedText)) / 2, h / 2 - 4 - 16, 16777215);
				renor.fontRenderer.drawStringWithShadow(progressMessage, (w - renor.fontRenderer.getStringWidth(progressMessage)) / 2, h / 2 - 4 + 8, 16777215);

				renor.updateDisplay();

				try {
					Thread.yield();
				} catch (Exception e) {
					;
				}
			}
		}
	}

	public void onNoMoreProgress() {
	}
}
