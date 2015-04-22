package crawler.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class RobotsDBWrapper {
	private static String envDirectory = null;

	private Environment myEnv;
	private EntityStore robotStore;
	
	private PrimaryIndex<String, RobotsTxtData> robotsIndex;
	
	private static RobotsDBWrapper wrapper = null;

	private RobotsDBWrapper(String directory) throws DatabaseException, FileNotFoundException {
		envDirectory = directory;
		
		File file = new File(envDirectory);
		if (!file.exists()) {
			if(file.mkdirs()){
				System.out.println("Creating directory " + file.getAbsolutePath());
			}
			else{
				System.out.println("Failed creating directory " + file.getAbsolutePath());
			};
			
		}
		else{
			System.out.println("Database directory exists");
		}
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);
		

		myEnv = new Environment(file, envConfig);	   
		
		//URL Database
		robotStore = new EntityStore(myEnv, "RobotStore", storeConfig);
		robotsIndex = robotStore.getPrimaryIndex(String.class, RobotsTxtData.class);
		

	}


	public synchronized void close() throws DatabaseException{
		if (robotStore != null) {
			try {
				robotStore.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing store: " + dbe.toString());
				System.exit(-1);
			}
		}
		
		if (myEnv != null) {
			try {
				myEnv.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing environment: " + dbe.toString());
				System.exit(-1);
			}
		}
		wrapper = null;
	}
	
	public synchronized Environment getEnvironment(){
		return myEnv;
	}
	
	/**
	 * Add robots.txt data to database
	 * @param hostname - host for robots.txt file
	 * @param allowedLinks - links allowed for this host
	 * @param disallowedLinks - links disallowed for this host
	 * @param crawlDelay - delay for this host
	 */
	public synchronized void addRobotsTxt(String hostName, ArrayList<String> allowedLinks,  ArrayList<String> disallowedLinks, long crawlDelay){
		robotsIndex.put(new RobotsTxtData(hostName, allowedLinks, disallowedLinks, crawlDelay));
	}
	
	/**
	 * Get RobotsTxtData from url
	 * @param hostName - host to get robots.txt for
	 * @return RobotsTxtData
	 */
	public synchronized RobotsTxtData getRobotsTxtData(String hostName){
		return robotsIndex.get(hostName);
	}
	
	/**
	 * Delete RobotsTxtData 
	 * @param hostname - host to delete
	 */
	public synchronized void deleteRobotsTxtData(String hostName){
		robotsIndex.delete(hostName);
	}
	
	public static synchronized RobotsDBWrapper getInstance(String directory) throws DatabaseException, FileNotFoundException {
		if(wrapper == null) {
			wrapper = new RobotsDBWrapper(directory);
		}
		return wrapper;
	}
}
