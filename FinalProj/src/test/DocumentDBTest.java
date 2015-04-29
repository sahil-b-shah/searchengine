package test;

import crawler.storage.DocumentDBWrapper;

public class DocumentDBTest {
	
	public static void main(String[] args) {
		
		DocumentDBWrapper db = DocumentDBWrapper.getInstance("/home/cis455/ShuffleURLWorker1/documentDB");
		db.printAllContent();
	}
}
