package crawler.storage;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class XMLEntity {

	@PrimaryKey
	private String url;
	private String content;
	private String lastSeen;
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=ChannelEntity.class, onRelatedEntityDelete=DeleteAction.CASCADE)
	private Integer channelId;
	
	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}
	
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
	
	public String getUrl() {
		return url;
	}
	
	public String getLastSeen() {
		return lastSeen;
	}
	
	public Integer getChannelId() {
		return channelId;
	}
}
