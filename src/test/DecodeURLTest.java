package test;

import java.io.UnsupportedEncodingException;

import crawler.CrawlerThread;
import junit.framework.TestCase;

public class DecodeURLTest extends TestCase {

	
	public void test1() throws UnsupportedEncodingException{	
		assertEquals(CrawlerThread.decodeURL("/%7ejoe/%7eindex.html"), "/~joe/~index.html");
		assertEquals(CrawlerThread.decodeURL("/a%2fb.html"), "/a%2fb.html");
	}
}
