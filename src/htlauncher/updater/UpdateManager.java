package htlauncher.updater;


import htlauncher.utilities.ComponentDescriptor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UpdateManager {	
	private UpdateDataManager dataManager;
	private FileDownloader downloader;
	private URI appInfoURI;
	private DownloadProgressDisplay downloadProgress;
	
	public UpdateManager(String appInfoPath){
		try {
			setupComponents(appInfoPath);
			this.appInfoURI = new URI(appInfoPath);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setupComponents(String appPath){
		downloadProgress = new DownloadProgressDisplay();
		dataManager = new UpdateDataManager(appPath);
		downloader = new FileDownloader();
	}
	
	public void runUpdate(){
		if(checkServerConnection()){
			downloadProgress.showProgressWindow();
			updateAppDetails();
			updateAppComponents();
			downloadProgress.hideProgressWindow();
		}
	}
	
	public void updateAppDetails(){
		URI serverURI = dataManager.getServerAppInfoURI();
		//overwrite with file from server
		startDownload(serverURI, appInfoURI, true);
		dataManager.loadAppData();
	}
	
	public void updateAppComponents(){
		boolean success = true;
		for(ComponentDescriptor component: dataManager.getAppComponents()){
			success = updateComponent(component);
			if(!success){
				break;
			}
		}
		if(success){
			dataManager.saveUpdaterData();
			downloader.removeBackups();
		}else{
			downloader.rollBack();
		}
	}
	
	public boolean updateComponent(ComponentDescriptor component){
		String name = component.getComponentName();
		double latestVer = component.getVersion();
		double currentVer = dataManager.getDownloadedVersion(name);
		boolean success = true;
		if(latestVer > currentVer){
			downloadProgress.updateDownloadingComponent(component.getComponentName());
			//update jar for component from server
			success = startDownload(component.getServerURI(), component.getLocalURI(), true);
			if(success){
				//update success: update downloaded component version
				dataManager.updateDownloadedVersion(name, latestVer);
			}
		}
		return success;
	}
	
	public URI getAppLaunchPath(){
		return dataManager.getAppLaunchPath();
	}
	
	
	private boolean startDownload(URI source, URI dest, boolean showProgress){
		DownloadProgress progress = new DownloadProgress();
		if(showProgress){
			downloadProgress.startProgressDisplay(progress);
		}
		downloader.downloadFile(source, dest, progress);
		return progress.getDownloadSuccess();
	}
	
	private boolean checkServerConnection(){
		try {
			String serverPath = dataManager.getServerAppInfoURI().getHost();
			URL serverURL = new URL ("http", serverPath, 80, "");
			serverURL.openConnection().connect();
			return true;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
}
