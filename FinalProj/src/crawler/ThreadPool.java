package crawler;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import mapreduce.ShuffleURLWorker.ShuffleURLMapThread;

import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.RobotsDBWrapper;
import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.UnseenLinksDBWrapper;


public class ThreadPool {

	//private BlockingQueue bq = null;
    private List<Thread> threads = new ArrayList<Thread>();
    public ThreadPool(int noOfThreads, String documentDirectory, String frontierDirectory, String robotsDirectory, String unseenLinksDirectory,  int maxSize) throws DatabaseException, FileNotFoundException{
        //bq = new BlockingQueue();
    	DocumentDBWrapper docDB = DocumentDBWrapper.getInstance(documentDirectory);
    	URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);
		RobotsDBWrapper robotsDB = RobotsDBWrapper.getInstance(robotsDirectory);
		UnseenLinksDBWrapper unseenLinksDB = UnseenLinksDBWrapper.getInstance(unseenLinksDirectory);
    	/*while (!db.isEmpty()) {
			Entry<Integer, QueueEntity> entry1 = db.getNextUrl();
			System.out.println(entry1.getKey()+": " + entry1.getValue().getUrl());
		}*/
    	
        for(int i=0; i<noOfThreads; i++){
            threads.add(new CrawlerThread(docDB, frontierDB, robotsDB, unseenLinksDB, maxSize));
        }
        for(Thread thread : threads){
            thread.start();
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
		//Wait until all threads done
		for(Thread thread : threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Map thread ended unnaturally");
			}
		}
		
		System.out.println("Closing entire crawler");
		frontierDB.close();
		docDB.close();
		unseenLinksDB.close();
		robotsDB.close();

    }

    /*public synchronized void  execute(Socket request) throws Exception{
        if(this.isStopped) throw
            new IllegalStateException("ThreadPool is stopped");

        this.bq.enqueue(request);
    }*/

    public synchronized void stop(){
        notifyAll();
        for(Thread thread : threads){
           ((CrawlerThread) thread).doStop();
        }
    }
    
    public synchronized List<Thread> getThreadList() {
    	return this.threads;
    }
}
