package renor.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeyBinding {
	public static List<KeyBinding> keybindArray = new ArrayList<KeyBinding>();
	public String keyDescription;
	public static IntHashMap hash = new IntHashMap();
	public int keyCode;
	public int presses = 0;
	private boolean pressed;

	public static void onTick(int n) {
		// keyBinding
		KeyBinding kb = (KeyBinding) hash.lookup(n);

		if (kb != null) ++kb.presses;
	}

	public static void setKeyBindState(int n, boolean pressed) {
		// keyBinding
		KeyBinding kb = (KeyBinding) hash.lookup(n);

		if (kb != null) kb.pressed = pressed;
	}

	public static void unPressAllKeys() {
		Iterator<KeyBinding> keyBindingIterator = keybindArray.iterator();

		while (keyBindingIterator.hasNext()) {
			// keyBinding
			KeyBinding kb = keyBindingIterator.next();
			kb.unpressKey();
		}
	}

	public static void resetKeyBindingArrayAndHash() {
		hash.clearMap();
		Iterator<KeyBinding> keyBindingIterator = keybindArray.iterator();

		while (keyBindingIterator.hasNext()) {
			// keyBinding
			KeyBinding kb = keyBindingIterator.next();
			hash.addKey(kb.keyCode, kb);
		}
	}

	public KeyBinding(String name, int keyCode) {
		keyDescription = name;
		this.keyCode = keyCode;
		keybindArray.add(this);
		hash.addKey(keyCode, this);
	}

	public boolean isPressed() {
		if (presses == 0) return false;
		else {
			--presses;
			return true;
		}
	}

	public boolean getIsKeyPressed() {
		return pressed;
	}

	private void unpressKey() {
		presses = 0;
		pressed = false;
	}
}
