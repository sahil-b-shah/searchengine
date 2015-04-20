package mapreduce.ShuffleURL;

import java.io.IOException;

import mapreduce.Context;
import mapreduce.Job;

public class ShuffleURLReduceThread implements Runnable {

	private Job job;
	private ShuffleURLInputReduceReader reader;
	private Context context;

	public ShuffleURLReduceThread(ShuffleURLInputReduceReader reader, Context context) {
		job = new ShuffleURLJob();
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
				String key = line.split("\\t")[0];
				String value = line.split("\\t")[1];
				String[] valueArray = value.split(",");

				job.reduce(key, valueArray, context);

				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println("Error reading from reduce input");
		}
	}

}
