package crawler;


import crawler.storage.DocumentDBWrapper;
import crawler.storage.URLFrontierDBWrapper;


public class Crawler {
	
	private static String urlString;
	private static String documentDirectory;
	private static String frontierDirectory;
	private static int maxSize;
	private static int maxFiles;
	
	private static ThreadPool pool;
	
	private static void setup() {
		pool = new ThreadPool(1, documentDirectory, frontierDirectory, maxSize);
	}
	
	public static void main(String [] args) {
		if((args.length != 6) && (args.length != 5)) {
			System.err.println("Incorrect number of arguments");
			System.exit(-1);
		}
		
		urlString = args[0];
		//Directory for stores
		documentDirectory = args[1];
		frontierDirectory = args[2];
		//urlString = "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/part.xml";
		urlString = args[3];
		
		maxSize = Integer.parseInt(args[4]);
		if (args.length == 6) {
			maxFiles = Integer.parseInt(args[5]);
		}
		DocumentDBWrapper documentDB = DocumentDBWrapper.getInstance(documentDirectory);
		URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);

		frontierDB.addUrl(urlString);
		documentDB.getAllContent();
		frontierDB.close();
		documentDB.close();
		setup();
	}
}
