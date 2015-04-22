package mapreduce.InvertedIndexWorker;

import java.io.IOException;

import mapreduce.Context;
import mapreduce.Job;


public class InvertedIndexWorkerMapThread implements Runnable {

	private Job job;
	private InvertedIndexInputMapReader reader;
	private Context context;

	@SuppressWarnings("rawtypes")
	public InvertedIndexWorkerMapThread(Class jobClass, InvertedIndexInputMapReader reader, Context context){
		try {
			if(jobClass != null)
				job = (Job) jobClass.newInstance();
			else
				job = null;
			this.reader = reader;
			this.context = context;
			
		} catch (InstantiationException | IllegalAccessException e) {
			System.err.println("Can't instantiate this class");
		}
	}

	@Override
	public void run() {

		if(job == null)
			return;
		try {
			String line = reader.readLine();
			while(line != null){
				String params[] = line.split("\\t");
				job.map(params[0], params[1], context);
				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("Error reading from map input");
		}

	}

}
