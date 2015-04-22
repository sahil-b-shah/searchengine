package mapreduce.ShuffleURLWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ShuffleURLInputReduceReader {

	File input;
	private BufferedReader in;
	private String line;

	public ShuffleURLInputReduceReader(File input) throws IOException{
		this.input = input;
		if(input.exists()){
			this.in = new BufferedReader(new FileReader(input));
			line = in.readLine();
		}
		else{
			line = null;
		}

	}

	/**
	 * Gets next line
	 * @return line read, or null if done
	 * @throws IOException
	 */
	public synchronized String readLine() throws IOException{

		if(line != null){
			line = in.readLine();
		}

		return line;
	}

}
