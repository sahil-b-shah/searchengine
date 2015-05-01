package search;

import java.util.HashMap;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class SearchData {
	
	private double score;
	private int queryHits;
	
	public SearchData(double score, int queryHits){
		this.score = score;
		this.queryHits = queryHits;
	}
	
	public double getScore(){
		return score;
	}
	public int getQueryHits(){
		return queryHits;
	}
	
	public void setScore(double newScore){
		score = newScore;
	}
	public void incrementQueryHits(){
		queryHits ++;
	}
	
	
}