package crawler.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DocumentDBWrapper {
	
	private String envDirectory = null;
	private File envFile;
	
	private Environment myEnv;
	private EntityStore store;
	
	private PrimaryIndex<String, DocumentData> seenContent;
	private Iterator<String> keys;
	//private int channelId = 0;
	
	private static DocumentDBWrapper db;
	
	public synchronized static DocumentDBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			System.out.println("Making new db wrapper");
			db = new DocumentDBWrapper(homeDirectory);
		}
		return db;
	}
	
	private DocumentDBWrapper(String homeDirectory) {
		envDirectory = homeDirectory;
		System.out.println("Opening environment in: " + envDirectory);
		envFile = new File(System.getProperty("user.dir"), envDirectory);
		if (!envFile.exists()) {
			if(envFile.mkdir()){
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
			seenContent = store.getPrimaryIndex(String.class, DocumentData.class);
			
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
	
	public synchronized Map<String, DocumentData> getAllContent() {
		EntityCursor<DocumentData> c = seenContent.entities();
		Iterator<DocumentData> ir = c.iterator();
		while (ir.hasNext()) {
			DocumentData ce = ir.next();
			System.out.println(ce.getUrl()+": "+new Date(Long.valueOf(ce.getLastSeen())));
		}
		c.close();
		return seenContent.map();
	}
	
	public synchronized void initIterator(){
		keys = seenContent.keys().iterator();
	}
	
	public synchronized DocumentData getNextDocument(){
		if(keys == null)
			return null;
		
		if(!keys.hasNext()){
			keys = null;
			return null;
		}

		return seenContent.get(keys.next());
		
	}
	
	public synchronized DocumentData getContentById(String id) {
		DocumentData ce = seenContent.get(id);
		return ce;
	}
	
	public synchronized void addContent(String url, String content, long date, ArrayList<String> links) {
		DocumentData ce = new DocumentData();
		ce.setUrl(url);
		ce.setContent(content);
		ce.setLastSeen(String.valueOf(date));
		ce.setLinks(links);
		seenContent.put(ce);
	}
	
	public synchronized ArrayList<String> getLinks(String document){
		return seenContent.get(document).getLinks();
	}
	
	public synchronized boolean checkUrlSeen(String docURL) {
		if(docURL == null || docURL.equals(""))
			return true;
		docURL = docURL.trim();
		if(docURL.startsWith("http://")) {
			docURL = docURL.substring(7);
		} else if (docURL.startsWith("https://")) {
			docURL = docURL.substring(8);
		}
		if (docURL.isEmpty()) {
			return true;
		}
		
		if (seenContent.contains(docURL)) {
			return true;
		} else {
			return false;
		}
	}
	

	

	public void printContent() {
		Set<Entry<String, DocumentData>> seenContentEntries = seenContent.map().entrySet();
		
		for (Entry<String, DocumentData> e : seenContentEntries) {
			DocumentData ue = e.getValue();
			System.out.println(e.getKey()+" "+
					ue.getUrl()+": "+ue.getContent().hashCode());
		}
	}
	
}
