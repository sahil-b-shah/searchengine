package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.master.MyHttpClient;


public class WorkerServlet extends HttpServlet {

	private static final long serialVersionUID = 455555002;
	private static InputMapReader reader;
	private static EmitMap emit;
	private static String status;
	private static String lastMapKeysRead;
	private static String lastMapKeysWritten;
	private static String lastReduceKeysWritten;
	private static String storageDirectory;
	private static String port;
	private static String job;
	private static Thread statusThread;

	public void init(ServletConfig config) throws ServletException {
		String master = config.getInitParameter("master");
		port = config.getInitParameter("port");
		status = "idle";
		job = "none";


		lastMapKeysRead = "0";
		lastMapKeysWritten = "0";
		emit = null;
		reader = null;
		storageDirectory = config.getInitParameter("storagedir");

		System.out.println("Worker init");

		//Create thread that issues GET every 10 seconds
		StatusThread statusObj = new StatusThread(master, this);
		statusThread = new Thread(statusObj);
		statusThread.start();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Worker</title></head>");
		out.println("<body>Hi, I am the worker!</body></html>");
		out.flush();
	}

	@SuppressWarnings("rawtypes")
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		String IPPort = InetAddress.getLocalHost() + ":" + port;

		if(request.getRequestURI().contains("/runmap")){
			status = "mapping";
			String input = request.getParameter("input");
			job = request.getParameter("job");
			int numThreads = Integer.parseInt(request.getParameter("numThreads"));
			int numWorkers = Integer.parseInt(request.getParameter("numWorkers"));

			String workers[] = new String[numWorkers];


			Class jobClass = null;
			try {
				jobClass = Class.forName(job);
			} catch (ClassNotFoundException e) {
				System.err.println("Invalid job, not a java class");
				e.printStackTrace();

			}

			File spoolin = new File(storageDirectory,"spool-in");
			File spoolout = new File(storageDirectory, "spool-out");

			if(spoolin.exists() && spoolin.isDirectory()){
				//Delete directory and file
				File files[] = spoolin.listFiles();
				for(File file: files){
					file.delete();
				}
				spoolin.delete();
			}
			spoolin.mkdir();

			if(spoolout.exists() && spoolout.isDirectory()){
				//Delete directory and file
				File files[] = spoolout.listFiles();
				for(File file: files){
					file.delete();
				}
				spoolout.delete();
			}
			spoolout.mkdir();

			//Get files in input directory
			File inputDirectory = new File(new File(storageDirectory), input);
			File fileList[] = inputDirectory.listFiles();


			//Create reader from input directory files
			reader = new InputMapReader(fileList);

			//Create emit from Map function implementing context
			emit = new EmitMap(spoolout, workers);


			System.out.println(IPPort + ": starting threads mapping"); 

			//Create numThread threads to run map
			Thread threads[] = new Thread[numThreads];
			for(int i = 0; i < numThreads; i++){
				WorkerMapThread workerObj = new WorkerMapThread(jobClass, reader, emit);
				threads[i] = new Thread(workerObj);
				threads[i].start();
			}



			//Wait until all threads done
			for(int i = 0; i < numThreads; i++){
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					System.err.println("Map thread ended unnaturally");
				}
			}

			System.out.println(IPPort + ": threads done mapping"); 

			//Get files from emit
			File workerFiles[] = emit.getWorkerFiles();

			//once all keys read, send a POST to /pushdata for appropriate workers	
			for(File curWorker: workerFiles){
				String address = request.getParameter(curWorker.getName());
				MyHttpClient client = new MyHttpClient(address, "/worker/pushdata");
				BufferedReader in = new BufferedReader(new FileReader(curWorker));
				String body= "";
				String line = in.readLine();
				while(line != null){
					body += line + "\n";
					line = in.readLine();
				}
				in.close();
				client.setBody(body);
				client.sendPost();
			}

			status = "waiting";

			lastMapKeysRead = reader.getKeysRead();
			lastMapKeysWritten = emit.getKeysWritten();
			reader= null;
			emit  =null;

