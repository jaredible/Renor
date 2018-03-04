package renor.sound;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SoundPool {
	private final Random rand = new Random();
	private final Map<String, List<SoundPoolEntry>> nameToSoundPoolEntriesMapping = new HashMap<String, List<SoundPoolEntry>>();

	public void addSound(String name, File file) {
		// try {

		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// throw new RuntimeException(e);
		// }
	}

	public SoundPoolEntry getRandomSoundFromSoundPool(String name) {
		List<SoundPoolEntry> sound = nameToSoundPoolEntriesMapping.get(name);
		return sound == null ? null : sound.get(rand.nextInt(sound.size()));
	}

	public SoundPoolEntry getRandomSound() {
		return null;
	}
}
