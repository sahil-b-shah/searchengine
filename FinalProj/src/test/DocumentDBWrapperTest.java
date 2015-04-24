package test;

import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;
import junit.framework.TestCase;

public class DocumentDBWrapperTest extends TestCase {

	
	public void test1(){
		DocumentDBWrapper db = null;
		try {
			db = DocumentDBWrapper.getInstance("/home/cis455/docTest");
		} catch (DatabaseException e) {}

		
		for(int i = 0; i < 10; i++){
			db.addContent("Doc" + i, ""+ i, 0, null);
		}
		
		db.printInOrder();
		
		db.close();
		
		
	}
}
