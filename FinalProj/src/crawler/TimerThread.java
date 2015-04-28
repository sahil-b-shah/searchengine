package crawler;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.RobotsDBWrapper;
import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.UnseenLinksDBWrapper;

public class TimerThread extends Thread {
	
	private DocumentDBWrapper docDB;
	private URLFrontierDBWrapper frontierDB;
	private RobotsDBWrapper robotsDB;
	private UnseenLinksDBWrapper unseenLinksDB;
	
	public TimerThread(DocumentDBWrapper docDB, URLFrontierDBWrapper frontierDB, RobotsDBWrapper robotsDB, UnseenLinksDBWrapper unseenLinksDB) {
		this.docDB = docDB;
		this.frontierDB = frontierDB;
		this.robotsDB = robotsDB;
		this.unseenLinksDB = unseenLinksDB;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Time started");
			Thread.sleep(14400000);
			System.out.println("Done time");
			System.out.println("AFTER document DB size: "+docDB.getSize());
			System.out.println("AFTER unseen links size: "+unseenLinksDB.getSize());
			System.out.println("Closing entire crawler");
			frontierDB.close();
			docDB.close();
			unseenLinksDB.close();
			robotsDB.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
