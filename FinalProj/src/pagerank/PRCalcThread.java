package pagerank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import pagerank.storage.PageInfo;
import pagerank.storage.PageRankDBWrapper;
import crawler.storage.DocumentData;

public class PRCalcThread extends Thread {

	private PageRankDBWrapper prDB;
	private static long numDocs = 0;
	private static int seen;
	private static double damping = 0.85;

	public PRCalcThread(PageRankDBWrapper DB) {
		this.prDB = DB;
		seen = 0;
	}

	public void run(){

		PageInfo document = prDB.getNextWord();
		while(document != null){
			String url = document.getURL();

			if(url != null){
		//		try{
					HashMap<String, Integer> map = prDB.getUrls(url);
					double pagerank = 1 - damping;
					for(Entry<String, Integer> entry: map.entrySet()){
						try{
							String incoming = entry.getKey();
							double incPR = prDB.getRank(incoming);
							pagerank += damping * (incPR/entry.getValue());
						}catch(NullPointerException e){
							System.out.println(url + "---" + e );
						}
					}
					seen++;
					prDB.addRank(url, pagerank);
					System.out.println("Adding " + url + " seen " +seen + " on thread: " + Thread.currentThread().getName());
					System.out.println("DB SIZE " + prDB.getSize());
	/*			}
				catch(NullPointerException e1){
					System.out.println("prDB doesn't have that url");
				}*/
			}

			prDB.getNextWord();
		}
	}


}
