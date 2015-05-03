package pagerank;

import java.util.HashMap;
import java.util.Map.Entry;

import pagerank.storage.PageInfo;
import pagerank.storage.PageRankDBWrapper;

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
			System.out.println("NEW URL: " + url);
			if(url != null){
				//		try{
				HashMap<String, Integer> map = prDB.getUrls(url);
				double pagerank = 1 - damping;
				String incoming = "";
				for(Entry<String, Integer> entry: map.entrySet()){

					incoming = entry.getKey();
					System.out.println("INC: " + incoming);
					double incPR = .5;
					try{
						incPR = prDB.getRank(incoming);
					}catch(NullPointerException e){
						System.out.println(e);
					}
					pagerank += damping * (incPR/entry.getValue());

				}
				prDB.addRank(url, pagerank);
				//System.out.println("Adding " + url + " seen " +seen + " on thread: " + Thread.currentThread().getName());
				//ystem.out.println("DB SIZE " + prDB.getSize());
				/*			}
				catch(NullPointerException e1){
					System.out.println("prDB doesn't have that url");
				}*/
			}
			seen++;
			System.out.println("PageRank is: " + prDB.getRank(url));
			System.out.println("Seen: " + seen);
			document = prDB.getNextWord();
		}
	}


}
