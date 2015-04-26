package test;


import java.io.FileNotFoundException;

import com.sleepycat.je.DatabaseException;

import crawler.storage.UnseenLinksDBWrapper;
import junit.framework.TestCase;

public class PopulateUnseenLinks extends TestCase {
	public void test() throws DatabaseException, FileNotFoundException{
		UnseenLinksDBWrapper unseenlinks = UnseenLinksDBWrapper.getInstance("/unseenlinksdb");
		unseenlinks.addURL("");
		unseenlinks.close();
	}

}
