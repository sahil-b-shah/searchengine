package mapreduce;

public class InvertedIndexJob implements Job{

	@Override
	public void map(String key, String value, Context context) {
		context.write(key, value);
	}

	@Override
	public void reduce(String key, String[] values, Context context) {
		// TODO Auto-generated method stub
		
	}

}
