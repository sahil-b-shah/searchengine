package mapreduce;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ReduceContext implements Context {

	private static String directory;

	public ReduceContext(String d){
		directory =d;
		
		File wfile = new File(directory);
		if (wfile.exists()){
			System.out.println("FILE ALREADY HERE -- DELETING IT");
			wfile.delete();
		}
		try {
			wfile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void write(String key, String value) {

		//System.out.println("FIlanem: " + filename);
		File wfile = new File(directory);

		PrintWriter out = null;

		try {

			
			//System.out.println("Created a new file");
			FileWriter f = new FileWriter(wfile, true);
			out = new PrintWriter(new BufferedWriter(f));
			out.println(key + "\t" + value);

		}catch (IOException e) {
			System.out.println("Error  whie trying write to file in CONTEXT");
			e.printStackTrace();
		}finally{
			out.close();
		}

	}



}
