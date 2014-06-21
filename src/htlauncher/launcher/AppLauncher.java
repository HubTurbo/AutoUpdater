package htlauncher.launcher;

import java.io.IOException;
import java.net.URI;
import htlauncher.updater.UpdateManager;

public class AppLauncher {
	public static final String APP_INFO_FILEPATH = "HubTurbo.xml";
	
	private UpdateManager updater;
	
	public static void main(String[] args) {
		AppLauncher launcher = new AppLauncher();
		launcher.run();
		System.exit(0);
	}
	
	public AppLauncher(){
		updater = new UpdateManager(AppLauncher.APP_INFO_FILEPATH);
	}
	
	public void run(){
		runUpdater();
		launchApp();
	}
	
	public void runUpdater(){
		updater.runUpdate();
	}
	
	public void launchApp(){
		URI launchPath = updater.getAppLaunchPath();
		String command = "java -jar " + launchPath.toString(); 
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
