package crawler.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.TreeMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {
	
	private String envDirectory = null;
	private File envFile;
	
	private Environment myEnv;
	private EntityStore store;
	
	private PrimaryIndex<String, UserEntity> users;
	private PrimaryIndex<Integer, ChannelEntity> channels;
	private SecondaryIndex<String, Integer, ChannelEntity> si_channels;
	
	private PrimaryIndex<String, ContentEntity> seenContent;
	private PrimaryIndex<Integer, QueueEntity> urlFrontier;
	private PrimaryIndex<String, XMLEntity> xml;
	private SecondaryIndex<Integer, String, XMLEntity> si_xml;
	//private int channelId = 0;
	
	private static DBWrapper db;
	private static boolean isFirstThread = true;
	
	public synchronized static DBWrapper getInstance(String homeDirectory) {
		if (db == null) {
			System.out.println("Making new db wrapper");
			db = new DBWrapper(homeDirectory);
		}
		return db;
	}
	
	private DBWrapper(String homeDirectory) {
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
			users = store.getPrimaryIndex(String.class, UserEntity.class);
			channels = store.getPrimaryIndex(Integer.class, ChannelEntity.class);
			si_channels = store.getSecondaryIndex(channels, String.class, "username");
			seenContent = store.getPrimaryIndex(String.class, ContentEntity.class);
			urlFrontier = store.getPrimaryIndex(Integer.class, QueueEntity.class);
			xml = store.getPrimaryIndex(String.class, XMLEntity.class);
			si_xml = store.getSecondaryIndex(xml, Integer.class, "channelId");
			
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
	
	//Data accessor methods
	public synchronized Map<String, UserEntity> getUsers() {
		return users.map();
	}
	
	public synchronized Map<Integer, ChannelEntity> getAllChannels() {
		return channels.map();
	}
	
	public synchronized ChannelEntity getChannelById(int id) {
		return channels.get(id);
	}
	
	public synchronized EntityIndex<Integer, ChannelEntity> getChannelByUserName(String username) {
		EntityIndex<Integer, ChannelEntity> e = si_channels.subIndex(username);
		return e;
	}
	
	public synchronized void addUser(String username, String password) {
		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setPassword(password);
		
		users.put(user);
	}
	
	private Entry<Integer, ChannelEntity> getLastChannel() {
		TreeMap<Integer, ChannelEntity> orderedChannels = new TreeMap<Integer, ChannelEntity>(channels.map());
		Entry<Integer, ChannelEntity> e = orderedChannels.lastEntry();
		/*if (e!=null) {
			System.out.println(channels.delete(e.getKey()));
		}*/
		return e;
	}
	
	public synchronized void addChannel(String username, Set<String> xpaths, String xsl) {
		
		if (!users.contains(username)) {
			System.err.println("No user with given username");
		}
		
		System.out.println("Adding " + username+ " to db");
		int id;
		Entry<Integer, ChannelEntity> c = getLastChannel();
		if(c == null) {
			id = 0;
		} else {
			id = c.getKey();
		}
		System.out.println("User #"+(id+1));
		ChannelEntity channel = new ChannelEntity();
		channel.setId(id+1);
		channel.setUsername(username);
		channel.setXpaths(xpaths);
		channel.setXsl(xsl);
		if (channels.put(channel) == null) {
			System.out.println("New channel was added");
		} else {
			System.out.println("Channel was updated");
		}
	}
	
	public synchronized boolean removeChannel(int id) {
		if (!channels.contains(id)) {
			return true;
		}
		return channels.delete(id);
	}
	
	public synchronized Entry<Integer, QueueEntity> getLastUrl() {
		TreeMap<Integer, QueueEntity> orderedFrontier = new TreeMap<Integer, QueueEntity>(urlFrontier.map());
		Entry<Integer, QueueEntity> e = orderedFrontier.lastEntry();
		
		return e;
	}
	
	public synchronized Entry<Integer, QueueEntity> getNextUrl() {
		TreeMap<Integer, QueueEntity> orderedFrontier = new TreeMap<Integer, QueueEntity>(urlFrontier.map());
		Entry<Integer, QueueEntity> e = orderedFrontier.firstEntry();
		if (e!=null) {
			boolean success = urlFrontier.delete(e.getKey());
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
		
		System.out.println("Adding " + docURL+ " to db");
		int id;
		if(getLastUrl() == null) {
			id = 0;
		} else {
			id = getLastUrl().getKey();
		}
		System.out.println(id+1);
		QueueEntity q_url = new QueueEntity();
		q_url.setId(id+1);
		q_url.setUrl(docURL);
		urlFrontier.put(q_url);
	}
	
	public synchronized Map<String, ContentEntity> getAllContent() {
		EntityCursor<ContentEntity> c = seenContent.entities();
		Iterator<ContentEntity> ir = c.iterator();
		while (ir.hasNext()) {
			ContentEntity ce = ir.next();
			System.out.println(ce.getUrl()+": "+new Date(Long.valueOf(ce.getLastSeen())));
		}
		c.close();
		return seenContent.map();
	}
	
	public synchronized ContentEntity getContentById(String id) {
		ContentEntity ce = seenContent.get(id);
		return ce;
	}
	
	public synchronized void addContent(String url, String content, long date) {
		ContentEntity ce = new ContentEntity();
		ce.setUrl(url);
		ce.setContent(content);
		ce.setLastSeen(String.valueOf(date));
		seenContent.put(ce);
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
	
	public synchronized void addXML(int channelId, String url, String content, long date) {
		XMLEntity xe = new XMLEntity();
		xe.setUrl(url);
		xe.setContent(content);
		xe.setLastSeen(String.valueOf(date));
		xe.setChannelId(channelId);
		xml.put(xe);
	}
	
	public synchronized EntityIndex<String, XMLEntity> getxmlByChannelId(int id) {
		EntityIndex<String, XMLEntity> e = si_xml.subIndex(new Integer(id));
		return e;
	}
	
	public synchronized boolean authenticateUser(String username, String password) {
		if (!users.contains(username)) {
			return false;
		}
		UserEntity user = users.get(username);
		if (user == null) {
			return false;
		}
		return user.getUsername().equals(username) &&
				user.getPassword().equals(password);
	}
	
	public void printUsers() {
		Set<Entry<String, UserEntity>> userEntries = users.map().entrySet();
		
		for (Entry<String, UserEntity> e : userEntries) {
			UserEntity ue = e.getValue();
			System.out.println(e.getKey()+" "+ue.getUsername()+": "+ue.getPassword());
		}
	}
	
	public void printContent() {
		Set<Entry<String, ContentEntity>> seenContentEntries = seenContent.map().entrySet();
		
		for (Entry<String, ContentEntity> e : seenContentEntries) {
			ContentEntity ue = e.getValue();
			System.out.println(e.getKey()+" "+
					ue.getUrl()+": "+ue.getContent().hashCode());
		}
	}
	
	public void printXml() {
		Set<Entry<String, XMLEntity>> xmlEntries = xml.map().entrySet();
		
		for (Entry<String, XMLEntity> e : xmlEntries) {
			XMLEntity ue = e.getValue();
			System.out.println(e.getKey()+" "+ue.getUrl()+": ID: "+ue.getChannelId()+
					"Hash:"+ue.getContent().hashCode());
		}
	}
	
	public void printChannels() {
		Set<Entry<Integer, ChannelEntity>> xmlEntries = channels.map().entrySet();
		
		for (Entry<Integer, ChannelEntity> e : xmlEntries) {
			ChannelEntity ue = e.getValue();
			System.out.println(e.getKey()+" "+ue.getId()+": "+ue.getUsername()+
					ue.getXsl()+ue.getXpaths());
		}
	}
}
