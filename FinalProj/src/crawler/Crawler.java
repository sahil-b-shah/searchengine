package crawler;


import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.RobotsDBWrapper;
import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.UnseenLinksDBWrapper;


public class Crawler {
	
	private static String urlString;
	private static String documentDirectory;
	private static String frontierDirectory;
	private static String robotsDirectory;
	private static String unseenLinksDirectory;
	private static int maxSize;
	private static int maxFiles;
	
	private static ThreadPool pool;
	private static ConcurrentHashMap<String, String> currentHosts;
	
	private static void setup() throws DatabaseException, FileNotFoundException {
		pool = new ThreadPool(1, documentDirectory, frontierDirectory, robotsDirectory, unseenLinksDirectory,  maxSize);
	}
	
	
	public static synchronized void deleteCurrentHost(String host){
		currentHosts.remove(host);
	}
	
	public static synchronized boolean addCurrentHost(String host){
		if(currentHosts.contains(host))
			return false;
		currentHosts.put(host, host);
		return true;
	}
	
	public static void main(String [] args) throws DatabaseException, FileNotFoundException {
		if((args.length != 4) && (args.length != 3)) {
			System.err.println("Incorrect number of arguments");
			System.exit(-1);
		}
		
		currentHosts = new ConcurrentHashMap<String, String>();
		
		urlString = args[0];
		//Directory for stores
		documentDirectory = "/documentdb";
		frontierDirectory = "/frontierdb";
		robotsDirectory = "/robotsdb";
		unseenLinksDirectory = "/unseenlinksdb";
		
		//urlString = "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/part.xml";
		urlString = args[1];
		
		maxSize = Integer.parseInt(args[2]);
		if (args.length == 4) {
			maxFiles = Integer.parseInt(args[3]);
		}
		DocumentDBWrapper documentDB = DocumentDBWrapper.getInstance(documentDirectory);
		URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);
		RobotsDBWrapper robotsDB = RobotsDBWrapper.getInstance(robotsDirectory);
		UnseenLinksDBWrapper unseenLinksDB = UnseenLinksDBWrapper.getInstance(unseenLinksDirectory);

		frontierDB.addUrl(urlString);
		documentDB.getAllContent();
		frontierDB.close();
		documentDB.close();
		robotsDB.close();
		unseenLinksDB.close();
		setup();
	}
}
