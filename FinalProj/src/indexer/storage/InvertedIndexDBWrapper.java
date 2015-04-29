package indexer.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

import crawler.storage.DocumentData;

public class InvertedIndexDBWrapper {
	private String envDirectory = null;
	private File envFile;
	
	private Environment myEnv;
	private EntityStore store;
	
	private PrimaryIndex<String, InvertedIndexData> invertedIndex;
	//private int channelId = 0;
	
	private static InvertedIndexDBWrapper db;
	
	public synchronized static InvertedIndexDBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			System.out.println("Making new db wrapper");
			db = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db;
	}
	
	private InvertedIndexDBWrapper(String homeDirectory) {
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
			invertedIndex = store.getPrimaryIndex(String.class, InvertedIndexData.class);
			
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
		return (invertedIndex.count() == 0);
	}
	
	public synchronized void addWord(String w, HashMap<String, URLMetrics> map) {
		InvertedIndexData ce = new InvertedIndexData();
		ce.setWord(w);
		ce.setMap(map);
		invertedIndex.put(ce);
	}
	
	public synchronized InvertedIndexData getContentById(String w) {
		InvertedIndexData ce = invertedIndex.get(w);
		return ce;
	}
		
	public synchronized HashMap<String, URLMetrics> getUrls(String word){
		if(invertedIndex.get(word) == null)
			return null;
		return invertedIndex.get(word).getUrls();
	}
	
	public synchronized Integer getOccurences(String word, String url){
		return invertedIndex.get(word).getUrls().get(url).getOccurences();
	}
	
	public synchronized Double getTF(String word, String url, double tf){
		return invertedIndex.get(word).getUrls().get(url).getTF();
	}
	
	public synchronized Double getIDF(String word, String url, double idf){
		return invertedIndex.get(word).getUrls().get(url).getIDF();
	}
	
	public synchronized Iterator<Entry<String, URLMetrics>> getWordIterator(String word){
		return invertedIndex.get(word).getUrls().entrySet().iterator();
	}
	
	public void printContent() {
		Set<Entry<String, InvertedIndexData>> seenContentEntries = invertedIndex.map().entrySet();
		
		for (Entry<String, InvertedIndexData> e : seenContentEntries) {
			InvertedIndexData ue = e.getValue();
			System.out.println(ue.getWord()+"------"+
					ue.getUrls().toString());
		}
	}
	
	public long getSize(){
		return invertedIndex.count();
	}
	
	
}
