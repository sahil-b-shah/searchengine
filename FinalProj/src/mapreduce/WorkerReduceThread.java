package mapreduce;

import java.io.IOException;

public class WorkerReduceThread implements Runnable {

	private Job job;
	private InputReduceReader reader;
	private Context context;
	
	@SuppressWarnings("rawtypes")
	public WorkerReduceThread(Class jobClass, InputReduceReader reader, Context context) {
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
