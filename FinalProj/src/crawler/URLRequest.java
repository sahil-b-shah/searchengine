package crawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.sleepycat.je.DatabaseException;

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
	
	
	private int delay = 0;
	
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
		System.out.println(urlString);
		
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
	 * 
	 * @param date
	 * @return true if modified since last modified date, false otherwise 
	 * @throws ProtocolException 
	 */
	public synchronized boolean checkModified(long date) throws ProtocolException {
		HttpURLConnection con = sendRequest(hostName, filePath, "HEAD");
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		String dateString = format.format((new Date(date)));
		
		con.addRequestProperty("If-Modified-Since", dateString);
		
		return (con.getResponseCode()==200) ? true : false;
	}
	
	public RobotsTxtData checkRobots() throws IOException {
		RobotsDBWrapper robotsDB = null;
		robotsDB = RobotsDBWrapper.getInstance("/home/cis455/storage");

		
		RobotsTxtData robotsTxtData = robotsDB.getRobotsTxtData(this.hostName);
		if (robotsTxtData == null) {
			constructRobotsTxt(this.hostName);
			robotsTxtData = robotsDB.getRobotsTxtData(this.hostName);
		}
		
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
			 
			//Extract key-values of robot
			Pattern p = Pattern.compile(KEY_VALUE_REGEX);
			Matcher m = p.matcher(string);
			if (m.find()) {
				String key = m.group(1);
				String value = m.group(2);
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
		
		if (!filepath.startsWith("/")) {
			filepath = "/"+filepath;
		}
		String requestString = this.protocol+"://"+hostName + filepath;
		
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
	
	private void parseHeadResponse(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s;
		Pattern p = Pattern.compile(KEY_VALUE_REGEX);
		
		try {
			while((s = br.readLine()) != null) {
				System.out.println(s);
				if (s.isEmpty()) {
					break;
				}
				Matcher m = p.matcher(s);
				if (m.find()) {
					String key = m.group(1);
					String value = m.group(2);
					if (key.equalsIgnoreCase("Content-Type")) {
						contentType = value;
					} else if(key.equalsIgnoreCase("Content-Length")) {
						contentLength = Integer.valueOf(value);
					} else if(key.equalsIgnoreCase("Last-Modified")) {
						this.lastModified = checkModifiedDate(value);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading from input stream");
			e.printStackTrace();
			System.exit(-1);
		}
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
	
	private void sendHttpsHeadRequest(URL url) throws IOException {
		System.out.println("send HEAD https");

		HttpURLConnection con = (HttpsURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("HEAD");
		con.addRequestProperty("Host", hostName);
		con.addRequestProperty("User-Agent", "cis455crawler");
		/*PrintWriter pw = new PrintWriter(con.getOutputStream());
		System.out.println("HEAD " + filePath + " HTTP/1.1");
		pw.println("HEAD " + filePath + " HTTP/1.1");
		pw.println("Host: " + hostName);
		pw.println("User-Agent: cis455crawler");
		pw.println();
		pw.flush();*/
		
		//If connection accepted
		if(con.getResponseCode() == 200) {
			//System.out.println(con.getContentType());
			contentType = con.getContentType();
			contentLength = con.getContentLength();
			this.lastModified = new Date(con.getLastModified());
		}
	}
	
	private void delay(int milli) {
		milli = 0; //for testing
		try {
			Thread.sleep(milli);
		} catch (InterruptedException e) {
			System.err.println("Error sleeping thread");
			e.printStackTrace();
		}
	}
	
	public InputStream sendGetRequest() throws IOException {
		delay(delay*1000);
		InputStream is = null;
		System.out.println("Making request to " + hostName + filePath);
		if(protocol.equalsIgnoreCase("http")) {
			Socket s = null;
			try {
				s = new Socket(InetAddress.getByName(hostName), port);
			} catch (UnknownHostException e) {
				System.err.println("Host could not be resolved");
				e.printStackTrace();
				System.exit(-1);
			} catch (IOException e) {
				System.err.println("Could not create stream socket");
				e.printStackTrace();
				System.exit(-1);
			}
			
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			pw.println("GET " + filePath + " HTTP/1.1");
			pw.println("Host: " + hostName);
			pw.println("User-Agent: cis455crawler");
			//pw.println("Host: sahil");
			pw.println();
			pw.flush();
			System.out.println("Downloading content from " + hostName);
			is = s.getInputStream();
			
			String str;
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				while((str = br.readLine()) != null) {
					System.out.println(str);
					if (str.isEmpty()) {
						break;
					}
				}
			} catch (IOException e) {
				System.err.println("Error reading from input stream");
				e.printStackTrace();
				System.exit(-1);
			}
			
		} else if (protocol.equalsIgnoreCase("https")) {
			URL url = new URL(this.urlString);
			HttpURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.addRequestProperty("Host", hostName);
			con.addRequestProperty("User-Agent", "cis455crawler");
			
			//If connection accepted
			if(con.getResponseCode() == 200) {
				System.out.println("Downloading content from " + hostName);
				is = con.getInputStream();
			} 
		}
		return is;
	}
}

