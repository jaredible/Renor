package renor.sound;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import renor.level.entity.EntityLiving;
import renor.misc.GameSettings;
import renor.util.IReloadListener;

public class SoundManager implements IReloadListener {
	private Random random = new Random();
	private Set<String> playingSounds = new HashSet<String>();
	private final File fileAssets;
	private SoundSystem sndSystem;
	private final GameSettings options;
	private SoundPool soundPoolSounds = new SoundPool();
	private SoundPool soundPoolMusic = new SoundPool();
	private int ticksBeforeMusic;
	private int latestSoundId;
	private boolean loaded = false;

	public SoundManager(GameSettings gameSettings, File fileAssets) {
		options = gameSettings;
		this.fileAssets = fileAssets;

		ticksBeforeMusic = random.nextInt(12000);

		try {
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
			SoundSystemConfig.setCodec("wav", CodecWav.class);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error linking with the LibraryJavaSound plug-in");
		}

		loadSounds();
	}

	private void loadSounds() {
		if (fileAssets.isDirectory()) {
		}
	}

	private void loadSoundFile(File soundFile) {
	}

	private synchronized void tryToSetLibraryAndCodecs() {
		if (!loaded) {
			float tempSoundVolume = options.soundVolume;
			float tempMusicVolume = options.soundVolume;
			options.soundVolume = 0.0f;
			options.musicVolume = 0.0f;
			options.saveOptions();

			try {
				new Thread(new SoundManagerINNER1(this)).start();
				options.soundVolume = tempSoundVolume;
				options.musicVolume = tempMusicVolume;
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.err.println("Error starting sound system, turning off sounds & music");
				options.soundVolume = 0.0f;
				options.musicVolume = 0.0f;
			}

			options.saveOptions();
		}
	}

	public void onSoundOptionsChanged() {
		if (loaded) {
			if (options.musicVolume == 0.0f) sndSystem.stop("BgMusic");
			else sndSystem.setVolume("BgMusic", options.musicVolume);
		}
	}

	public void cleanup() {
		if (loaded) {
			loaded = false;
			sndSystem.cleanup();
		}
	}

	public void addSound(String name, File file) {
		soundPoolSounds.addSound(name, file);
	}

	public void addMusic(String name, File file) {
		soundPoolMusic.addSound(name, file);
	}

	public void setListener(EntityLiving living, float partialTickTime) {
		if (loaded && options.soundVolume > 0.0f) {
			if (living != null) {
			}
		}
	}

	public void playSound(String name, float x, float y, float z, float volume, float pitch) {
		if (loaded && options.soundVolume != 0.0f) {
			SoundPoolEntry randomSound = soundPoolSounds.getRandomSoundFromSoundPool(name);

			if (randomSound != null && volume > 0.0f) {
				latestSoundId = (latestSoundId + 1) % 256;
				String soundId = "sound_" + latestSoundId;
				float d = 16.0f;

				if (volume > 1.0f) d *= volume;

				sndSystem.newSource(volume > 1.0f, soundId, randomSound.getSoundUrl(), randomSound.getSoundName(), false, x, y, z, 2, d);

				if (volume > 1.0f) volume = 1.0f;

				sndSystem.setPitch(soundId, pitch);
				sndSystem.setVolume(soundId, options.soundVolume * volume);
				sndSystem.play(soundId);
			}
		}
	}

	public void playRandomMusicIfReady() {
		if (loaded && options.musicVolume > 0.0f) {
			if (!sndSystem.playing("BgMusic")) {
				if (ticksBeforeMusic > 0) ticksBeforeMusic--;
				else {
					SoundPoolEntry randomMusic = soundPoolMusic.getRandomSound();

					if (randomMusic != null) {
						ticksBeforeMusic = random.nextInt(12000) + 12000;
					}
				}
			}
		}
	}

	public void stopAllSounds() {
		if (loaded) {
			Iterator<String> playingSoundsIterator = playingSounds.iterator();

			while (playingSoundsIterator.hasNext()) {
				String string = playingSoundsIterator.next();
				sndSystem.stop(string);
			}

			playingSounds.clear();
		}
	}

	public void pauseAllSounds() {
		Iterator<String> playingSoundsIterator = playingSounds.iterator();

		while (playingSoundsIterator.hasNext()) {
			String string = playingSoundsIterator.next();
			sndSystem.pause(string);
		}
	}

	public void resumeAllSounds() {
		Iterator<String> playingSoundsIterator = playingSounds.iterator();

		while (playingSoundsIterator.hasNext()) {
			String string = playingSoundsIterator.next();
			sndSystem.play(string);
		}
	}

	public void onReload() {
		stopAllSounds();
		cleanup();
		tryToSetLibraryAndCodecs();
	}

	static SoundSystem a(SoundManager soundManager, SoundSystem soundSystem) {
		return soundManager.sndSystem = soundSystem;
	}

	static boolean b(SoundManager soundManager, boolean flag) {
		return soundManager.loaded = flag;
	}
}
