package test;

import java.util.Map.Entry;

import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.URLFrontierData;
import junit.framework.TestCase;

public class FrontierDBTest extends TestCase{
	
	public void test1() {
		URLFrontierDBWrapper db = URLFrontierDBWrapper.getInstance("/home/cis455/test/frontierdbTest");
		db.addUrl("http://www.visitphilly.com/");
		db.addUrl("http://www.upenn.edu/");
		db.addUrl("http://philadelphia.cbslocal.com/");
		db.addUrl("https://www.yahoo.com/");
		
		db.getAll();
		
		Entry<Integer, URLFrontierData> data = db.getNextUrl();
		assertEquals(data.getKey(), 0, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://www.visitphilly.com/"));
		
		data = db.getNextUrl();
		assertEquals(data.getKey(), 1, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://www.upenn.edu/"));
		
		data = db.getNextUrl();
		assertEquals(data.getKey(), 2, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://philadelphia.cbslocal.com/"));
		
		data = db.getNextUrl();
		assertEquals(data.getKey(), 3, 0.0001);
		assertTrue(data.getValue().getUrl().equals("https://www.yahoo.com/"));
		
		db.close();
		
	}
	
	public void test2() {
		
		URLFrontierDBWrapper db = URLFrontierDBWrapper.getInstance("/home/cis455/test/frontierdbTest");
		db.addUrl("http://www.visitphilly.com/");
		
		Entry<Integer, URLFrontierData> data = db.getNextUrl();
		assertEquals(data.getKey(), 0, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://www.visitphilly.com/"));
		
		db.addUrl("http://www.upenn.edu/");
		data = db.getNextUrl();
		assertEquals(data.getKey(), 1, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://www.upenn.edu/"));
		
		db.getAll();
		db.close();
		
	}
	
	public void test3() {
		
		URLFrontierDBWrapper db = URLFrontierDBWrapper.getInstance("/home/cis455/test/frontierdbTest");
		db.addUrl("http://www.visitphilly.com/");
		db.addUrl("http://www.upenn.edu/");		
		db.close(); 
		
		URLFrontierDBWrapper db2 = URLFrontierDBWrapper.getInstance("/home/cis455/test/frontierdbTest");
		db2.addUrl("http://philadelphia.cbslocal.com/");

		Entry<Integer, URLFrontierData> data = db2.getNextUrl();
		assertEquals(data.getKey(), 0, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://www.visitphilly.com/"));
		
		data = db2.getNextUrl();
		assertEquals(data.getKey(), 1, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://www.upenn.edu/"));
		
		data = db2.getNextUrl();
		assertEquals(data.getKey(), 2, 0.0001);
		assertTrue(data.getValue().getUrl().equals("http://philadelphia.cbslocal.com/"));
		
		
		db2.getAll();
		db2.close();
	}

}
