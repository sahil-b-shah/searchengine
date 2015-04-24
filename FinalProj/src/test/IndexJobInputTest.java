package test;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;


public class IndexJobInputTest extends TestCase  {

	private String content;
	
	public void test() {
		content  = "This is a sentence";
		DocumentDBWrapper db = null;
		DocumentDBWrapper db2 = null;
		try {
			db = DocumentDBWrapper.getInstance("/home/cis455/InvertedIndexWorker1/documentdb");
		} catch (DatabaseException e) {}
		
		
		System.out.println("Adding 1000 docs");
		ArrayList<String> links = new ArrayList<String>();
		links.add("adsfasdfa");
		for(int i = 0; i < 1000; i++){
			db.addContent("url" + i, content, 0, links);
		}
		
		System.out.println("Size: " + db.getSize());

		db.close();
		
		try {
			db2 = DocumentDBWrapper.getInstance("/home/cis455/InvertedIndexWorker2/documentdb");
		} catch (DatabaseException e) {}
		
		
		System.out.println("Adding 1000 docs");
		for(int i = 0; i < 1000; i++){
			db2.addContent("url" + i, content, 0, links);
		}
		
		System.out.println("Size: " + db2.getSize());

		db2.close();


		
		
	}
}
