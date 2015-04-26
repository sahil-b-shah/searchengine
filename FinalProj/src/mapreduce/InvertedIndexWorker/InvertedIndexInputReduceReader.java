package mapreduce.InvertedIndexWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class InvertedIndexInputReduceReader {

	File input;
	private BufferedReader in;
	private String line;

	public InvertedIndexInputReduceReader(File input) throws IOException{
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

		String tempLine  = null;
		String nextLine = null;
		if(line != null){
			System.out.println("Line in reduce reader " + line);
			int count = 1;
			String word = line.split("\\t")[0];
			String value = line.split("\\t")[1];

			//tempLine = word + "\t";

			nextLine = in.readLine();
			while(nextLine != null){
				String nextLineKey = nextLine.split("\\t")[0];
				String nextLineValue = nextLine.split("\\t")[1];

				if(nextLineKey.equals(word)){
					if(nextLineValue.equals(value)){
						count++;
					}
					else{
						if (tempLine == null){
							tempLine = word + "\t" + value+";"+count;
						}
						else{
							tempLine += " " + value + ";" + count;
						}
						count = 1;
						value = nextLine.split("\\t")[1];
					}
					nextLine = in.readLine();
					System.out.println("New next line: " + nextLine + "   " + tempLine);
					System.out.println(nextLineKey + nextLineValue);
				}
				else{
					System.out.println("In break " + nextLineKey + nextLineValue);
					break;
				}
			}
			tempLine += " " + value + ";" + count;
		}
		

		line = nextLine;
		System.out.println("Templine" + tempLine);
		return tempLine;

	}

}
