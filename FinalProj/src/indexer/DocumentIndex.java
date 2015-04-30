package indexer;

import java.util.HashMap;

public class DocumentIndex {

	private HashMap<String, Integer> map;
	private int maxOccurence;
	private String maxWord;
	
	
	public DocumentIndex(){
		map = new HashMap<String, Integer>();
		maxOccurence = 0;
		maxWord = "";
	}
	
	public void addWord(String word){
		int occur = 0;
		if(map.get(word) != null){
			occur = map.get(word);
		}
		occur += 1;

		if(occur >= maxOccurence){
			maxWord = word;
			maxOccurence = occur;
		}
		
		map.put(word, occur);
	}

	public HashMap<String, Integer> getWords() {
		return map;
	}

	public String getMaxWord() {
		return maxWord;
	}

	public String getMaxOccurence() {
		return maxOccurence + "";
	}
	
	
}
