package mapreduce;

public class InvertedIndexJob implements Job{

	@Override
	public void map(String key, String value, Context context) {
		context.write(key, value);
	}

	@Override
	public void reduce(String key, String[] values, Context context) {
		String val = "";
		for(int i = 0; i < values.length; i++){
			if(i == 0)
				val = values[i];
			else
				val += " " + values[i];
		}
		context.write(key, val);
		
	}

}
