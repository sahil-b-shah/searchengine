package test;

import junit.framework.TestCase;
import indexer.storage.InvertedIndexDBWrapper;

import com.sleepycat.je.DatabaseException;

import crawler.storage.IndexDocumentDBWrapper;

public class InvertedIndexDBWrapperTest extends TestCase {

	public void test() {
		InvertedIndexDBWrapper db = null;
		try {
			db = InvertedIndexDBWrapper.getInstance("/home/cis455/InvertedIndexWorker1/indexdb");
		} catch (DatabaseException e) {}

		
		System.out.println("Size" + db.getSize());
		
		db.printContent();
		
		db.close();
	}
	
	public void test2() {
		IndexDocumentDBWrapper db = null;
		try {
			db = IndexDocumentDBWrapper.getInstance("/home/cis455/InvertedIndexWorker1/indexdocdb");
		} catch (DatabaseException e) {}

		
		System.out.println("Size" + db.getSize());
		
		db.printContent();
		
		db.close();
	}

}
