package mapreduce.ShuffleURLWorker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import mapreduce.Context;

import org.apache.commons.codec.digest.DigestUtils;


public class ShuffleURLMapContext implements Context{
	
	private File workerFiles[];
	private int numWorkers;
	private File spoolout;

	public ShuffleURLMapContext(File spoolout, String workers[]) throws IOException{
		this.spoolout = spoolout;
		workerFiles = new File[workers.length];
		for(int i = 1; i <= workers.length; i++){
			String currentWorker = "worker" + i +".txt";
			File tempFile = new File(spoolout, currentWorker);
			tempFile.createNewFile();
			workerFiles[i-1] = tempFile;
		}
		numWorkers = workerFiles.length;
	}

	@Override
	public synchronized void write(String key, String value) {
		
		URL url;
		try {
			url = new URL(key);
		} catch (MalformedURLException e1) {
			System.out.println("Malformed url to write");
			return;
		}
		
		//Hash by hostname
		String host = url.getHost();
		
		//Hash key using SHA-1
		String hashedValue = DigestUtils.sha1Hex(host);
		
		int fileNumber = pickNumberBucket(numWorkers, hashedValue);
		
		//Pick file based on hash
		File selectedFile = new File(spoolout, "worker" +fileNumber +".txt");
		
		
		
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(selectedFile,true)));
			out.println(key);
			out.close();
			
		} catch (IOException e) {
			System.err.println("Error in emitting to worker files in map");
		}
		
	}
	
	/**
	 * Picks which bucket hash value goes in
	 * @param numWorkers - number of ranges to split
	 * @param hashedValue - value to place
	 * @return
	 */
	private int pickNumberBucket(int numWorkers, String hashedValue) {
		String maxValue = "";
		for(int i = 0; i < 40; i++){
			maxValue += "f";
		}
		BigInteger hash = new BigInteger(hashedValue, 16);
		BigInteger bigMax = new BigInteger(maxValue, 16).add(BigInteger.ONE);
		
		BigInteger rangeSize = bigMax.divide(BigInteger.valueOf(numWorkers));
		
		int bucket = hash.divide(rangeSize).intValue() + 1;
		return bucket;
	}

	/**
	 * Gets worker file list
	 * @return worker files
	 */
	public File[] getWorkerFiles(){
		return workerFiles;
	}
	
	
}