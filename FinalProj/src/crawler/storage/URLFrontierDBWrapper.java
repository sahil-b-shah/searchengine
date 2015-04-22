package crawler.storage;

import java.io.File;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class URLFrontierDBWrapper {
	private String envDirectory = null;
	private File envFile;
	
	private Environment myEnv;
	private EntityStore store;
	
	private PrimaryIndex<Integer, URLFrontierData> urlFrontier;
	//private int channelId = 0;
	
	private static URLFrontierDBWrapper db;
	
	public synchronized static URLFrontierDBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			db = new URLFrontierDBWrapper(homeDirectory);
		}
		return db;
	}
	
	private URLFrontierDBWrapper(String homeDirectory) {
		envDirectory = homeDirectory;
		System.out.println("Opening environment in: " + envDirectory);
		envFile = new File(envDirectory);
		if (!envFile.exists()) {
			if(envFile.mkdirs()){
				System.out.println("Creating directory " + envFile.getAbsolutePath());
			}
			else{
				System.out.println("Failed creating directory " + envFile.getAbsolutePath());
			};
			
		}
		else{
			System.out.println("Database directory exists");
		}
		setup();
	}
	
	private void setup() {

		try {
	        EnvironmentConfig envConfig = new EnvironmentConfig();
	        StoreConfig storeConfig = new StoreConfig();
	        //envConfig.setTransactional(true);
	        envConfig.setAllowCreate(true);
	        storeConfig.setAllowCreate(true);
	        //storeConfig.setTransactional(true);
	
	        myEnv = new Environment(envFile, envConfig);
	        store = new EntityStore(myEnv, "entityStore", storeConfig);
		} catch (DatabaseException dbe) {
			System.err.println("Error opening environment and store: " + dbe.toString());
			System.exit(-1);
		}
		
		try {
			urlFrontier = store.getPrimaryIndex(Integer.class, URLFrontierData.class);
			
		} catch (DatabaseException dbe) {
			System.err.println("Error making indexes");
			System.exit(-1);
		}
	}
	
	public synchronized void close() {
		if (store != null) {
			try {
				store.close();
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
		db = null;
	}
	
	public synchronized final Environment getEnvironment() {
		return myEnv;
	}
	
	public synchronized final EntityStore getStore() {
		return store;
	}
	
	
	public synchronized Entry<Integer, URLFrontierData> getLastUrl() {
		TreeMap<Integer, URLFrontierData> orderedFrontier = new TreeMap<Integer, URLFrontierData>(urlFrontier.map());
		Entry<Integer, URLFrontierData> e = orderedFrontier.lastEntry();
		
		return e;
	}
	
	public synchronized Entry<Integer, URLFrontierData> getNextUrl() {
		TreeMap<Integer, URLFrontierData> orderedFrontier = new TreeMap<Integer, URLFrontierData>(urlFrontier.map());
		Entry<Integer, URLFrontierData> e = orderedFrontier.firstEntry();
		if (e!=null) {
			urlFrontier.delete(e.getKey());
			//System.out.println(e.getValue().getUrl()+": "+success);
		}
		return e;
	}
	
	public synchronized boolean isEmpty() {
		return (urlFrontier.count() == 0);
	}
	
	public synchronized void addUrl(String docURL) {
		/*if(docURL == null || docURL.equals(""))
			return true;
		docURL = docURL.trim();
		if(docURL.startsWith("http://")) {
			docURL = docURL.substring(7);
		} else if (docURL.startsWith("https://")) {
			docURL = docURL.substring(8);
		}
		if (docURL.isEmpty()) {
			return;
		}*/
		
		int id;
		if(getLastUrl() == null) {
			id = 0;
		} else {
			id = getLastUrl().getKey();
		}
		URLFrontierData q_url = new URLFrontierData();
		q_url.setId(id+1);
		q_url.setUrl(docURL);
		urlFrontier.put(q_url);
		System.out.println("Added to frontier: " + (id+1)+"--" + docURL);

	}
		
}
