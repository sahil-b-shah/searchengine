package crawler.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class RobotsTxtData{

	@PrimaryKey
	private String hostName;
	private ArrayList<String> allowedLinks;
	private ArrayList<String> disallowedLinks;
	private long crawlDelay;
	
	public RobotsTxtData(String hostName, ArrayList<String> allowedLinks,  ArrayList<String> disallowedLinks, long crawlDelay)
	{
		this.hostName = hostName;
		this.allowedLinks = allowedLinks;
		this.disallowedLinks = disallowedLinks;
		this.crawlDelay = crawlDelay;
	}
	
	public final String getHostName(){
		return hostName;
	}
	
	public final ArrayList<String> getAllowedLinks(){
		return allowedLinks;	
	}
	
	public final ArrayList<String> getDisallowedLinks(){
		return allowedLinks;	
	}
	
	public final long getCrawlDelay(){
		return crawlDelay;
	}
}
