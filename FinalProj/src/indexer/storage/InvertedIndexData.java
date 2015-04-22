package indexer.storage;

import java.util.HashMap;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class InvertedIndexData {

	@PrimaryKey
	private String word;
	private HashMap<String, Integer> document;
	
	public void setWord (String data) {
		word = data;
	}
	
	public String getWord(){
		return word;
	}
	
	public void setMap(HashMap<String, Integer> map){
		document = map;
	}
	
	public HashMap<String, Integer> getUrls() {
		return document;
	}
	
}
