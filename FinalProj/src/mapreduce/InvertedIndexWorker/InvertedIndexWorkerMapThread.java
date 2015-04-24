package mapreduce.InvertedIndexWorker;

import java.io.IOException;

import mapreduce.Context;
import mapreduce.InvertedIndexJob;
import mapreduce.Job;


public class InvertedIndexWorkerMapThread implements Runnable {

	private Job job;
	private InvertedIndexInputMapReader reader;
	private Context context;

	public InvertedIndexWorkerMapThread(InvertedIndexInputMapReader reader, Context context){
		job = new InvertedIndexJob();
		this.reader = reader;
		this.context = context;
	}

	@Override
	public void run() {

		if(job == null)
			return;
		try {
			String line = reader.readLine();
			while(line != null){
				System.out.println(Thread.currentThread().getName() + " reading map");
				String params[] = line.split("\\s+");
				job.map(params[0], params[1], context);
				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("Error reading from map input");
		}

	}

}
