package mapreduce.InvertedIndexWorker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import mapreduce.Context;


public class InvertedIndexReduceContext implements Context {

	private File output;
	private int keysWritten;
	
	public InvertedIndexReduceContext(File output) {
		this.output = output;	
		keysWritten = 0;
	}

	public synchronized void write(String key, String value) {
		try {
			keysWritten++;
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output,true)));
			out.println(key + "\t"+value);
			out.close();
		} catch (IOException e) {
			System.err.println("Error in emitting reduce");
			e.printStackTrace();
		}

	}
	
	/**
	 * Gets current number of keys written
	 * @return number of keys written
	 */
	public String getKeysWritten(){
		return keysWritten + "";
	}
}
