package crawler.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class UnseenLinksDBWrapper {
	private static String envDirectory = null;

	private Environment myEnv;
	private EntityStore unseenLinksStore;
	
	private PrimaryIndex<String, UnseenLinksData> unseenLinksIndex;
	
	private static UnseenLinksDBWrapper wrapper = null;

	private UnseenLinksDBWrapper(String directory) throws DatabaseException, FileNotFoundException {
		envDirectory = directory;
		
		File file = new File(envDirectory);
		if (!file.exists()) {
			if(file.mkdirs()){
				System.out.println("Creating directory " + file.getAbsolutePath());
			}
			else{
				System.out.println("Failed creating directory " + file.getAbsolutePath());
			}
			
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
		unseenLinksStore = new EntityStore(myEnv, "unseenLinks", storeConfig);
		unseenLinksIndex = unseenLinksStore.getPrimaryIndex(String.class, UnseenLinksData.class);
		

	}


	public synchronized void close() throws DatabaseException{
		if (unseenLinksStore != null) {
			try {
				unseenLinksStore.close();
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
	public synchronized void addURL(String url){
		unseenLinksIndex.put(new UnseenLinksData(url));
	}
	
	/**
	 * Get UnseenLinksData from url
	 * @param url - link to look at
	 * @return RobotsTxtData
	 */
	public synchronized UnseenLinksData getUnseenLinksData(String url){
		return unseenLinksIndex.get(url);
	}
	
	/**
	 * Delete UnseenLinksData 
	 * @param hostname - link just seen
	 */
	public synchronized void deleteUnseenLinksData(String url){
		unseenLinksIndex.delete(url);
	}
	
	public static synchronized UnseenLinksDBWrapper getInstance(String directory) throws DatabaseException, FileNotFoundException {
		if(wrapper == null) {
			wrapper = new UnseenLinksDBWrapper(directory);
		}
		return wrapper;
	}
	
	public synchronized Entry<String, UnseenLinksData> getNextUrl() {
		TreeMap<String, UnseenLinksData> orderedFrontier = new TreeMap<String, UnseenLinksData>(unseenLinksIndex.map());
		Entry<String, UnseenLinksData> e = orderedFrontier.firstEntry();
		if (e!=null) {
			deleteUnseenLinksData(e.getKey());
			//System.out.println(e.getValue().getUrl()+": "+success);
		}
		return e;
	}
	
	public synchronized boolean isEmpty() {
		return (unseenLinksIndex.count() == 0);
	}
	
	public synchronized Map<String, UnseenLinksData> getAllContent() {
		EntityCursor<UnseenLinksData> c = unseenLinksIndex.entities();
		Iterator<UnseenLinksData> ir = c.iterator();
		while (ir.hasNext()) {
			UnseenLinksData data = ir.next();
			System.out.println("Unseen: "+data.getUrl());
		}
		c.close();
		return unseenLinksIndex.map();
	}
	
	public synchronized long getSize() {
		return unseenLinksIndex.count();
	}
}
