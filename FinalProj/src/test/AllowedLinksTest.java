package test;

import java.util.ArrayList;

import crawler.CrawlerThread;
import junit.framework.TestCase;

public class AllowedLinksTest extends TestCase {


	public void test1(){
		ArrayList<String> allowed = new ArrayList<String>();
		ArrayList<String> disallowed = new ArrayList<String>();
		
		//allowed paths
		allowed.add("/allowed");
	
		disallowed.add("/disallowed");
		disallowed.add("/test1/test2/");
		disallowed.add("/test3/test4");
		
		assertTrue(CrawlerThread.allowed("/allowed/test", disallowed, allowed));
		assertTrue(CrawlerThread.allowed("/allowed", disallowed, allowed));
		assertTrue(CrawlerThread.allowed("/asdggasd", disallowed, allowed));
		assertTrue(CrawlerThread.allowed("/test1/test2", disallowed, allowed));
		
		assertFalse(CrawlerThread.allowed("/disallowed", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/disallowed/", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test1/test2/", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test1/test2/sfsfh", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test3/test4/", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test3/test4/sfsfh", disallowed, allowed));
		
	}
	
	public void test2(){
		ArrayList<String> allowed = new ArrayList<String>();
		ArrayList<String> disallowed = new ArrayList<String>();
		

		disallowed.add("/");

		
		assertFalse(CrawlerThread.allowed("/allowed/test", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/allowed", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/asdggasd", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test1/test2", disallowed, allowed));
		
		assertFalse(CrawlerThread.allowed("/disallowed", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/disallowed/", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test1/test2/", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test1/test2/sfsfh", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test3/test4/", disallowed, allowed));
		assertFalse(CrawlerThread.allowed("/test3/test4/sfsfh", disallowed, allowed));
		
	}
}
