package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class UserEntity {
	
	@PrimaryKey
	private String username;
	private String password;
	
	public void setUsername(String data) {
		username = data;
	}
	
	public void setPassword(String data) {
		password = data;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
