package mapreduce.InvertedIndexMaster;


/*import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;*/

import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.URLMetrics;
import indexer.storage.WordCountDBWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

//import crawler.storage.URLFrontierDBWrapper;

//import mapreduce.MyHttpClient;

public class InvertedIndexMasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	static InvertedIndexDBWrapper indexDB;
	static WordCountDBWrapper numWordsDB;
	/*private static Map<String, ArrayList<String>> statusMap; 
	private static String numMapThreads = "20";
	private static String numReduceThreads = "20";*/

	public void init(ServletConfig config) throws ServletException {
		//statusMap = new HashMap<String, ArrayList<String>>();
		System.out.println("Master init");
		indexDB = InvertedIndexDBWrapper.getInstance("/home/cis455/Index/indexdb");
		numWordsDB = WordCountDBWrapper.getInstance("/home/cis455/Index/numwordsdb");



	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{

		

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		if(request.getRequestURI().contains("/pushdata")){
			System.out.println("Data received");
			addData(request);
			PrintWriter writer = response.getWriter();
			writer.close();
		}
		else if(request.getRequestURI().contains("/close")){
			System.out.println("Closing db");
			indexDB.close();
			numWordsDB.close();
		}
	}


	/**
	 * Append data from POST into index DB
	 * @param request from POST
	 * @throws IOException
	 */
	public synchronized void addData(HttpServletRequest request) throws IOException{
		BufferedReader in = request.getReader();
		//InvertedIndexDBWrapper indexDB = InvertedIndexDBWrapper.getInstance("/home/cis455/Index/indexdb");
		WordCountDBWrapper numWordsDB = WordCountDBWrapper.getInstance("/home/cis455/Index/numwordsdb");


		String line = in.readLine();
		if(line == null)
			return;

		String[] firstLine = line.split("\\s+");
		int num = Integer.parseInt(firstLine[1]);
		String url = firstLine[0];

		System.out.println("url adding:" + firstLine[0] + " " + num);
		numWordsDB.addWord(firstLine[0], num);
		System.out.println("Size of num words" + numWordsDB.getSize());

		//numWordsDB.close();

		line = in.readLine();
		while(line != null){
			try{

				String[] docData = line.split("\\s+");
				System.out.println("docData: " + line);
				HashMap<String, URLMetrics> urlMap= indexDB.getUrls(docData[0]); //look up by word
				if(urlMap == null){
					urlMap = new HashMap<String, URLMetrics>();
				}
				urlMap.put(url, new URLMetrics(Integer.parseInt(docData[1]),0,0));

				indexDB.addWord(docData[0], urlMap);
			}catch(Exception e){
				//Caught exception
				System.out.println("<---Caught Exception--->: " + e);
			}
				line = in.readLine();

			
		}
		System.out.println("Index DB Size: " + indexDB.getSize());
		System.out.println();
		//indexDB.close();
	}

}

