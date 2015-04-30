package indexer.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class WordCountDBWrapper {
	private String envDirectory = null;
	private File envFile;
	
	private Environment myEnv;
	private EntityStore store;
	
	private PrimaryIndex<String, WordCountData> numWordsIndex;
	//private int channelId = 0;
	
	private static WordCountDBWrapper db;
	
	public synchronized static WordCountDBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			System.out.println("Making new db wrapper");
			db = new WordCountDBWrapper(homeDirectory);
		}
		return db;
	}
	
	private WordCountDBWrapper(String homeDirectory) {
		envDirectory = homeDirectory;
		System.out.println("Opening environment in: " + envDirectory);
		envFile = new File(envDirectory);
		envFile.mkdirs();
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
			numWordsIndex = store.getPrimaryIndex(String.class, WordCountData.class);
			
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
	
	
	public synchronized boolean isEmpty() {
		return (numWordsIndex.count() == 0);
	}
	
	public synchronized void addWord(String w, int number) {
		WordCountData ce = new WordCountData();
		ce.setURL(w);
		ce.setNumberWords(number);
		numWordsIndex.put(ce);
	}
	
	public synchronized WordCountData getContentById(String w) {
		WordCountData ce = numWordsIndex.get(w);
		return ce;
	}
		
	public synchronized int getNumberWords(String url){
		if(numWordsIndex.get(url) == null)
			return 0;
		return numWordsIndex.get(url).getNumber();
	}
	
	
	public long getSize(){
		return numWordsIndex.count();
	}
}
