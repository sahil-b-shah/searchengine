package mapreduce.PageRankMaster;


/*import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;*/

import pagerank.storage.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

//import crawler.storage.URLFrontierDBWrapper;

//import mapreduce.MyHttpClient;

public class PageRankMasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	/*private static Map<String, ArrayList<String>> statusMap; 
	private static String numMapThreads = "20";
	private static String numReduceThreads = "20";*/

	public void init(ServletConfig config) throws ServletException {
		System.out.println("Master init");

	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{


	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		if(request.getRequestURI().contains("/pushdata")){
			System.out.println("Page received");
			addData(request);
			PrintWriter writer = response.getWriter();
			writer.close();
		}
	}


	/**
	 * Append data from POST into index DB
	 * @param request from POST
	 * @throws IOException
	 */
	public synchronized void addData(HttpServletRequest request) throws IOException{
		BufferedReader in = request.getReader();
		PageRankDBWrapper indexDB = PageRankDBWrapper.getInstance("/home/cis455/PageRank/pagerankdb");

		String line = in.readLine();
		while(line != null){
			String[] docData = line.split("\\s+");
			System.out.println("url: " + docData[0]);
			HashMap<String, Integer> urlMap= indexDB.getUrls(docData[2]); //look up by word
			if(urlMap == null){
				urlMap = new HashMap<String, Integer>();
			}
			urlMap.put(docData[0], Integer.valueOf(docData[1]));
			System.out.println("Incoming: " + docData[0] + ", Outgoign Count: " + docData[1]);
			indexDB.addUrl(docData[2], urlMap);
			System.out.println("Storing for: " + docData[2]);
			line = in.readLine();
		}
		System.out.println("PR DB Size: " + indexDB.getSize());
		System.out.println();
		indexDB.close();
		
	}


}