			//Issue /workerstatus
			MyHttpClient client = new MyHttpClient(IPPort, "/master/workerstatus");
			if(client.connected()){

				HashMap<String, String> params = getStatusParameters();
				client.addParams("port", params.get("port"));
				client.addParams("status", params.get("status"));
				client.addParams("job", params.get("job"));
				client.addParams("keysRead", params.get("keysRead"));
				client.addParams("keysWritten", params.get("keysWritten"));

				client.sendPost();

			}

		}
		else if(request.getRequestURI().contains("/runreduce")){
			status = "reducing";
			int numThreads = Integer.parseInt(request.getParameter("numThreads"));
			String output = request.getParameter("output");
			Class jobClass = null;
			try {
				jobClass = Class.forName(job);
			} catch (ClassNotFoundException e) {
				System.err.println("Invalid job, not a java class");
			}


			//Sort file
			File spoolin = new File(storageDirectory,"spool-in");
			File storeFile = new File(spoolin, "store.txt");

			if(storeFile.exists()){
				System.out.println(IPPort + ": starting sort command"); 
				Process proc = Runtime.getRuntime().exec("sort store.txt", null, spoolin);
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(storeFile,false)));
				String line = in.readLine();
				while(line != null){
					out.println(line);
					line = in.readLine();
				}

				in.close();
				out.close();

			}

			InputReduceReader reader = new InputReduceReader(storeFile);

			File outputDir = new File(storageDirectory, output);

			//Delete directory if there
			if(outputDir.exists() && outputDir.isDirectory()){
				//Delete directory and file
				File files[] = outputDir.listFiles();
				for(File file: files){
					file.delete();
				}
				outputDir.delete();
			}
			outputDir.mkdir();

			File outputFile = new File(outputDir, "output.txt");

			EmitReduce emit = new EmitReduce(outputFile);

			//Create numThread threads to run map
			Thread threads[] = new Thread[numThreads];
			for(int i = 0; i < numThreads; i++){
				WorkerReduceThread workerObj = new WorkerReduceThread(jobClass, reader, emit);
				threads[i] = new Thread(workerObj);
				threads[i].start();
			}

			//Wait until all threads done
			for(int i = 0; i < numThreads; i++){
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					System.err.println("Map thread ended unnaturally");
				}
			}

			System.out.println(IPPort + ": threads done reducing"); 

			status = "idle";
			lastReduceKeysWritten = emit.getKeysWritten();


			//Issue workerstatus
			MyHttpClient client = new MyHttpClient(IPPort, "/master/workerstatus");
			if(client.connected()){

				HashMap<String, String> params = getStatusParameters();
				client.addParams("port", params.get("port"));
				client.addParams("status", params.get("status"));
				client.addParams("job", params.get("job"));
				client.addParams("keysRead", params.get("keysRead"));
				client.addParams("keysWritten", params.get("keysWritten"));

				client.sendPost();
			}
		}
		else if(request.getRequestURI().contains("/pushdata")){
			addData(request);
		}

	}

	public HashMap<String, String> getStatusParameters(){

		HashMap<String, String> statusMap = new HashMap<String, String>();

		if(status != null){
			if(reader != null){
				String keysRead = reader.getKeysRead();
				if(status.equals("mapping") || status.equals("reducing")){
					statusMap.put("keysRead", keysRead);
				}
			}
			if(status.equals("waiting")){
				statusMap.put("keysRead", lastMapKeysRead);
			}
			else{
				statusMap.put("keysRead", "0");
			}
		}
		else{
			statusMap.put("keysRead", "0");
		}

		if(status != null){
			if(emit != null){
				String keysWritten = emit.getKeysWritten();
				if(status.equals("mapping") || status.equals("reducing")){
					statusMap.put("keysWritten", keysWritten);
				}
			}
			if(status.equals("waiting")){
				statusMap.put("keysWritten", lastMapKeysWritten);
			}
			else if(status.equals("idle")){
				statusMap.put("keysWritten", lastReduceKeysWritten);
			}
			else{
				statusMap.put("keysWritten", "0");
			}

		}
		else{
			statusMap.put("keysWritten", "0");
		}

		statusMap.put("status", status);
		statusMap.put("port", port);
		statusMap.put("job", job);

		return statusMap;

	}

	/**
	 * Append data from POST into spool-in
	 * @param request from POST
	 * @throws IOException
	 */
	public synchronized void addData(HttpServletRequest request) throws IOException{
		BufferedReader in = request.getReader();
		File spoolin = new File(storageDirectory, "spool-in");

		if(!spoolin.exists()){
			spoolin.mkdir();
		}

		File storeFile = new File(spoolin, "store.txt");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(storeFile,true)));

		String line = in.readLine();
		while(line != null){
			out.println(line);
			line = in.readLine();
		}
		out.close();
	}

	public void destroy(){
		statusThread.interrupt();
	}
}

