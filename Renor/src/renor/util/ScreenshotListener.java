package renor.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class ScreenshotListener {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	private static IntBuffer pixelBuffer;
	private static int[] pixelValues;

	public static String saveScreenshot(File dir, int requestedWidthInPixels, int requestedHeightInPixels) {
		return saveScreenshot(dir, null, requestedWidthInPixels, requestedHeightInPixels);
	}

	public static String saveScreenshot(File dir, String filename, int requestedWidthInPixels, int requestedHeightInPixels) {
		try {
			File saveDir = new File(dir, "screenshots");
			saveDir.mkdir();
			int bufferSize = requestedWidthInPixels * requestedHeightInPixels;

			if (pixelBuffer == null || pixelBuffer.capacity() < bufferSize) {
				pixelBuffer = BufferUtils.createIntBuffer(bufferSize);
				pixelValues = new int[bufferSize];
			}

			GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, GL11.GL_ONE);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, GL11.GL_ONE);
			pixelBuffer.clear();
			GL11.glReadPixels(0, 0, requestedWidthInPixels, requestedHeightInPixels, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
			pixelBuffer.get(pixelValues);
			a(pixelValues, requestedWidthInPixels, requestedHeightInPixels);
			BufferedImage image = new BufferedImage(requestedWidthInPixels, requestedHeightInPixels, BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, requestedWidthInPixels, requestedHeightInPixels, pixelValues, 0, requestedWidthInPixels);
			File saveFile;

			if (filename == null) saveFile = getTimestampedPNGFileForDirectory(saveDir);
			else saveFile = new File(saveDir, filename);

			ImageIO.write(image, "png", saveFile);
			return "Saved screenshot as " + saveFile.getName();
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to save: " + e;
		}
	}

	private static File getTimestampedPNGFileForDirectory(File dir) {
		String time = dateFormat.format(new Date()).toString();
		int attempts = 1;

		while (true) {
			File saveFile = new File(dir, time + (attempts == 1 ? "" : "_" + attempts) + ".png");

			if (!saveFile.exists()) return saveFile;

			++attempts;
		}
	}

	private static void a(int[] data, int width, int height) {
		int[] v0 = new int[width];
		int v1 = height / 2;

		for (int i = 0; i < v1; ++i) {
			System.arraycopy(data, i * width, v0, 0, width);
			System.arraycopy(data, (height - 1 - i) * width, data, i * width, width);
			System.arraycopy(v0, 0, data, (height - 1 - i) * width, width);
		}
	}
}
