package htlauncher.updater;

import htlauncher.launcher.AppLauncher;
import htlauncher.utilities.ComponentDescriptor;
import htlauncher.utilities.Utilities;
import htlauncher.utilities.Version;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A facade for the other components.
 */
public class UpdateManager {

	private DownloadProgressDisplay downloadProgressDisplay;
	private StorageManager storageManager;
	private FileDownloader downloader;
	
	// The path to the application descriptor file
	private URI appDescURI;

	// Will be set to true if an updated version of the application was found and downloaded.
	private boolean applicationUpdated = false;

	public UpdateManager(String appDescPath) throws URISyntaxException {
		downloadProgressDisplay = new DownloadProgressDisplay();
		storageManager = new StorageManager(appDescPath);
		downloader = new FileDownloader();
		appDescURI = new URI(appDescPath);
	}

	/**
	 * Displays the progress bar and starts the update process.
	 * @param firstRun
	 * @return
	 */
	public boolean runUpdate(boolean firstRun) {

		// Invalidate the current version of the application
		applicationUpdated = false;
		
		if (checkServerConnection()) {
			if (firstRun) {
				downloadProgressDisplay.showProgressWindow();
			}

			boolean success = runRequiredUpdate();

			if (firstRun) {
				downloadProgressDisplay.hideProgressWindow();
				StorageManager.moveLastDownload();
			} else if (applicationUpdated) {
				Utilities.showMessageOnTop("Application updated", storageManager.getAppName()
						+ " has been successfully updated. Restart application to get the latest update.");
			}

			return success;
		}
		return false;
	}

	/**
	 * Updates the app descriptor, then the app itself
	 * @return true if successful
	 */
	private boolean runRequiredUpdate() {
		if (AppLauncher.UPDATE_APP_DESC) {
			updateAppDesc();
		}
		boolean success = updateAppComponents();
		return success;
	}

	/**
	 * Updates the XML file containing the application descriptor.
	 * Overwrites the current XML file.
	 */
	public void updateAppDesc() {
		URI serverURI = storageManager.getServerAppDescURI();

		boolean success = startDownload(serverURI, appDescURI, true);
		if (success) {
			storageManager.loadAppDesc();
		} else {
			downloader.rollBack();
		}
	}

	/**
	 * Updates the application itself.
	 * @return true if successful
	 */
	public boolean updateAppComponents() {
		boolean success = true;

		for (ComponentDescriptor component : storageManager.getAppComponents()) {
			success = updateComponent(component);
			if (!success) {
				break;
			}
		}
		
		if (success) {
			storageManager.saveUpdaterData();
			downloader.removeBackups();
			return true;
		} else {
			downloader.rollBack();
			applicationUpdated = false;
			return false;
		}
	}

	/**
	 * Updates a single component identified by the given component descriptor.
	 * @param component
	 * @return a boolean value indicating success.
	 */
	public boolean updateComponent(ComponentDescriptor component) {
		String name = component.getComponentName();
		Version latestVersion = component.getVersion();
		Version currentVersion = storageManager.getDownloadedVersion(name);
		boolean success = true;

		if (latestVersion.compareTo(currentVersion) > 0) { // latest > current
			downloadProgressDisplay.updateDownloadingComponent(component.getComponentName());

			// Update jar for component from server
			String compath = StorageManager.UPDATE_FOLDER + component.getLocalURI().toString();
			URI dlURI;
			try {
				dlURI = new URI(compath);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return false;
			}

			success = startDownload(component.getServerURI(), dlURI, true);

			if (success) {
				storageManager.updateDownloadedVersion(name, latestVersion);
				applicationUpdated = true;
			}
		}
		return success;
	}

	public String getAppLaunchPath() {
		return storageManager.getAppLaunchPath();
	}
	
	public String getDownloadedVersion(String name) {
		return storageManager.getDownloadedVersion(name).toString();
	}
	
	public String getAppName() {
		return storageManager.getAppName();
	}

	/**
	 * Downloads a file, using the current progress display to show progress.
	 * @param source
	 * @param dest
	 * @param showProgress
	 * @return a boolean value indicating if the download was successful.
	 */
	private boolean startDownload(URI source, URI dest, boolean showProgress) {
		DownloadProgress progress = new DownloadProgress();
		if (showProgress) {
			downloadProgressDisplay.startProgressDisplay(progress);
		}
		downloader.downloadFile(source, dest, progress);
		return progress.getDownloadSuccess();
	}

	private boolean checkServerConnection() {
		try {
			String serverPath = storageManager.getServerAppDescURI().getHost();
			if (serverPath == null) {
				throw new MalformedURLException();
			}
			URL serverURL = new URL("http", serverPath, 80, "");
			serverURL.openConnection().connect();
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Utilities.showError("Cache Corrupted",
					"The application launcher's cache has been corrupted! Please delete "
							+ StorageManager.UPDATER_INFO_FILEPATH);
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
