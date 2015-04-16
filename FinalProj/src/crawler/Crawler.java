package crawler;


import crawler.storage.DocumentDBWrapper;
import crawler.storage.URLFrontierDBWrapper;


public class Crawler {
	
	private static String urlString;
	private static String envDirectory;
	private static int maxSize;
	private static int maxFiles;
	
	private static ThreadPool pool;
	
	private static void setup() {
		pool = new ThreadPool(1, envDirectory, maxSize);
	}
	
	public static void main(String [] args) {
		if((args.length != 4) && (args.length != 3)) {
			System.err.println("Incorrect number of arguments");
			System.exit(-1);
		}
		
		urlString = args[0];
		//Directory for store
		envDirectory = args[1];
		urlString = "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/part.xml";
		
		maxSize = Integer.parseInt(args[2]);
		if (args.length == 4) {
			maxFiles = Integer.parseInt(args[3]);
		}
		DocumentDBWrapper documentDB = DocumentDBWrapper.getInstance(envDirectory);
		URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(envDirectory);

		frontierDB.addUrl(urlString);
		documentDB.getAllContent();
		frontierDB.close();
		documentDB.close();
		setup();
	}
}
