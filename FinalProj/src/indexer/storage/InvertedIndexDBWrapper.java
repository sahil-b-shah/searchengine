package indexer.storage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
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
	private Iterator<String>  keys;
	//private int channelId = 0;
	
	private static InvertedIndexDBWrapper db;
	private static InvertedIndexDBWrapper db2;
	private static InvertedIndexDBWrapper db3;
	private static InvertedIndexDBWrapper db4;
	private static InvertedIndexDBWrapper db5;
	private static InvertedIndexDBWrapper db6;
	private static InvertedIndexDBWrapper db7;
	private static InvertedIndexDBWrapper db8;
	private static InvertedIndexDBWrapper db9;
	private static InvertedIndexDBWrapper db10;
	
	
	public synchronized static InvertedIndexDBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			System.out.println("Making new db wrapper");
			db = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance2(String homeDirectory) {
		if (db2 == null) {
			System.out.println("Making new db wrapper");
			db2 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db2;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance3(String homeDirectory) {
		if (db3 == null) {
			System.out.println("Making new db wrapper");
			db3 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db3;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance4(String homeDirectory) {
		if (db4 == null) {
			System.out.println("Making new db wrapper");
			db4 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db4;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance5(String homeDirectory) {
		if (db5 == null) {
			System.out.println("Making new db wrapper");
			db5 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db5;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance6(String homeDirectory) {
		if (db6 == null) {
			System.out.println("Making new db wrapper");
			db6 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db6;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance7(String homeDirectory) {
		if (db7 == null) {
			System.out.println("Making new db wrapper");
			db7 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db7;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance8(String homeDirectory) {
		if (db8 == null) {
			System.out.println("Making new db wrapper");
			db8 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db8;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance9(String homeDirectory) {
		if (db9 == null) {
			System.out.println("Making new db wrapper");
			db9 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db9;
	}
	
	public synchronized static InvertedIndexDBWrapper getInstance10(String homeDirectory) {
		if (db10 == null) {
			System.out.println("Making new db wrapper");
			db10 = new InvertedIndexDBWrapper(homeDirectory);
		}
		return db10;
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
		if(invertedIndex.get(word) == null)
			return null;
		if(invertedIndex.get(word).getUrls().get(url) == null)
			return null;
		return invertedIndex.get(word).getUrls().get(url).getOccurences();
	}
	
	public synchronized Double getTF(String word, String url, double tf){
		if(invertedIndex.get(word) == null)
			return null;
		if(invertedIndex.get(word).getUrls().get(url) == null)
			return null;
		return invertedIndex.get(word).getUrls().get(url).getTF();
	}
	
	public synchronized Double getIDF(String word, String url, double idf){
		if(invertedIndex.get(word) == null)
			return null;
		if(invertedIndex.get(word).getUrls().get(url) == null)
			return null;
		return invertedIndex.get(word).getUrls().get(url).getIDF();
	}
	
	public synchronized Iterator<Entry<String, URLMetrics>> getWordIterator(String word){
		if(invertedIndex.get(word) == null)
			return null;
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
	
	public synchronized void initIterator(){
		keys = invertedIndex.map().keySet().iterator();
	}
	
	public synchronized InvertedIndexData getNextWord(){
		
		if(keys == null || !keys.hasNext()){
			return null;
		}
		
		InvertedIndexData doc = invertedIndex.get(keys.next());
		return doc;
		
		
	}
	
	public synchronized void closeIterator(){
		keys = null;
	}
	
	
}
