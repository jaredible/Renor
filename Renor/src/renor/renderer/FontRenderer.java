package renor.renderer;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;

import renor.misc.ChatAllowedCharacters;
import renor.misc.GameSettings;
import renor.util.IReloadListener;
import renor.util.Tessellator;

public class FontRenderer implements IReloadListener {
	private final String fontTextureName;
	public Random fontRandom = new Random();
	private final RenderEngine renderEngine;
	public static final int FONT_HEIGHT = 9;
	private int textColor;
	private int[] charWidth = new int[256];
	private int[] colorCode = new int[32];
	private float posX;
	private float posY;
	private float red;
	private float green;
	private float blue;
	private float alpha;
	private boolean unicodeFlag;
	private boolean randomStyle = false;
	private boolean boldStyle = false;
	private boolean italicStyle = false;
	private boolean underlineStyle = false;
	private boolean strikethroughStyle = false;

	public FontRenderer(GameSettings gameSettings, RenderEngine renderEngine, String textureName, boolean unicodeFlag) {
		this.renderEngine = renderEngine;
		fontTextureName = textureName;
		this.unicodeFlag = unicodeFlag;

		readFontData();
		renderEngine.bindTexture(textureName);

		for (int i = 0; i < 32; ++i) {
			int o = (i >> 3 & 1) * 85;
			int r = (i >> 2 & 1) * 170 + o;
			int g = (i >> 1 & 1) * 170 + o;
			int b = (i >> 0 & 1) * 170 + o;

			if (i == 6) r += 85;

			if (gameSettings.anaglyph) {
				int ar = (r * 30 + g * 59 + b * 11) / 100;
				int ag = (r * 30 + g * 70) / 100;
				int ab = (r * 30 + b * 70) / 100;
				r = ar;
				g = ag;
				b = ab;
			}

			if (i >= 16) {
				r /= 4;
				g /= 4;
				b /= 4;
			}

			colorCode[i] = (r & 255) << 16 | (g & 255) << 8 | b & 255;
		}
	}

	public void readFontData() {
		readFontTexture(fontTextureName);
	}

