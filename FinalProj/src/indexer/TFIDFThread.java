package indexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import crawler.storage.DocumentData;
import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.InvertedIndexData;
import indexer.storage.URLMetrics;
import indexer.storage.WordCountDBWrapper;

public class TFIDFThread extends Thread {

	private InvertedIndexDBWrapper indexDB;
	private WordCountDBWrapper maxDB;
	private HashSet<String> stopWords;
	private static long numDocs = 0;
	private static int seen;

	public TFIDFThread(InvertedIndexDBWrapper indexDB,WordCountDBWrapper maxDB, HashSet<String> stopWords) {
		this.indexDB = indexDB;
		this.maxDB = maxDB;
		this.stopWords = stopWords;
		numDocs = maxDB.getSize();
		seen = 0;
	}

	public void run(){


		InvertedIndexData document = indexDB.getNextWord();
		while(document != null){
			String word = document.getWord();
			if(word != null){
				HashMap<String, URLMetrics> map = indexDB.getURLMap(word);
				HashMap<String, URLMetrics> newMap = new HashMap<String, URLMetrics>();

				for(Entry<String, URLMetrics> entry: map.entrySet()){
					try{
						String url = entry.getKey();
						double tf = 0.5 + (1- 0.5)*((double)entry.getValue().getOccurences()/maxDB.getNumberWords(url));
						double idf = (numDocs/indexDB.getURLMapSize(word));
						idf = Math.log(idf)/Math.log(2.0);
						URLMetrics urlMetrics = new URLMetrics(entry.getValue().getOccurences(),0,0);
						urlMetrics.setIDF(idf);
						urlMetrics.setTF(tf);
						newMap.put(url, urlMetrics);
						System.out.println("URL:" + url + "tf: " +tf + "idf:" + idf);
					}catch(NullPointerException e){
						System.out.println(word + "---" + e );
					}
				}
				seen++;
				indexDB.addWord(word, newMap);
				System.out.println("Adding " + word + " seen " +seen + " on thread: " + Thread.currentThread().getName());
				System.out.println("DB SIZE " + indexDB.getSize());


			}
			document = indexDB.getNextWord();
		}
	}


}
