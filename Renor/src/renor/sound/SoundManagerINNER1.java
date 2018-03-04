package renor.sound;

import paulscode.sound.SoundSystem;

public class SoundManagerINNER1 implements Runnable {
	final SoundManager theSoundManager;

	SoundManagerINNER1(SoundManager soundManager) {
		theSoundManager = soundManager;
	}

	public void run() {
		SoundManager.a(theSoundManager, new SoundSystem());
		SoundManager.b(theSoundManager, true);
	}
}
