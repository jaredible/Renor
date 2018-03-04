package renor.misc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

public class GLAllocation {
	private static final Map<Integer, Integer> displayLists = new HashMap<Integer, Integer>();
	private static final List<Integer> textureNames = new ArrayList<Integer>();
	public static int a = 0;

	public static synchronized int generateTextureNames() {
		int n = GL11.glGenTextures();
		textureNames.add(Integer.valueOf(n));
		return n;
	}

	public static synchronized void deleteTextures() {
		for (int i = 0; i < textureNames.size(); ++i)
			GL11.glDeleteTextures(textureNames.get(i).intValue());

		textureNames.clear();
	}

	public static synchronized int generateDisplayLists(int listName) {
		int n = GL11.glGenLists(listName);
		a += n;
		displayLists.put(Integer.valueOf(n), Integer.valueOf(listName));
		return n;
	}

	public static synchronized void deleteDisplayLists(int listName) {
		GL11.glDeleteLists(listName, displayLists.remove(Integer.valueOf(listName).intValue()));
	}

	public static synchronized void deleteTexturesAndDisplayLists() {
		Iterator<Map.Entry<Integer, Integer>> displayListsIterator = displayLists.entrySet().iterator();

		while (displayListsIterator.hasNext()) {
			a--;
			Map.Entry<Integer, Integer> displayList = displayListsIterator.next();
			GL11.glDeleteLists(displayList.getKey().intValue(), displayList.getKey().intValue());
		}

		displayLists.clear();
		deleteTextures();
	}

	public static synchronized ByteBuffer createDirectByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
	}

	public static IntBuffer createDirectIntBuffer(int capacity) {
		return createDirectByteBuffer(capacity << 2).asIntBuffer();
	}

	public static FloatBuffer createDirectFloatBuffer(int capacity) {
		return createDirectByteBuffer(capacity << 2).asFloatBuffer();
	}
}
