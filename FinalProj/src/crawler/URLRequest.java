package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import crawler.storage.RobotsDBWrapper;
import crawler.storage.RobotsTxtData;

public class URLRequest {

	static final String KEY_VALUE_REGEX = "(.+):\\s*([\\w/\\*\\.-]+);*.*";
	static final String HTTP_HEADER_REGEX = "(.+):\\s*([\\w/\\*\\.-]+)";

	private String urlString;
	private String protocol;
	private String hostName;
	private String filePath;
	private int port = -1;
	private String contentType;
	private int contentLength = -1;
	private Date lastModified;


	private int delay = 5;

	public int getPort() {
		return (port == -1) ? 80 : port;
	}

	public String getUrlString() {
		return urlString;
	}
	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return hostName;
	}

	public String getFilePath() {
		if (filePath == null) {
			return "";
		} else {
			return filePath;
		}
	}

	public String getContentType() {
		return contentType;
	}

	public int getContentLength() {
		return contentLength;
	}

	public Date getLastModified() {
		return (lastModified == null) ? new Date(System.currentTimeMillis()) : lastModified;
	}

	/*public RobotsTxtInfo getRobots() {
		return robotsTxt;
	}*/

	public URLRequest(String urlString) {
		if (urlString.startsWith("http")) {
			this.urlString = urlString;
		} else {
			this.urlString = "http://"+urlString;
		}
		//System.out.println("Request object being made for " + urlString);

		URL urlObj = null;
		try {
			urlObj = new URL(this.urlString);
		} catch (MalformedURLException e) {
			System.err.println("Malformed URL string");
			e.printStackTrace();
			//System.exit(-1);
		}
		hostName = urlObj.getHost();

		port = urlObj.getPort();
		protocol = urlObj.getProtocol();
		if (urlObj.getPath().isEmpty()) {
			filePath = "/";
		} else {
			filePath = urlObj.getPath();
		}
		//printProperties();

	}

	private void printProperties() {
		System.out.println(protocol);
		System.out.println(hostName);
		System.out.println(filePath);
		System.out.println(port);
	}

	/***
	 * Sends HEAD request with If-Modified-Since header
	 * @param date
	 * @return true if modified since last modified date, false otherwise 
	 * @throws IOException 
	 */
	public boolean checkModified(long date) throws IOException {

		//Set if modified since header
		Map<String, String> params = new HashMap<String, String>();
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		String dateString = format.format((new Date(date)));
		params.put("If-Modified-Since", dateString);

		if(this.protocol.equals("http")) {
			InputStream responseStream = sendRequest(hostName, filePath, "HEAD", params);
			if (responseStream == null) {
				return false;
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
			if (setResponseHeaders(br)){
				System.out.println(this.hostName+"---- Content length: "+this.contentLength);
				return true;
			}
		} else {
			HttpURLConnection con = sendHttpsRequest(hostName, filePath, "HEAD", params);

			if(con.getResponseCode() == 200) {
				this.contentLength = con.getContentLength();
				this.contentType = con.getContentType();
				return true;
			}
		}
		return false;
	}

	/***
	 * Sends HEAD request to host
	 * @return true if response code return success, false otherwise
	 * @throws IOException
	 */
	public boolean sendHead() throws IOException {
		//int responseCode = con.getResponseCode();
		if(this.protocol.equals("http")) {
			InputStream responseStream = sendRequest(hostName, filePath, "HEAD", null);
			if (responseStream == null) {
				return false;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
			if (setResponseHeaders(br)){
				System.out.println(this.hostName+"---- Content length: "+this.contentLength);
				return true;
			}
		} else {
			HttpURLConnection con = sendHttpsRequest(hostName, filePath, "HEAD", null);

			if(con.getResponseCode() == 200) {
				this.contentLength = con.getContentLength();
				this.contentType = con.getContentType();
				return true;
			}
		}
		return false;
	}

	/***
	 * Check the robots txt for host
	 * @return the robots txt data to be checked by the client
	 * @throws IOException
	 */
	public RobotsTxtData checkRobots() throws IOException {
		RobotsDBWrapper robotsDB = RobotsDBWrapper.getInstance("/home/cis455/storage");

		RobotsTxtData robotsTxtData = robotsDB.getRobotsTxtData(this.hostName);
		if (robotsTxtData == null) {
			constructRobotsTxt(this.hostName);
			robotsTxtData = robotsDB.getRobotsTxtData(this.hostName);
		}
		if (robotsTxtData == null) {
			this.delay = 5;
			return null;
		}

		this.delay = (int) robotsTxtData.getCrawlDelay();
		this.delay = (this.delay == 0) ? 5 : this.delay;
		return robotsTxtData;
	}


	private void constructRobotsTxt(String hostName) throws IOException {
		
		//Get reponse and read headers 
		BufferedReader br = null;
		RobotsTxtInfo robotsTxt = new RobotsTxtInfo();
		//Get reponse and read headers 
		if (this.protocol.equals("http")) {
			InputStream responseStream = sendRequest(this.hostName, "/robots.txt", "GET", null);
			if (responseStream==null) {
				return;
			}
			br = new BufferedReader(new InputStreamReader(responseStream));
			if (!setResponseHeaders(br)){
				//System.out.println(responseStream.available());
				return;
			}

			System.out.println(this.hostName+"---- Content length: "+this.contentLength);

		} else {
			HttpURLConnection con = sendHttpsRequest(hostName, "/robots.txt", null);
			
			if(con.getResponseCode() == 200) {
				this.contentLength = con.getContentLength();
				this.contentType = con.getContentType();
			}
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		}
		String string;
		String userAgent = "*";
		//System.out.println("Bitches");
		while ((string = br.readLine()) != null) {
			//System.out.println(string);
			if (string.contains("#")) {
				string = string.substring(0, string.indexOf("#"));
			}

			//Extract key-values of robot
			Pattern p = Pattern.compile(KEY_VALUE_REGEX);
			Matcher m = p.matcher(string);
			if (m.find()) {
				String key = m.group(1);
				key = key.trim();
				String value = m.group(2);
				//System.out.println("Robots --"+key+": "+value);
				value = value.trim();
				if (key.equalsIgnoreCase("User-Agent")) {
					userAgent = value;
					robotsTxt.addUserAgent(value);
				} else if (key.equalsIgnoreCase("Disallow")) {
					robotsTxt.addDisallowedLink(userAgent, value);
				} else if (key.equalsIgnoreCase("Allow")) {
					robotsTxt.addAllowedLink(userAgent, value);
				} else if (key.equalsIgnoreCase("Crawl-delay")) {
					robotsTxt.addCrawlDelay(userAgent, Integer.valueOf(value));
				}
			}
		}
		//System.out.println("bitches "+string);
		RobotsDBWrapper robotsDB = RobotsDBWrapper.getInstance("/home/cis455/storage");
		robotsDB.addRobotsTxt(this.hostName, robotsTxt.getAllowedLinks("cis455crawler"),
				robotsTxt.getDisallowedLinks("cis455crawler"), robotsTxt.getCrawlDelay("cis455crawler"));

	}

	private InputStream sendRequest(String hostname, String filepath) throws IOException {
		return sendRequest(hostname, filepath, "GET", null);
	}

	private InputStream sendRequest(String hostname, String filepath, String method,
			Map<String, String> params) throws IOException {

		System.out.println("Sending http "+method+" request to "+hostname + filepath);
		this.delay();
		Socket s = null;
		try {
			s = new Socket();
			s.connect(new InetSocketAddress(hostname, getPort()), 2000);
			//System.out.println("Consdfsa");
		} catch (UnknownHostException e) {
			System.err.println("Host could not be resolved");
			e.printStackTrace();
			return null;
			//System.exit(-1);
		} catch (IOException e) {
			System.err.println("Could not create stream socket");
			e.printStackTrace();
			return null;
			//System.exit(-1);
		}

		PrintWriter pw = new PrintWriter(s.getOutputStream());
		//System.out.println("Consdfsa");
		//PrintWriter pw = new PrintWriter(System.out);

		pw.println(method+" "+filepath+" HTTP/1.0");
		pw.println("Host: " + hostname);
		pw.println("User-Agent: cis455crawler");
		pw.println("Accept-Language: en-US");
		pw.println("Connection: close");


		//Add all headers in params to request
		if (params != null) {
			Set<Entry<String, String>> entrySet = params.entrySet();
			for (Entry<String, String> entry : entrySet) {
				pw.println(entry.getKey()+": "+entry.getValue());
			}
		}

		pw.print("\r\n");
		pw.flush();
		//System.out.println("Consdfsa");
		return s.getInputStream();
	}

	private boolean setResponseHeaders(BufferedReader br) throws IOException {
		//System.out.println("In response");
		String line;

		if (!br.ready()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!br.ready()) {
			System.out.println("Buffered reader not ready");
			return false;
		}
		line = br.readLine();
		//System.out.println("line read");
		if (line == null) {
			System.err.println("Empty header from" + this.hostName);
			return false;
		}
		String[] firstLine = line.split("\\s");
		//System.out.println("First line:" + firstLine);
		if ((Integer.valueOf(firstLine[1]) != 200)) {
			System.err.println("Response code not 200: "+firstLine[1]);
			return false;
		}
		Pattern p = Pattern.compile(HTTP_HEADER_REGEX);
		while ((line = br.readLine())!=null) {
			//System.out.println(line);
			if (line.isEmpty()) {
				//System.out.print("Empty line");
				break;
			}
			Matcher m = p.matcher(line);
			if (m.find()) {
				String key = m.group(1);
				key = key.trim();
				String value = m.group(2);
				value = value.trim();
				//System.out.println("Response header-- "+key+": "+value);
				if(key.equalsIgnoreCase("Content-Length")) {
					this.contentLength = Integer.valueOf(value);
				} else if(key.equals("Content-Type")) {
					this.contentType = value;
				}
			}
		}
		return true;
	}

	private HttpURLConnection sendHttpsRequest(String hostName, String filepath,
			Map<String, String> params) throws IOException {
		return sendHttpsRequest(hostName, filepath, "GET", params);
	}

	private HttpURLConnection sendHttpsRequest(String hostName, String filepath, String method, Map<String, String> params) 
			throws IOException {
		this.delay();
		if (!filepath.startsWith("/")) {
			filepath = "/"+filepath;
		}
		String requestString = this.protocol+"://"+hostName + filepath;
		System.out.println("Sending https "+method+" request to "+requestString);
		HttpURLConnection con = null;
		URL url = null;

		url = new URL(requestString);		
		con = (HttpsURLConnection) url.openConnection();

		con.setRequestMethod(method);
		con.setConnectTimeout(3000);
		con.addRequestProperty("Host", hostName);
		con.addRequestProperty("User-Agent", "cis455crawler");
		con.addRequestProperty("Accept-Language", "en-US");

		//Add additional params
		if (params != null) {
			Set<Entry<String, String>> entrySet = params.entrySet();
			for (Entry<String, String> entry : entrySet) {
				con.addRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		return con;
	}

	private Date checkModifiedDate(String date) {
		DateFormat df1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		DateFormat df2 = new SimpleDateFormat("EEEEE, dd-MMM-yy HH:mm:ss zzz");
		DateFormat df3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
		Date modDate = null;
		try {
			modDate = df1.parse(date);
			return modDate;
		} catch (ParseException e) {
			System.out.println("Date format 1 failed");
		}
		try {
			modDate = df2.parse(date);
			return modDate;
		} catch (ParseException e) {
			System.out.println("Date format 2 failed");
		}
		try {
			modDate = df3.parse(date);
			return modDate;
		} catch (ParseException e) {
			System.err.println("Date format 3 failed: none of the formats applied");
			e.printStackTrace();
			System.exit(-1);
		}
		return modDate;
	}

	private void delay() {
		//milli = 0; //for testing
		try {
			Thread.sleep(this.delay*1000);
		} catch (InterruptedException e) {
			System.err.println("Error sleeping thread");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return input stream to host or null if response code of response was not 200
	 * @throws IOException
	 */
	public BufferedReader sendGetRequest() throws IOException {

		//Get reponse and read headers 
		if(this.protocol.equals("http")) {
			InputStream responseStream = sendRequest(hostName, filePath,"GET", null);
			if (responseStream == null) {
				return null;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
			if (setResponseHeaders(br)){
				return br;
			}
		} else {
			HttpURLConnection con = sendHttpsRequest(hostName, this.filePath, null);
			if(con.getResponseCode() == 200) {
				this.contentLength = con.getContentLength();
				this.contentType = con.getContentType();
				return new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
		}

		System.err.println("Get request failed");
		return null;
	}
}

