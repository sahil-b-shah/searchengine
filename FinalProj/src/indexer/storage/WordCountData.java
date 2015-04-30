package indexer.storage;


import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class WordCountData {
	@PrimaryKey
	private String url;
	private int num;
	
	public void setURL (String data) {
		url = data;
	}
	
	public String getURL(){
		return url;
	}
	
	public void setNumberWords(int num){
		this.num = num;
	}
	
	public int getNumber() {
		return num;
	}
	
}
