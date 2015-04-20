package mapreduce;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class ShuffleURLMasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	private static Map<String, ArrayList<String>> statusMap; 
	private static Map<String, ArrayList<String>> jobMap; 

	public void init(ServletConfig config) throws ServletException {
		statusMap = new HashMap<String, ArrayList<String>>();
		jobMap = new HashMap<String, ArrayList<String>>();
		System.out.println("Master init");

	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{

		//Status page
		if(request.getRequestURI().contains("/status")){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><head><title>Master</title></head><body>");
			out.println("<h1>Name: Daniel Salowe</h1>");
			out.println("<h1>Pennkey: salowed</h1>");
			out.println("<table border='1' style='width:100%'>");
			out.println("<tr>");
			out.println("<td>IP:port</td>");
			out.println("<td>Status</td>");
			out.println("<td>Job</td>");
			out.println("<td>Keys Read</td>");
			out.println("<td>Keys Written</td>");
			out.println("</tr>");

			Map<String, ArrayList<String>> localStatusMap =  getStatusMap();

			//Add full rows with stored data
			for(String key: localStatusMap.keySet()){
				ArrayList<String> params = localStatusMap.get(key);
				//Check if in last 30 seconds
				if((System.currentTimeMillis() - Long.parseLong(params.get(4))) < 30000){
					out.println("<tr>");
					out.println("<td>"+ key+ "</td>");
					for(int i = 0; i < 4; i++){
						out.println("<td>"+ params.get(i)+ "</td>");
					}
					out.println("</tr>");
				}

			}


			out.println("</table>");


			//Post to itself first
			out.println("<form style='text-align:center;'  method='post'>");
			out.println("<p align='center'>Class Name of Job: </p><input type='text' name='job'></br>");
			out.println("<p align='center'>Input directory: </p><input type='text' name='input'></br>");
			out.println("<p align='center'>Output directory: </p><input type='text' name='output'></br>");
			out.println("<p align='center'>Number of map threads: </p><input type='text' name='numMapThreads'></br>");
			out.println("<p align='center'>Number of reduce threads: </p><input type='text' name='numReduceThreads'></br>");
			out.println("<input type='submit' value='Start Job'></form>");
			out.println("</body></html>");


			out.flush();


		}




	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		//Status update url
		if(request.getRequestURI().contains("/workerstatus")){
			//Get query string params
			String status = request.getParameter("status");
			String job = request.getParameter("job");
			String keysRead = request.getParameter("keysRead");
			String keysWritten = request.getParameter("keysWritten");
			String port = request.getParameter("port");
			String IPPort = request.getLocalAddr() + ":" + port;
			String timeLastReceived = System.currentTimeMillis() + "";			

			Map<String, ArrayList<String>> localStatusMap =  getStatusMap();
			ArrayList<String> params = new ArrayList<String>();
			params.add(status);
			params.add(job);
			params.add(keysRead);
			params.add(keysWritten);
			params.add(timeLastReceived);
			localStatusMap.put(IPPort, params);

			updateStatusMap(IPPort, params);


			//Send post request to /runreduce for each active cleint

			boolean allWaiting = true;
			for(String key: localStatusMap.keySet()){
				if(!localStatusMap.get(key).get(0).equals("waiting")){
					allWaiting = false;
				}
			}

			if(allWaiting){
				System.out.println("All workers waiting");
				Map<String, ArrayList<String>> localJobsMap =  getJobMap();
				for(String iport: localStatusMap.keySet()){
					if((System.currentTimeMillis() - Long.parseLong(params.get(4))) < 30000){
						MyHttpClient client = new MyHttpClient(iport, "/worker/runreduce");
						String jobValue = localStatusMap.get(iport).get(1);
						client.addParams("job", jobValue);
						String outputValue = localJobsMap.get(job).get(1);
						client.addParams("output", outputValue);
						String threadValue = localJobsMap.get(job).get(3);
						client.addParams("numThreads", threadValue);						
						client.sendPost();
					}
				}
			}
		}
		else{
			String job = request.getParameter("job");
			String input = request.getParameter("input");
			String output = request.getParameter("output");
			String numMapThreads = request.getParameter("numMapThreads");
			String numReduceThreads = request.getParameter("numReduceThreads");


			Map<String, ArrayList<String>> localJobMap =  getJobMap();
			ArrayList<String> params = new ArrayList<String>();
			params.add(input);
			params.add(output);
			params.add(numMapThreads);
			params.add(numReduceThreads);
			localJobMap.put(job, params);

			updateJobMap(job, params);

			Map<String, ArrayList<String>> localStatusMap =  getStatusMap();

			//Post to /runmap on every active worker
			for(String worker: localStatusMap.keySet()){
				MyHttpClient client = new MyHttpClient(worker, "/worker/runmap");
				client.addParams("job", job);
				client.addParams("input", input);
				client.addParams("numThreads", numMapThreads);
				client.addParams("numWorkers", localStatusMap.size() + "");
				int counter = 1;
				for(String workerID: localStatusMap.keySet()){
					client.addParams("worker"+counter, workerID);
					counter++;
				}
				//System.out.println("Posting /runmap to "+ worker);
				client.sendPost();
			}
		}

	}


	/**
	 * Gets the current status map synchronously and copies it to a new one
	 * @return a copy of the map
	 */
	private Map<String, ArrayList<String>> getStatusMap(){
		Map<String, ArrayList<String>> copyMap =  new HashMap<String, ArrayList<String>>();
		synchronized(statusMap){
			for(String key: statusMap.keySet()){
				copyMap.put(key, statusMap.get(key));
			}
		}
		return copyMap;
	}

	/**
	 * Updates the status map synchronously
	 * @param key to add
	 * @param value to add 
	 */
	private void updateStatusMap(String key, ArrayList<String> value){
		synchronized(statusMap){
			statusMap.put(key, value);
		}
	}

	/**
	 * Gets the current job map synchronously and copies it to a new one
	 * @return a copy of the map
	 */
	private Map<String, ArrayList<String>> getJobMap(){
		Map<String, ArrayList<String>> copyMap =  new HashMap<String, ArrayList<String>>();
		synchronized(jobMap){
			for(String key: jobMap.keySet()){
				copyMap.put(key, jobMap.get(key));
			}
		}
		return copyMap;
	}

	/**
	 * Updates the job map synchronously
	 * @param key to add
	 * @param value to add 
	 */
	private void updateJobMap(String key, ArrayList<String> value){
		synchronized(jobMap){
			System.out.println("Adding " + key + "to job map");
			jobMap.put(key, value);
		}
	}



}
