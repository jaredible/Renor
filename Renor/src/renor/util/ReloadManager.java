package renor.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import renor.Renor;

public class ReloadManager {
	private final List<IReloadListener> reloadListeners = new ArrayList<IReloadListener>();

	public void reloadResources() {
		Renor.getRenor().getLogAgent().logInfo("Reloading");
		notifyReloadListeners();
	}

	public void registerReloadListener(IReloadListener reloadListener) {
		reloadListeners.add(reloadListener);
		reloadListener.onReload();
	}

	private void notifyReloadListeners() {
		Iterator<IReloadListener> reloadListenersIterator = reloadListeners.iterator();

		while (reloadListenersIterator.hasNext()) {
			// reloadListener
			IReloadListener rl = reloadListenersIterator.next();
			rl.onReload();
		}
	}
}
