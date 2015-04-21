package crawler.storage;

import com.sleepycat.persist.model.PrimaryKey;

public class UnseenLinksData {

	@PrimaryKey
	private String url;
	
	public UnseenLinksData(){
		
	}
	
	public UnseenLinksData(String url)
	{
		this.url = url;
	}
}
