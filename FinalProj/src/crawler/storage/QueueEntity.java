package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class QueueEntity {

	@PrimaryKey
	private Integer id;
	private String url;
	
	public void setUrl (String data) {
		url = data;
	}
	
	public void setId (int data) {
		id = data;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getId() {
		return id;
	}
}
