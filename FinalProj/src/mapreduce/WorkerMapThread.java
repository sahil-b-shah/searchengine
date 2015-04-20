package edu.upenn.cis455.mapreduce.worker;

import java.io.IOException;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WorkerMapThread implements Runnable {

	private Job job;
	private InputMapReader reader;
	private Context context;

	@SuppressWarnings("rawtypes")
	public WorkerMapThread(Class jobClass, InputMapReader reader, Context context){
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
