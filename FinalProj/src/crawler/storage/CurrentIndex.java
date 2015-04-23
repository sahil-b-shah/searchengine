package crawler.storage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CurrentIndex {

	@PrimaryKey
	private String name;
	private int index;
	
	public CurrentIndex(){
		
	}
	
	public CurrentIndex(String name){
		index = 0;
		this.name = name;
	}
	
	public void setIndex(int data){
		index = data;
	}
	
	public int getIndex(){
		return index;
	}
}
