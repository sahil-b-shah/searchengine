package test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import junit.framework.TestCase;

import com.sleepycat.je.DatabaseException;

import crawler.storage.RobotsDBWrapper;
import crawler.storage.RobotsTxtData;


public class RobotsDBWrapperTest extends TestCase {

	protected void setUp(){

	}
	
	
	public void test1(){
		RobotsDBWrapper db = null;
		try {
			db = RobotsDBWrapper.getInstance("/robotdb");
		} catch (DatabaseException | FileNotFoundException e) {}
		ArrayList<String> allowedLinks = new ArrayList<String>();
		allowedLinks.add("allowed1");
		allowedLinks.add("allowed2");
		ArrayList<String> disallowedLinks = new ArrayList<String>();
		disallowedLinks.add("disallowed1");
		disallowedLinks.add("disallowed2");
		
		db.addRobotsTxt("host", allowedLinks, disallowedLinks, 10);
		
		
		RobotsTxtData data = db.getRobotsTxtData("host");
		assertTrue(data.getAllowedLinks().contains("allowed1"));
		assertFalse(data.getAllowedLinks().contains("allowed100Q0"));
		db.close();
	}
	
	
	public void test2(){
		RobotsDBWrapper db = null;
		RobotsDBWrapper db2 = null;
		try {
			db = RobotsDBWrapper.getInstance("/robotTest");
		} catch (DatabaseException | FileNotFoundException e) {}
		
		ArrayList<String> allowedLinks = new ArrayList<String>();
		allowedLinks.add("allowed1");
		allowedLinks.add("allowed2");
		ArrayList<String> disallowedLinks = new ArrayList<String>();
		disallowedLinks.add("disallowed1");
		disallowedLinks.add("disallowed2");
		
		db.addRobotsTxt("host", allowedLinks, disallowedLinks, 10);
		
		try {
			db2 = RobotsDBWrapper.getInstance("/robotTest");
		} catch (DatabaseException | FileNotFoundException e) {}
		
		RobotsTxtData data = db2.getRobotsTxtData("host");
		assertTrue(data.getAllowedLinks().contains("allowed1"));
		assertFalse(data.getAllowedLinks().contains("allowed100Q0"));
		
		db.close();
		db2.close();
		
		
	}
	

	
}
