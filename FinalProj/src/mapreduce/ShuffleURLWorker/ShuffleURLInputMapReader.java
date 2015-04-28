package mapreduce.ShuffleURLWorker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;

import crawler.storage.UnseenLinksDBWrapper;
import crawler.storage.UnseenLinksData;

public class ShuffleURLInputMapReader {


	private UnseenLinksDBWrapper unseenLinksDB;
	private boolean done;
	private int numLinks;
	
	
	public ShuffleURLInputMapReader(String unseenLinksDirectory) throws FileNotFoundException{
		unseenLinksDB = UnseenLinksDBWrapper.getInstance(unseenLinksDirectory);
		done = false;
		numLinks = 0;
	}
	
	/**
	 * Gets next line
	 * @return line read, or null if done
	 * @throws IOException
	 */
	public synchronized String readLine() throws IOException{
		
		if(done || numLinks > 2000){
			done = true;
			if(unseenLinksDB != null) 
				unseenLinksDB.close();
			System.out.println("Done, returning null");
			return null;
		}
		
		Entry<String, UnseenLinksData> link = unseenLinksDB.getNextUrl();
		String line = null;
		
		if(link == null){
			done = true;
		}
		else{
			line = link.getKey();
			numLinks++;
			System.out.println("Numkeys: " + numLinks);
		}
		
		return line;
	}
	
}
