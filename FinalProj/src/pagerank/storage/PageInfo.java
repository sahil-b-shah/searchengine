package pagerank.storage;

import java.util.HashMap;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class PageInfo {

	@PrimaryKey
	private String url;
	private HashMap<String, Integer> incoming;
	private double prScore;
	
	public void setURL (String data) {
		url = data;
	}
	
	public String getURL(){
		return url;
	}
	
	public void addIncoming(String u, int out){
		incoming.put(u, out);
	}
	
	public HashMap<String, Integer> getIncoming(){
		return incoming;
	}
	
	public void setIncoming(HashMap<String, Integer> inc){
		incoming = inc;
	}
	
	public void setScore(double value){
		prScore = value;
	}
	
	public double getScore() {
		return prScore;
	}
	
	public void resetScore(){
		prScore = 0;
	}
	
}
