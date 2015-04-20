package mapreduce.ShuffleURL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import mapreduce.Context;



public class ShuffleURLReduceContext implements Context {

	private File output;
	
	public ShuffleURLReduceContext(File output) {
		this.output = output;	
	}

	public synchronized void write(String key, String value) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output,true)));
			out.println(key + "\t"+value);
			out.close();
		} catch (IOException e) {
			System.err.println("Error in emitting reduce");
			e.printStackTrace();
		}

	}
	
}