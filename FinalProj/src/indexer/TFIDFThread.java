package indexer;

import crawler.storage.DocumentData;
import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.InvertedIndexData;

public class TFIDFThread extends Thread {
	
	private InvertedIndexDBWrapper indexDB;

	public TFIDFThread(InvertedIndexDBWrapper indexDB) {
		this.indexDB = indexDB;
	}
	
	public void run(){
		
		InvertedIndexData document = indexDB.getNextWord();
		while(document != null){
			String word = document.getWord();
			//TODO
		}
	}
}
