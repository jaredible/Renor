package renor.sound;

import java.net.URL;

public class SoundPoolEntry {
	private final String soundName;
	private final URL soundUrl;

	public SoundPoolEntry(String soundName, URL soundUrl) {
		this.soundName = soundName;
		this.soundUrl = soundUrl;
	}

	public String getSoundName() {
		return soundName;
	}

	public URL getSoundUrl() {
		return soundUrl;
	}
}
