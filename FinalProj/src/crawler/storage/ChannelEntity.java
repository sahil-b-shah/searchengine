package crawler.storage;

import java.util.Set;

import com.sleepycat.persist.model.DeleteAction;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class ChannelEntity {
	
	@SecondaryKey(relate=Relationship.MANY_TO_ONE, relatedEntity=UserEntity.class, onRelatedEntityDelete=DeleteAction.CASCADE)
	private String username;
	private Set<String> xpaths;
	private String xsl;
	
	@PrimaryKey
	private Integer id;
	
	public void setId(int data) {
		id = data;
	}
	
	public void setUsername(String data) {
		username = data;
	}
	
	public void setXpaths(Set<String> data) {
		xpaths = data;
	}
	
	public void setXsl(String data) {
		xsl = data;
	}
	
	public String getUsername() {
		return username;
	}
	
	public Set<String> getXpaths() {
		return xpaths;
	}
	
	public String getXsl() {
		return xsl;
	}
	
	public int getId() {
		return id;
	}
}
