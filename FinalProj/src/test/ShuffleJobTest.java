package test;

import crawler.storage.URLFrontierDBWrapper;
import junit.framework.TestCase;

public class ShuffleJobTest extends TestCase {

	public void testFrontierDB() {
		URLFrontierDBWrapper worker1frontierdb = URLFrontierDBWrapper.getInstance("/home/cis455/ShuffleWorker1");
		URLFrontierDBWrapper worker2frontierdb = URLFrontierDBWrapper.getInstance("/home/cis455/ShuffleWorker1");

	}
}
