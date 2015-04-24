package test;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;
import crawler.storage.IndexDocumentDBWrapper;


public class IndexJobInputTest extends TestCase  {

	private String content;
	
	public void test() {
		content  = "";
		DocumentDBWrapper db = null;
		try {
			db = DocumentDBWrapper.getInstance("/home/cis455/docTest");
		} catch (DatabaseException e) {}
		
		
		System.out.println("Adding 1000 docs");
		ArrayList<String> links = new ArrayList<String>();
		links.add("adsfasdfa");
		for(int i = 0; i < 10000; i++){
			db.addContent("url" + i, content, 0, links);
		}
		
		System.out.println("Size: " + db.getSize());
		
		IndexDocumentDBWrapper indexDB = IndexDocumentDBWrapper.getInstance("/home/cis455/indexTest");
		
		long size = db.getSize();
		
		db.initIterator();
		for(long i = 0; i < size; i++){
			DocumentData data = db.getNextDocument();
			indexDB.addContent(data.getUrl(), data.getContent(), Long.parseLong(data.getLastSeen()), data.getLinks());
		}
		
		System.out.println("Index Size: " + indexDB.getSize());
		System.out.println("Doc DB Size: " + db.getSize());
		
		db.close();
		indexDB.close();

		
		
	}
}
