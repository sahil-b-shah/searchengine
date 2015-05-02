package pagerank;

import java.util.HashSet;

import pagerank.storage.PageRankDBWrapper;

public class PRCalc {

	public static void main(String args[]){
		String prDirectory = args[0];
		PageRankDBWrapper prDB = PageRankDBWrapper.getInstance(prDirectory);
		prDB.initIterator();
		
		Thread threadpool[] = new Thread[10];
        for(int i=0; i<threadpool.length; i++){
        	threadpool[i] = (new PRCalcThread(prDB));
        }
        
        for(int i=0; i<threadpool.length; i++){
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
            threadpool[i].start();
        }
        
        
		//Wait until all threads done
        for(int i=0; i<threadpool.length; i++){
			try {
				threadpool[i].join();
			} catch (InterruptedException e) {
				System.err.println("Index thread ended unnaturally");
			}
		}
        
        System.out.println("PageRank done");
        prDB.closeIterator();;
		prDB.close();
		
	}

}
