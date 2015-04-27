package crawler;


import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.UnseenLinksDBWrapper;
import crawler.storage.UnseenLinksData;


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
	/**
	 * 
	 * @param args
	 * 0: seed url
	 * 1: location of home directory
	 * 3: max size of files being downloaded
	 * optional 4: max number of files to be downloaded
	 * @throws DatabaseException
	 * @throws FileNotFoundException
	 */
	public static void main(String [] args) throws DatabaseException, FileNotFoundException {
		
		if((args.length != 4) && (args.length != 3)) {
			System.err.println("Incorrect number of arguments");
			System.exit(-1);
		}
		
		currentHosts = new ConcurrentHashMap<String, String>();
		
		//urlString = "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/part.xml";
		urlString = args[0];
		String homeDir = args[1];
		//Directory for stores
		documentDirectory = homeDir+"/documentdb";
		frontierDirectory = homeDir+"/frontierdb";
		robotsDirectory = homeDir+"/robotsdb";
		unseenLinksDirectory = homeDir+"/unseenlinksdb";
		
		maxSize = Integer.parseInt(args[2]);
		if (args.length == 4) {
			maxFiles = Integer.parseInt(args[3]);
		}
		
		DocumentDBWrapper docDB = DocumentDBWrapper.getInstance(documentDirectory);
		System.out.println("Printing current document DB size: "+docDB.getSize());
		docDB.close();
		
		/*System.out.println("Prinitng unseen links");
		UnseenLinksDBWrapper unseenDB = UnseenLinksDBWrapper.getInstance(unseenLinksDirectory);
		unseenDB.getAllContent();
		unseenDB.close();*/
		
		//seedFromUnseen();
		
		URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);
		frontierDB.addUrl(urlString);
		setup();
	}
	
	private static void seedFromUnseen() throws DatabaseException, FileNotFoundException {
		Map<String, UnseenLinksData> unseenMap = UnseenLinksDBWrapper.getInstance(unseenLinksDirectory).getAllContent();
		Set<String> links = unseenMap.keySet();
		
		URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);
		
		for (String link : links) {
			frontierDB.addUrl(link);
		}
		frontierDB.close();
	}
}
