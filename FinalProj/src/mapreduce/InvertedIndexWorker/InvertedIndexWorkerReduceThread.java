package mapreduce.InvertedIndexWorker;

import java.io.IOException;

import mapreduce.Context;
import mapreduce.InvertedIndexJob;
import mapreduce.Job;


public class InvertedIndexWorkerReduceThread implements Runnable {

	private Job job;
	private InvertedIndexInputReduceReader reader;
	private Context context;

	public InvertedIndexWorkerReduceThread(InvertedIndexInputReduceReader reader, Context context) {
		job = new InvertedIndexJob();
		this.reader = reader;
		this.context = context;
	}

	@Override
	public void run() {
		String line;
		if(job == null)
			return;
		try {
			line = reader.readLine();
			while(line != null){
				String params[] = line.split("\\t");
				String key = params[0];
				String value = params[1];
				String[] valueArray = value.split("\\s+");

				job.reduce(key, valueArray, context);

				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("Error reading from reduce input");
		}
	}

}
