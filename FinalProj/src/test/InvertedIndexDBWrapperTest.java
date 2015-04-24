package test;

import junit.framework.TestCase;
import indexer.storage.InvertedIndexDBWrapper;

import com.sleepycat.je.DatabaseException;

public class InvertedIndexDBWrapperTest extends TestCase {

	public void test() {
		InvertedIndexDBWrapper db = null;
		try {
			db = InvertedIndexDBWrapper.getInstance("/home/cis455/InvertedIndexWorker1/indexdb");
		} catch (DatabaseException e) {}

		
		System.out.println("Size");
		
		db.printContent();
		
		db.close();
	}

}
