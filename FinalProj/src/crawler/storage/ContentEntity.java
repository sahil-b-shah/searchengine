package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class ContentEntity {

	@PrimaryKey
	private String url;
	private String content;
	private String lastSeen;
	
	public void setUrl(String data) {
		url = data;
	}
	
	public void setContent(String data) {
		content = data;
	}
	
	public void setLastSeen(String data) {
		lastSeen = data;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getLastSeen() {
		return lastSeen;
	}
	
	public String getUrl() {
		return url;
	}
}
