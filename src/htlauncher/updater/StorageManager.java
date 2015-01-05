package htlauncher.updater;

import htlauncher.utilities.AppDescriptor;
import htlauncher.utilities.ComponentDescriptor;
import htlauncher.utilities.Utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * An interface to all the local data created and used by the updater.
 */
public class StorageManager {
	
	// URL from which the app descriptor XML file is downloaded.
	private static final String APP_DESC_XML_URL = "https://raw.githubusercontent.com/HubTurbo/AutoUpdater/master/HubTurbo.xml";
	
	// Temporary location where downloaded files are placed.
	public static final String UPDATE_FOLDER = "update/";
	
	// Eventual location of application files.
	private static final String LAUNCH_FOLDER = "app/";
	
	// Location of the file that the updater uses to maintain state after downloading all components.
	public static final String UPDATER_INFO_FILEPATH = "updater_data";
	
	// Marker used to partition the updater info file.
	private static final String SPLIT_MARKER = "<-sp->";

	private File appDescFile;
	private File updaterInfoFile;

	// The updated URI of the app descriptor, taken from previously-downloaded app descriptor
	private URI serverAppDescURI;

	private AppDescriptor appDescriptor;

	// All versions that have already been downloaded
	private HashMap<String, Double> downloadedVersions = new HashMap<String, Double>();

	public StorageManager(String appDescPath) {
		appDescFile = new File(appDescPath);
		updaterInfoFile = new File(UPDATER_INFO_FILEPATH);

		// If there is a previously-downloaded file in the temporary folder, move it
		moveLastDownload();

		createDownloadDirectory();
		loadUpdaterData();
	}

	/**
	 * Moves downloads from the temporary folder to the launch folder.
	 */
	public static void moveLastDownload() {
		try {
			File downloadDir = new File(UPDATE_FOLDER);
			if (downloadDir.exists() && downloadDir.list().length > 0) {
				moveAndReplaceExistingFiles(UPDATE_FOLDER, LAUNCH_FOLDER);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createDownloadDirectory() {
		File downloadDir = new File(UPDATE_FOLDER);
		if (!downloadDir.exists()) {
			downloadDir.mkdir();
		}
	}

	/**
	 * Attempts to load updater date file, creating it if it doesn't exist.
	 */
	private void loadUpdaterData() {
		try {
			if (!updaterInfoFile.exists()) {
				updaterInfoFile.createNewFile();
			} else {
				loadUpdaterDataFromFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			Utilities.showWarning("Launcher cache file creation failed",
					"Cannot create or open application launcher cache file. Check directory permissions");
		}
	}

	/**
	 * Loads and parses the data file, extracting information about what versions have been downloaded.
	 */
	private void loadUpdaterDataFromFile() {
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(updaterInfoFile));
			String storedPath = fileReader.readLine();
			if (storedPath != null) {
				serverAppDescURI = new URI(storedPath);
			}
	
			String line;
			while ((line = fileReader.readLine()) != null) {
				String[] lineArr = line.split(SPLIT_MARKER);
				if (lineArr.length == 2) {
					String name = lineArr[0];
					Double ver = Double.parseDouble(lineArr[1]);
					downloadedVersions.put(name, ver);
				}
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Utilities.showWarning("Launcher cache read failed.",
					"Cannot read application launcher data. Check directory permissions.");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a file or directory
	 * @param filePath
	 */
	private static void remove(File filePath) {
		if (filePath.isDirectory()) {
			String[] dirItems = filePath.list();
			if (dirItems.length == 0) {
				filePath.delete();
			} else {
				for (String item : dirItems) {
					File itemPath = new File(filePath, item);
					remove(itemPath);
				}
			}
		} else {
			filePath.delete();
		}
	}

	public void updateDownloadedVersion(String name, double version) {
		downloadedVersions.put(name, version);
	}

	public double getDownloadedVersion(String name) {
		Double ver = downloadedVersions.get(name);
		if (ver == null) {
			return -1;
		}
		return ver;
	}

	/**
	 * Saves data to the updater info file.
	 * Includes the last-known app descriptor URL and information about downloaded versions.
	 */
	public void saveUpdaterData() {
		try {
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(updaterInfoFile));
			fileWriter.write(serverAppDescURI.toString());
			fileWriter.write("\n");

			for (Entry<String, Double> entry : downloadedVersions.entrySet()) {
				String entryString = entry.getKey() + SPLIT_MARKER + entry.getValue();
				fileWriter.write(entryString);
				fileWriter.write("\n");
			}
			fileWriter.flush();
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
			Utilities.showWarning("Launcher data save failed",
					"Cannot save application launcher data to disk. Check directory permissions.");
		}
	}

	/**
	 * @return the path from which the downloaded application can be launched
	 */
	public String getAppLaunchPath() {
		if (appDescriptor == null) {
			if (!loadAppDesc()) {
				return "";
			}
		}
		String path = LAUNCH_FOLDER + appDescriptor.getLaunchPath().toString();
		return path;
	}

	/**
	 * Loads the app descriptor if it exists
	 * @return true if the app descriptor was loaded
	 */
	public boolean loadAppDesc() {
		if (appDescFile.exists()) {
			appDescriptor = AppDescriptor.unserialiseFromXMLFile(appDescFile);
			serverAppDescURI = appDescriptor.getServerAppDescriptorURI();
			return true;
		}
		return false;
	}

	/**
	 * @return a list of the application's component descriptors
	 */
	public ArrayList<ComponentDescriptor> getAppComponents() {
		if (appDescriptor == null) {
			loadAppDesc();
		}
		return appDescriptor.getComponents();
	}

	/**
	 * @return the last-known app descriptor
	 */
	public URI getServerAppDescURI() {
		if (serverAppDescURI == null) {
			try {
				serverAppDescURI = new URI(APP_DESC_XML_URL);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return serverAppDescURI;
	}

	/**
	 * @return the name of the app as shown in the app descriptor
	 */
	public String getAppName() {
		return appDescriptor.getAppName();
	}

	private static void moveAndReplaceExistingFiles(String source, String dest) throws IOException {
		File destDir = new File(dest);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		File sourceDir = new File(source);
		String[] items = sourceDir.list();
		for (String item : items) {
			File itemFile = new File(sourceDir, item);
			File destItemFile = new File(destDir, item);
			if (itemFile.isDirectory()) {
				moveAndReplaceFolder(itemFile.toString(), destItemFile.toString());
			} else {
				Files.move(itemFile.toPath(), destItemFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	private static void moveAndReplaceFolder(String source, String dest) throws IOException {
		File destDir = new File(dest);
		if (destDir.exists()) {
			remove(destDir);
		}
		Files.move(Paths.get(source), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
	}
}
