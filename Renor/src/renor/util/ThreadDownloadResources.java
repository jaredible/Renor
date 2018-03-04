package renor.util;

import java.io.File;

import renor.Renor;

public class ThreadDownloadResources extends Thread {
	public File resourcesFolder;
	private Renor renor;
	private boolean closing = false;

	public ThreadDownloadResources(File dir, Renor renor) {
		resourcesFolder = new File(dir, "resources/");
		this.renor = renor;

		setName("Resource download thread");
		setDaemon(true);

		if (!resourcesFolder.exists() && !resourcesFolder.mkdirs()) throw new RuntimeException("The working directory could not be created: " + resourcesFolder);
	}

	public void run() {
		loadResource(resourcesFolder, "");
	}

	private void loadResource(File dir, String name) {
		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; ++i) {
			if (files[i].isDirectory()) loadResource(files[i], name + files[i].getName() + "/");
			else {
				try {
					renor.installResource(name + files[i].getName(), files[i]);
				} catch (Exception e) {
					renor.getLogAgent().logWarning("Failed to add " + name + files[i].getName() + " in resources");
				}
			}
		}
	}

	public void reloadResources() {
		loadResource(resourcesFolder, "");
	}

	public void closeRenor() {
		closing = true;
	}
}
