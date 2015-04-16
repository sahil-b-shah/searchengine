package crawler;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.QueueEntity;

public class ThreadPool {

	//private BlockingQueue bq = null;
    private List<Thread> threads = new ArrayList<Thread>();
    private boolean isStopped = false;

    public ThreadPool(int noOfThreads, String envDirectory, int maxSize){
        //bq = new BlockingQueue();
    	DBWrapper db = DBWrapper.getInstance(envDirectory);
    	/*while (!db.isEmpty()) {
			Entry<Integer, QueueEntity> entry1 = db.getNextUrl();
			System.out.println(entry1.getKey()+": " + entry1.getValue().getUrl());
		}*/
    	
        for(int i=0; i<noOfThreads; i++){
            threads.add(new CrawlerThread(db, maxSize));
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
    }

    /*public synchronized void  execute(Socket request) throws Exception{
        if(this.isStopped) throw
            new IllegalStateException("ThreadPool is stopped");

        this.bq.enqueue(request);
    }*/

    public synchronized void stop(){
        this.isStopped = true;
        notifyAll();
        for(Thread thread : threads){
           ((WorkerThread) thread).doStop();
        }
    }
    
    public synchronized List<Thread> getThreadList() {
    	return this.threads;
    }
}
