package pagerank.storage;

import indexer.storage.InvertedIndexData;
import indexer.storage.URLMetrics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

public class PageRankDBWrapper {
	private String envDirectory = null;
	private File envFile;
	
	private Environment myEnv;
	private EntityStore store;
	
	private PrimaryIndex<String, PageInfo> prIndex;
	//private int channelId = 0;
	
	private static PageRankDBWrapper db;
	
	//should probably just be /pageRank
	public synchronized static PageRankDBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			System.out.println("Making new pr db wrapper");
			db = new PageRankDBWrapper(homeDirectory);
		}
		return db;
	}
	
	private PageRankDBWrapper(String homeDirectory) {
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
			prIndex = store.getPrimaryIndex(String.class, PageInfo.class);
			
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
		return (prIndex.count() == 0);
	}
	
	public synchronized void addRank(String u, double val) {
		PageInfo pinfo = new PageInfo();
		pinfo.setURL(u);
		pinfo.setScore(val);
		prIndex.put(pinfo);
	}
	
	public synchronized void addUrl(String u, HashMap<String, Integer> map) {
		PageInfo pi = new PageInfo();
		pi.setURL(u);
		pi.setIncoming(map);
		prIndex.put(pi);
	}
	
	public synchronized PageInfo getContentById(String u) {
		PageInfo pi = prIndex.get(u);
		return pi;
	}
		
	public synchronized HashMap<String, Integer> getUrls(String word){
		if(prIndex.get(word) == null)
			return null;
		return prIndex.get(word).getIncoming();
	}
	
	public synchronized double getRank(String url){
		return prIndex.get(url).getScore();
	}
	
	public synchronized void reset(String url){
		prIndex.get(url).resetScore();
	}
	
	public void printContent() {
		Set<Entry<String, PageInfo>> seenContentEntries = prIndex.map().entrySet();
		
		for (Entry<String, PageInfo> e : seenContentEntries) {
			PageInfo ue = e.getValue();
			System.out.println(ue.getURL()+"------"+
					String.valueOf(ue.getScore()));
		}
	}
	
	public long getSize(){
		return prIndex.count();
	}
	
	
}
