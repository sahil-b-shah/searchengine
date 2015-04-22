package crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import crawler.storage.RobotsDBWrapper;
import crawler.storage.RobotsTxtData;

public class URLRequest {
	
	static final String KEY_VALUE_REGEX = "(.+):\\s*([\\w/\\*\\.-]+);*.*";
	
	private String urlString;
	private String protocol;
	private String hostName;
	private String filePath;
	private int port;
	private String contentType;
	private int contentLength = -1;
	private Date lastModified;
	
	
	private long delay = 5000;
	
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
		this.urlString = urlString;
		System.out.println("Request object being made for " + urlString);
		
		URL urlObj = null;
		try {
			urlObj = new URL(urlString);
		} catch (MalformedURLException e) {
			System.err.println("Malformed URL string");
			e.printStackTrace();
			System.exit(-1);
		}
		hostName = urlObj.getHost();
		port = urlObj.getPort();
		protocol = urlObj.getProtocol();
		if (urlObj.getPath().isEmpty()) {
			filePath = "/";
		} else {
			filePath = urlObj.getPath();
		}
		
	}
	
	private void printProperties() {
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
		HttpURLConnection con = sendRequest(hostName, filePath, "HEAD");
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		String dateString = format.format((new Date(date)));
		
		con.addRequestProperty("If-Modified-Since", dateString);
		
		if (con.getResponseCode()==200){
			this.contentLength = con.getContentLength();
			this.contentType = con.getContentType();
			System.out.println("Content length: "+this.contentLength);
			return true;
		}
		return false;
	}
	
	/***
	 * Sends HEAD request to host
	 * @return true if response code return success, false otherwise
	 * @throws IOException
	 */
	public boolean sendHead() throws IOException {
		HttpURLConnection con = sendRequest(hostName, filePath, "HEAD");
				
		if (con.getResponseCode()==200){
			this.contentLength = con.getContentLength();
			this.contentType = con.getContentType();
			System.out.println("Content length: "+this.contentLength);
			return true;
		}
		return false;
	}
	
	/***
	 * 
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
		this.delay = robotsTxtData.getCrawlDelay();
		return robotsTxtData;
	}
	
	
	private void constructRobotsTxt(String hostName) throws IOException {
		HttpURLConnection con = sendRequest(hostName, "/robots.txt");
		RobotsTxtInfo robotsTxt = new RobotsTxtInfo();
		
		if(con.getResponseCode() != 200) {
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String string;
		String userAgent = "*";
		while ((string = br.readLine()) != null) {
			 
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
			RobotsDBWrapper robotsDB = RobotsDBWrapper.getInstance("/home/cis455/storage");
			robotsDB.addRobotsTxt(hostName, robotsTxt.getAllowedLinks("cis455crawler"),
					robotsTxt.getDisallowedLinks("cis455crawler"), robotsTxt.getCrawlDelay("cis455crawler"));
			
		}
	}
	
	private HttpURLConnection sendRequest(String hostName, String filepath) throws ProtocolException {
		return sendRequest(hostName, filepath, "GET");
	}
	
	private HttpURLConnection sendRequest(String hostName, String filepath, String method) throws ProtocolException {
		this.delay();
		if (!filepath.startsWith("/")) {
			filepath = "/"+filepath;
		}
		String requestString = this.protocol+"://"+hostName + filepath;
		System.out.println("Sending request to "+requestString);
		HttpURLConnection con = null;
		URL url = null;
		try {
			url = new URL(requestString);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		if (this.protocol.equals("http")) {
			//Connection
			try {
				con = (HttpURLConnection) url.openConnection();
			} catch (IOException e1) {
				System.err.println("Error opening connection to master servlet: http");
				e1.printStackTrace();
			}
		} else { 
			try {
				con = (HttpsURLConnection) url.openConnection();
			} catch (IOException e1) {
				System.err.println("Error opening connection to master servlet: http");
				e1.printStackTrace();
			}
		}
		con.setRequestMethod(method);
		con.addRequestProperty("Host", hostName);
		con.addRequestProperty("User-Agent", "cis455crawler");
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
			Thread.sleep(this.delay);
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
	public InputStream sendGetRequest() throws IOException {
		HttpURLConnection con = sendRequest(hostName, filePath);
		int responseCode = con.getResponseCode();
		if (responseCode==200) {
			return con.getInputStream();
		}
		System.err.println("Get request had response code of "+responseCode);
		return null;
	}
}

