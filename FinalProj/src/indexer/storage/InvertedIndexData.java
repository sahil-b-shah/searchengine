package indexer.storage;

import java.util.HashMap;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class InvertedIndexData {

	@PrimaryKey
	private String word;
	private HashMap<String, URLMetrics> document;
	
	public void setWord (String data) {
		word = data;
	}
	
	public String getWord(){
		return word;
	}
	
	public void setMap(HashMap<String, URLMetrics> map){
		document = map;
	}
	
	public HashMap<String, URLMetrics> getUrls() {
		return document;
	}
	
}
