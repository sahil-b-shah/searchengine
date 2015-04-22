package mapreduce.InvertedIndexWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InvertedIndexInputMapReader {

	File files[];
	private int fileIndex;
	private BufferedReader in;
	private int keysRead;
	private boolean done;
	
	
	public InvertedIndexInputMapReader(File fileList[]) throws FileNotFoundException{
		files = fileList;
		fileIndex = 0;
		done = false;
		if(files.length > 0)
			in = new BufferedReader(new FileReader(files[0]));
		else
			done = true;
		this.keysRead = 0;
	}
	
	/**
	 * Gets next line
	 * @return line read, or null if done
	 * @throws IOException
	 */
	public synchronized String readLine() throws IOException{
		
		if(done){
			return null;
		}
		
		String line = in.readLine();
		
		while(line == null){
			fileIndex++; //go to next file
			in.close();  //close previous stream
			if(fileIndex >= files.length){
				done = true;
				return null;
			}
			else{
				in = new BufferedReader(new FileReader(files[fileIndex]));
				line = in.readLine();  //get first line here
				keysRead++;
			}
		}
		
		return line;
	}
	
	/**
	 * Gets current number of keys read
	 * @return keys read
	 */
	public String getKeysRead(){
		return keysRead + "";
	}
	
}
