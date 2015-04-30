package test;

import crawler.storage.URLFrontierDBWrapper;
import junit.framework.TestCase;

public class ShuffleJobTest extends TestCase {

	public void testFrontierDB() {
		URLFrontierDBWrapper worker1frontierdb = URLFrontierDBWrapper.getInstance("/home/cis455/ShuffleWorker1/frontierdb");
		
		System.out.println("Printing worker 1 bitch----------------------------------");
		worker1frontierdb.getAll();
		worker1frontierdb.close();
		
		
		URLFrontierDBWrapper worker2frontierdb = URLFrontierDBWrapper.getInstance("/home/cis455/ShuffleWorker2/frontierdb");

		System.out.println("Printing worker 2 bitch----------------------------------");
		worker2frontierdb.getAll();
		worker2frontierdb.close();
	}
}
