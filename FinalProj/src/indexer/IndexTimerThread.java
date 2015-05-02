package indexer;

import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.WordCountDBWrapper;



public class IndexTimerThread extends Thread {

	private InvertedIndexDBWrapper indexDB;
	private WordCountDBWrapper maxDB;
	
	public IndexTimerThread(InvertedIndexDBWrapper indexDB,WordCountDBWrapper maxDB) {
		this.indexDB = indexDB;
		this.maxDB = maxDB;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Time started");
			Thread.sleep(28800000);
			System.out.println("Done time");
			System.out.println("AFTER index DB size: "+indexDB.getSize());
			System.out.println("AFTER max links size: "+maxDB.getSize());
			System.out.println("Closing entire indexer");

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
