package test;


import java.io.FileNotFoundException;

import com.sleepycat.je.DatabaseException;

import crawler.storage.UnseenLinksDBWrapper;
import junit.framework.TestCase;

public class PopulateUnseenLinks extends TestCase {
	public void test() throws DatabaseException, FileNotFoundException{
		UnseenLinksDBWrapper unseenlinks = UnseenLinksDBWrapper.getInstance("/home/cis455/unseenlinksdb");
		unseenlinks.addURL("http://www.thedp.com/");
		unseenlinks.addURL("http://www.philly.com/");
		unseenlinks.addURL("http://www.visitphilly.com/");
		unseenlinks.addURL("http://www.upenn.edu/ ");
		unseenlinks.addURL("http://philadelphia.cbslocal.com/");
		unseenlinks.addURL("https://www.yahoo.com/");
		unseenlinks.close();
	}

}