	public void readFontTexture(String textureName) {
		BufferedImage fontImage;

		try {
			// ?
			fontImage = ImageIO.read(RenderEngine.class.getResourceAsStream(textureName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		int width = fontImage.getWidth();
		int height = fontImage.getHeight();
		int[] data = new int[width * height];
		fontImage.getRGB(0, 0, width, height, data, 0, width);
		int w = height / 16;
		int h = width / 16;
		byte ext = 1;
		float inc = 8.0f / (float) h;
		int i = 0;

		while (i < 256) {
			int x = i % 16;
			int y = i / 16;

			if (i == 32) charWidth[i] = 3 + ext;

			int dw = h - 1;

			while (true) {
				if (dw >= 0) {
					int xx = x * h + dw;
					boolean flag = true;

					for (int j = 0; j < w && flag; ++j) {
						int yy = (y * h + j) * width;

						if ((data[xx + yy] >> 24 & 255) != 0) flag = false;
					}

					if (flag) {
						--dw;
						continue;
					}
				}

				++dw;
				charWidth[i] = (int) (0.5 + (double) ((float) dw * inc)) + ext;
				++i;
				break;
			}
		}
	}

	private float renderCharAtPos(int ch, char c, boolean flag) {
		return c == 32 ? 4.0f : (ch > 0 && !unicodeFlag ? renderDefaultChar(ch + 32, flag) : renderUnicodeChar(ch));
	}

	public float renderDefaultChar(int ch, boolean flag) {
		float x = (float) (ch % 16 * 8);
		float y = (float) (ch / 16 * 8);
		// italicOffset
		float o = flag ? 1.0f : 0.0f;
		renderEngine.bindTexture(fontTextureName);
		float cw = (float) charWidth[ch] - 0.01f;
		glBegin(GL_TRIANGLE_STRIP);
		glTexCoord2f(x / 128.0f, y / 128.0f);
		glVertex3f(posX + o, posY, 0.0f);
		glTexCoord2f(x / 128.0f, (y + 7.99f) / 128.0f);
		glVertex3f(posX - o, posY + 7.99f, 0.0f);
		glTexCoord2f((x + cw) / 128.0f, y / 128.0f);
		glVertex3f(posX + cw + o, posY, 0.0f);
		glTexCoord2f((x + cw) / 128.0f, (y + 7.99f) / 128.0f);
		glVertex3f(posX + cw - o, posY + 7.99f, 0.0f);
		glEnd();
		return (float) charWidth[ch];
	}

	private float renderUnicodeChar(int ch) {
		return 0.0f;
	}

	public int drawStringWithShadow(String message, int x, int y, int color) {
		return drawString(message, x, y, color, true);
	}

	public int drawString(String message, int x, int y, int color) {
		return drawString(message, x, y, color, false);
	}

	public int drawString(String message, int x, int y, int color, boolean shadow) {
		glEnable(GL_ALPHA_TEST);
		resetStyles();
		int xx;

		if (shadow) {
			xx = renderString(message, x + 1, y + 1, color, true);
			xx = Math.max(xx, renderString(message, x, y, color, false));
		} else xx = renderString(message, x, y, color, false);

		return xx;
	}

	private void resetStyles() {
		randomStyle = false;
		boldStyle = false;
		italicStyle = false;
		underlineStyle = false;
		strikethroughStyle = false;
	}

	private void renderStringAtPos(String message, boolean darken) {
		for (int i = 0; i < message.length(); ++i) {
			char c = message.charAt(i);
			int ch;
			int n;

			if (c == 167 && i + 1 < message.length()) {
				ch = "0123456789abcdefklmnor".indexOf(message.toLowerCase().charAt(i + 1));

				if (ch < 16) {
					randomStyle = false;
					boldStyle = false;
					italicStyle = false;
					underlineStyle = false;
					strikethroughStyle = false;

					if (ch < 0 || ch > 15) ch = 15;

					if (darken) ch += 16;

					n = colorCode[ch];
					textColor = n;
					glColor4f((float) (n >> 16) / 255.0f, (float) (n >> 8 & 255) / 255.0f, (float) (n & 255) / 255.0f, alpha);
				} else if (ch == 16) randomStyle = true;
				else if (ch == 17) boldStyle = true;
				else if (ch == 18) italicStyle = true;
				else if (ch == 19) underlineStyle = true;
				else if (ch == 20) strikethroughStyle = true;
				else if (ch == 21) {
					randomStyle = false;
					boldStyle = false;
					italicStyle = false;
					underlineStyle = false;
					strikethroughStyle = false;
					glColor4f(red, green, blue, alpha);
				}

				++i;
			} else {
				ch = ChatAllowedCharacters.allowedCharacters.indexOf(c);

				if (randomStyle && ch > 0) {
					do
						n = fontRandom.nextInt(ChatAllowedCharacters.allowedCharacters.length());
					while (charWidth[ch + 32] != charWidth[n + 32]);

					ch = n;
				}

				// offset
				float o = unicodeFlag ? 0.5f : 1.0f;
				boolean flag = (ch <= 0 || unicodeFlag) && darken;

				if (flag) {
					posX -= o;
					posY -= o;
				}

				float cw = renderCharAtPos(ch, c, italicStyle);

				if (flag) {
					posX += o;
					posY += o;
				}

				if (boldStyle) {
					posX += o;

					if (flag) {
						posX -= o;
						posY -= o;
					}

					renderCharAtPos(ch, c, italicStyle);
					posX -= o;

					if (flag) {
						posX += o;
						posY += o;
					}

					++cw;
				}

				Tessellator tess;

				if (underlineStyle) {
					tess = Tessellator.instance;
					glDisable(GL_TEXTURE_2D);
					tess.startDrawingQuads();
					tess.addVertex((double) (posX + (float) (-1)), (double) (posY + (float) FONT_HEIGHT), 0.0);
					tess.addVertex((double) (posX + cw), (double) (posY + (float) FONT_HEIGHT), 0.0);
					tess.addVertex((double) (posX + cw), (double) (posY + (float) FONT_HEIGHT - 1.0f), 0.0);
					tess.addVertex((double) (posX + (float) (-1)), (double) (posY + (float) FONT_HEIGHT - 1.0f), 0.0);
					tess.draw();
					glEnable(GL_TEXTURE_2D);
				}

				if (strikethroughStyle) {
					tess = Tessellator.instance;
					glDisable(GL_TEXTURE_2D);
					tess.startDrawingQuads();
					tess.addVertex((double) posX, (double) (posY + (float) (FONT_HEIGHT / 2)), 0.0);
					tess.addVertex((double) (posX + cw), (double) (posY + (float) (FONT_HEIGHT / 2)), 0.0);
					tess.addVertex((double) (posX + cw), (double) (posY + (float) (FONT_HEIGHT / 2) - 1.0f), 0.0);
					tess.addVertex((double) posX, (double) (posY + (float) (FONT_HEIGHT / 2) - 1.0f), 0.0);
					tess.draw();
					glEnable(GL_TEXTURE_2D);
				}

				posX += (float) ((int) cw);
			}
		}
	}

	private int renderString(String message, int x, int y, int color, boolean darken) {
		if (message == null) return 0;
		else {
			if ((color & -67108864) == 0) color |= -16777216;

			if (darken) color = (color & 16579836) >> 2 | color & -16777216;

			red = (float) (color >> 16 & 255) / 255.0f;
			green = (float) (color >> 8 & 255) / 255.0f;
			blue = (float) (color & 255) / 255.0f;
			alpha = (float) (color >> 24 & 255) / 255.0f;
			glColor4f(red, green, blue, alpha);
			posX = (float) x;
			posY = (float) y;
			renderStringAtPos(message, darken);
			return (int) posX;
		}
	}

	public int getStringWidth(String string) {
		if (string == null) return 0;
		else {
			int sw = 0;
			boolean flag = false;

			for (int i = 0; i < string.length(); ++i) {
				char c = string.charAt(i);
				int cw = getCharWidth(c);

				if (cw < 0 && i < string.length() - 1) {
					++i;
					c = string.charAt(i);

					if (c != 108 && c != 76) {
						if (c == 114 || c == 82) flag = false;
					} else flag = true;

					cw = 0;
				}

				sw += cw;

				if (flag) ++sw;
			}

			return sw;
		}
	}

	public int getCharWidth(char c) {
		if (c == 167) return -1;
		else if (c == 32) return 4;
		else {
			int ch = ChatAllowedCharacters.allowedCharacters.indexOf(c);

			if (ch >= 0 && !unicodeFlag) return charWidth[ch + 32];
			else return 0;
		}
	}

	public void onReload() {
	}
}
