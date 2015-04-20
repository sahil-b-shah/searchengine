package mapreduce.ShuffleURL;

import java.io.IOException;

import mapreduce.Context;
import mapreduce.Job;


public class ShuffleURLMapThread implements Runnable {

	private Job job;
	private ShuffleURLInputMapReader reader;
	private Context context;

	public ShuffleURLMapThread(ShuffleURLInputMapReader reader, Context context){

		job = new ShuffleURLJob();
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
				String params[] = line.split("\\t");
				job.map(params[0], params[1], context);
				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("Error reading from map input");
		}

	}

}
