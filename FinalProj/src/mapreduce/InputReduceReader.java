package mapreduce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class InputReduceReader {

	File input;
	private BufferedReader in;
	private int keysRead;
	private String line;

	public InputReduceReader(File input) throws IOException{
		this.input = input;
		if(input.exists()){
			this.in = new BufferedReader(new FileReader(input));
			line = in.readLine();
		}
		else{
			line = null;
		}
		this.keysRead = 0;

	}

	/**
	 * Gets next line
	 * @return line read, or null if done
	 * @throws IOException
	 */
	public synchronized String readLine() throws IOException{

		String tempLine  = null;
		String nextLine = null;
		if(line != null){
			String key = line.split("\\t")[0];
			String value = line.split("\\t")[1];
			tempLine = key + "\t" + value;

			nextLine = in.readLine();
			while(nextLine != null){
				String nextLineKey = nextLine.split("\\t")[0];
				String nextLineValue = nextLine.split("\\t")[1];

				if(nextLineKey.equals(key)){
					tempLine += "," + nextLineValue;
					nextLine = in.readLine();
				}
				else{
					break;
				}


			}


			keysRead++;
		}

		line = nextLine;
		return tempLine;

	}

	/**
	 * Gets current number of keys read
	 * @return keys read
	 */
	public String getKeysRead(){
		return keysRead + "";
	}
}
