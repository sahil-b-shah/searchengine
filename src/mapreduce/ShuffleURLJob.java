package mapreduce;


public class ShuffleURLJob implements Job {

	@Override
	public void map(String key, String value, Context context) {
		context.write(key, "");

	}

	@Override
	public void reduce(String key, String[] values, Context context) {
		//context.write(key, "");
	}

}
