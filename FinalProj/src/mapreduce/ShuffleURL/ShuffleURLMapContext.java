package mapreduce.ShuffleURL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.*;

import mapreduce.Context;


public class ShuffleURLMapContext implements Context {

	private static int numworkers;
	private static String directory;

	public ShuffleURLMapContext(int i, String d, int kw){
		numworkers = i;
		directory =d;
	}

	@Override
	public void write(String key, String value) {
	
		String sha = hash(key);
		BigInteger bigsha = new BigInteger(sha, 16);
		BigInteger workers = new BigInteger(String.valueOf(numworkers));

		BigInteger base = new BigInteger("2");
		
		BigInteger bucket = base.pow(160).divide(workers);
		BigInteger one = new BigInteger("1");
		
		BigInteger workerfile = bigsha.divide(bucket);
		

		//System.out.println("Bucket to go into: " + workerfile);
		
		String filename = directory + "spool-out/" + workerfile.add(one) + ".txt";

		//System.out.println("FIlanem: " + filename);
		File wfile = new File(filename);

		PrintWriter out = null;

		try {
			if (wfile.exists()){
				out = new PrintWriter(new FileOutputStream(new File(filename), true));
				out.append(key + "\t" + value + "\n");
				//WorkerServlet.keyswritten++;
			}else{
				wfile.createNewFile();
				System.out.println("Created a new file");
				FileWriter f = new FileWriter(filename, true);
				out = new PrintWriter(new BufferedWriter(f));
				out.println(key + "\t" + value);
				//WorkerServlet.keyswritten++;
				}
		}catch (IOException e) {
			System.out.println("Error  whie trying write to file in CONTEXT");
			e.printStackTrace();
		}finally{
			out.close();
		}

	}

	private static String hash(String key){
		String sha1 = "";
		try{
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(key.getBytes("UTF-8"));
			sha1 = byteArrayToHexString(crypt.digest());
		}
		catch(NoSuchAlgorithmException | UnsupportedEncodingException e1){
			e1.printStackTrace();
		}
		return sha1;
	}

	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i=0; i < b.length; i++) {
			result +=
					Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}


}
