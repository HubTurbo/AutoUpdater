package htlauncher.launcher;

import htlauncher.updater.UpdateManager;
import htlauncher.utilities.Utilities;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Entry point.
 */
public class AppLauncher {

	// The path to the data file containing the application descriptor
	public static final String APP_DESC_FILEPATH = "HubTurbo.xml";
	
	private UpdateManager updater;

	public static void main(String[] args) {
		AppLauncher launcher = new AppLauncher();
		launcher.run();
		System.exit(0);
	}

	public AppLauncher() {
		try {
			updater = new UpdateManager(AppLauncher.APP_DESC_FILEPATH);
		} catch (URISyntaxException e) {
			// Should not happen. Means APP_DESC_FILEPATH is set wrongly
			e.printStackTrace();
			Utilities.showFatalErrorDialog(e);
			System.exit(-1);
		}
	}

	public void run() {
		// Launch the app if it exists.
		boolean isAppRunning = launchAppIfPathExists();

		// If it doesn't exist, this must be the first time the updater is running.
		// Perform an update, then try to launch the app again.
		boolean isFirstRun = !isAppRunning;
		updater.runUpdate(isFirstRun);

		if (isFirstRun) {
			launchAppIfPathExists();
		}
	}

	public boolean launchAppIfPathExists() {
		String launchPath = updater.getAppLaunchPath();
		if (launchPath.isEmpty()) {
			return false;
		}
		File path = new File(launchPath);
		if (path.exists()) {
			launchApp(launchPath);
			return true;
		} else {
			return false;
		}
	}

	private void launchApp(String launchPath) {
		String command = "java -jar " + launchPath;
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
			Utilities.showFatalErrorDialog(e);
			System.exit(-1);
		}
	}

}
