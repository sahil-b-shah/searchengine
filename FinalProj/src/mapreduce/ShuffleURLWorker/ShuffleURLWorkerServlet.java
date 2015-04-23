package mapreduce.ShuffleURLWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.HashMap;

import javax.servlet.*;
import javax.servlet.http.*;

import crawler.storage.URLFrontierDBWrapper;
import mapreduce.MyHttpClient;



public class ShuffleURLWorkerServlet extends HttpServlet {

	private static final long serialVersionUID = 455555002;
	private static ShuffleURLInputMapReader reader;
	private static ShuffleURLMapContext mapContext;
	private static String status;
	private static String storageDirectory;
	private static String port;
	private static String job;
	private static Thread statusThread;
	private static String frontierDirectory;
	private static String unseenLinksDirectory;

	public void init(ServletConfig config) throws ServletException {
		String master = config.getInitParameter("master");
		port = config.getInitParameter("port");
		status = "idle";
		job = "none";
		frontierDirectory = config.getInitParameter("frontierDirectory");
		unseenLinksDirectory = config.getInitParameter("unseenLinksDirectory");


		mapContext = null;
		reader = null;
		storageDirectory = config.getInitParameter("storagedir");

		System.out.println("Worker init");

		//Create thread that issues GET every 10 seconds
		ShuffleURLStatusThread statusObj = new ShuffleURLStatusThread(master, this);
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
	
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		String IPPort = InetAddress.getLocalHost() + ":" + port;

		if(request.getRequestURI().contains("/runmap")){
			status = "mapping";
			//String input = request.getParameter("input");
			int numThreads = Integer.parseInt(request.getParameter("numThreads"));
			int numWorkers = Integer.parseInt(request.getParameter("numWorkers"));

			String workers[] = new String[numWorkers];

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
			spoolin.mkdirs();

			if(spoolout.exists() && spoolout.isDirectory()){
				//Delete directory and file
				File files[] = spoolout.listFiles();
				for(File file: files){
					file.delete();
				}
				spoolout.delete();
			}
			spoolout.mkdirs();

			//Get files in input directory
			//File inputDirectory = new File(new File(storageDirectory), input);
			//File fileList[] = inputDirectory.listFiles();


			//Create reader from input db
			reader = new ShuffleURLInputMapReader(unseenLinksDirectory);

			//Create emit from Map function implementing context
			mapContext = new ShuffleURLMapContext(spoolout, workers);


			System.out.println(IPPort + ": starting threads mapping"); 

			//Create numThread threads to run map
			Thread threads[] = new Thread[numThreads];
			for(int i = 0; i < numThreads; i++){
				ShuffleURLMapThread workerObj = new ShuffleURLMapThread(reader, mapContext);
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
			File workerFiles[] = mapContext.getWorkerFiles();

			//once all keys read, send a POST to /pushdata for appropriate workers	
			for(File curWorker: workerFiles){
				String address = request.getParameter(curWorker.getName());
				System.out.println("Cur worker: "+curWorker.getName());
				MyHttpClient client = new MyHttpClient(address, "/ShuffleURLWorker/pushdata");
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

			reader= null;
			mapContext  =null;

			//Issue /workerstatus
			MyHttpClient client = new MyHttpClient(IPPort, "/ShuffleURLMaster/workerstatus");
			if(client.connected()){

				HashMap<String, String> params = getStatusParameters();
				client.addParams("port", params.get("port"));
				client.addParams("status", params.get("status"));
				client.addParams("job", params.get("job"));
				client.sendPost();

			}

		}
		else if(request.getRequestURI().contains("/runreduce")){
			/*
			status = "reducing";
			int numThreads = Integer.parseInt(request.getParameter("numThreads"));
			String output = request.getParameter("output");

			//Sort file
			File spoolin = new File(storageDirectory,"spool-in");
			File storeFile = new File(spoolin, "store.txt");

			ShuffleURLInputReduceReader reader = new ShuffleURLInputReduceReader(storeFile);

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

			//ShuffleURLReduceContext reduceContext = new ShuffleURLReduceContext(outputFile);

			//Create numThread threads to run map
			Thread threads[] = new Thread[numThreads];
			for(int i = 0; i < numThreads; i++){
				ShuffleURLReduceThread workerObj = new ShuffleURLReduceThread(reader, null);
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

			*/
			System.out.println(IPPort + ": threads done reducing"); 

			status = "idle";


			//Issue workerstatus
			MyHttpClient client = new MyHttpClient(IPPort, "/master/workerstatus");
			if(client.connected()){

				HashMap<String, String> params = getStatusParameters();
				client.addParams("port", params.get("port"));
				client.addParams("status", params.get("status"));
				client.sendPost();
			}
		}
		else if(request.getRequestURI().contains("/pushdata")){
			addData(request);
		}

	}

	public HashMap<String, String> getStatusParameters(){

		HashMap<String, String> statusMap = new HashMap<String, String>();

		statusMap.put("status", status);
		statusMap.put("port", port);
		statusMap.put("job", job);

		return statusMap;

	}

	/**
	 * Append data from POST into frontier DB
	 * @param request from POST
	 * @throws IOException
	 */
	public synchronized void addData(HttpServletRequest request) throws IOException{
		BufferedReader in = request.getReader();
		URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);

		String line = in.readLine();
		while(line != null){
			frontierDB.addUrl(line);
			line = in.readLine();
		}
		frontierDB.close();
	}

	public void destroy(){
		statusThread.interrupt();
	}
}

