package mapreduce.InvertedIndexWorker;

import java.util.HashMap;

import indexer.storage.InvertedIndexDBWrapper;
import mapreduce.Context;


public class InvertedIndexReduceContext implements Context {

	private InvertedIndexDBWrapper indexDB;
	
	public InvertedIndexReduceContext(String indexDirectory) {
		indexDB = InvertedIndexDBWrapper.getInstance(indexDirectory);
	}

	public synchronized void write(String key, String value) {
		
		String entry[] = value.split("\\s+");
		HashMap<String, Integer> map = indexDB.getUrls(key);
		if(map == null)
			map = new HashMap<String, Integer>();

		for(int i = 0; i < entry.length; i++){
			String tempEntry = entry[i];
			String params [] = tempEntry.split(";");
			map.put(params[0], Integer.parseInt(params[1]));
		}
		System.out.println("Reduce emit " + key + map.size());

				
		indexDB.addWord(key, map);

	}
	
	public void close(){
		indexDB.close();
	}
	
}
