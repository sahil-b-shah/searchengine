package crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import crawler.storage.DBWrapper;

public class CrawlerThread extends Thread {

	private boolean isStopped = false;
	private DBWrapper db;
	private URLRequest request;
	private int maxLength = -1; //in bytes
	private String urlString;
	
	public CrawlerThread(DBWrapper db) {
		this.db = db;
	}
	
	public CrawlerThread(DBWrapper db, int maxLength) {
		this.db = db;
		this.maxLength = maxLength*1000000;
	}
	
	@Override
	public void run() {
		while(!isStopped) {
			Entry<Integer, QueueEntity> entry = db.getNextUrl();
			if (entry != null) {
				//System.out.println("not null");
				urlString = entry.getValue().getUrl();
				request = new URLRequest(urlString);
			
				if (checkRequest()) {
					try {
						parseRequest(request.sendGetRequest());
					} catch (IOException e) {
						System.err.println("Error sending GET request to server");
						e.printStackTrace();
						System.exit(-1);
					}
				}
			} else {
				System.out.println("DB closing");
				db.close();
				isStopped = true;
			}
			//db.close();
		}
	}
	
	private boolean checkRequest(){
		boolean makeRequest = true;
		RobotsTxtInfo robots = request.getRobots();
		
		//Get set of disallowed paths from robots.txt
		List<String> disallowed = null;
		if (robots.containsUserAgent("cis455crawler")) {
			disallowed = robots.getDisallowedLinks("cis455crawler");
		} else if (robots.containsUserAgent("*")) {
			disallowed = robots.getDisallowedLinks("*");
		}
		
		System.out.println(disallowed);
		//Check if the request filepath is in the disallowed set
		if (disallowed == null) {
			 makeRequest = true;
		} else if (disallowed.contains(request.getFilePath())) {
			//makeRequest = false;
			System.out.println(request.getFilePath()+"filepath is disallowed");
			return false;
		}
		System.out.println("filepath is not disallowed");
		
		ContentEntity ce = db.getContentById(request.getHost()+request.getFilePath());
		//Date lastSeen = checkModifiedDate(ce.getLastSeen());
		
		if (ce == null) {
			makeRequest = true & makeRequest;
		} else {
			System.out.println("content is not null");
			Date lastSeen = new Date(Long.valueOf(ce.getLastSeen()));
			Date lastModified = request.getLastModified();
			//System.out.println(request.getHost()+request.getFilePath()+ " last modified"+lastModified);
			//Check if last seen is less than last modified
			if (lastModified.getTime()!= 0) {
				if (lastModified.before(lastSeen)) {
					//makeRequest = false;
					System.out.println(request.getFilePath()+" Last modified is before last seen");
					return false;
				} else {
					makeRequest = true & makeRequest;
				}
			}
		}
		System.out.println("Modified date is after last seen, or link has not been seen");
		
		//Check content length
		if (maxLength == -1) {
			makeRequest = true & makeRequest;
		} else if (request.getContentLength() == -1) {
			System.out.println(request.getContentLength()+"is not specified");
			return false;
			//makeRequest = false;
		} else if(request.getContentLength() <= maxLength) {
			makeRequest = true & makeRequest;
		} else if (request.getContentLength() > maxLength) {
			//makeRequest = false;
			System.out.println(request.getContentLength()+"is more than max");
			return false;
		}
		return makeRequest;
	}
	
	private void parseRequest(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[request.getContentLength()];
		int len;
		try {
			while ((len = is.read(buffer)) > -1 ) {
			    baos.write(buffer, 0, len);
			}
			baos.flush();
		} catch (IOException e1) {
			System.err.println("Error making new input stream");
			e1.printStackTrace();
			System.exit(-1);
		}
		
		InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
		InputStream is2 = new ByteArrayInputStream(baos.toByteArray()); 
		InputStream is3 = new ByteArrayInputStream(baos.toByteArray()); 


		//InputStream isCopy = new InputStream(is);
		String contentType = request.getContentType();
		if (contentType.contains("html")) {
			//Need to use JTidy
			ByteArrayOutputStream xhtmlOS = new ByteArrayOutputStream();
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			
			//xhtml is tide output from parsing
			tidy.parse(is1, xhtmlOS);
			
			Document document = null;
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				document = dBuilder.parse(new ByteArrayInputStream(xhtmlOS.toByteArray()));
			} catch (Exception e) {
				System.err.println("Error building w3 document: HTML");
				e.printStackTrace();
				System.exit(-1);
			}
			extractUrls(document);
			addContent(is2);
		} else if(contentType.contains("xml")) {
			//just add to XML table
			Document document = null;
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				document = dBuilder.parse(is1);
			} catch (Exception e) {
				System.err.println("Error building w3 document: XML");
				e.printStackTrace();
				System.exit(-1);
			}
			byte b[] = new byte[request.getContentLength()];
			try {
				is2.read(b);
			} catch (IOException e) {
				System.err.println("Error reading bytes from input stream: Line 200");
				e.printStackTrace();
				System.exit(-1);
			}
			addContent(is3);
			addXml(document, new String(b));
			//db.addXML(request.getHost()+request.getFilePath(), new String(b), System.currentTimeMillis());
		}
	}
	
	private void addXml(Document document, String content) {
		System.out.println("in add xml");
		Set<Entry<Integer, ChannelEntity>> channels = db.getAllChannels().entrySet();
		//xpe.setXPaths(s);
		XPathEngineImpl xpe = (XPathEngineImpl) XPathEngineFactory.getXPathEngine();

		for (Entry<Integer, ChannelEntity> e : channels) {
			ChannelEntity channel = e.getValue();
			Set<String> xpathSet = channel.getXpaths();
			String[] xpaths = xpathSet.toArray(new String[xpathSet.size()]);
			System.out.println(xpaths[0]);
			xpe.setXPaths(xpaths);
			boolean[] evals = xpe.evaluate(document);
			System.out.println(xpe.isValid(0));
			for (boolean b : evals) {
				System.out.println(b);
				if(b) {
					db.addXML(e.getKey(), request.getHost()+request.getFilePath(),
							content, System.currentTimeMillis());
					break;
				}
			}
		}
	}
	
	private void addContent(InputStream is) {
		byte b[] = new byte[request.getContentLength()];
		try {
			is.read(b);
		} catch (IOException e) {
			System.err.println("Error reading bytes from input stream");
			e.printStackTrace();
			System.exit(-1);
		}
		db.addContent(request.getHost()+request.getFilePath(),
				new String(b), System.currentTimeMillis());
	}
	
	private void extractUrls(Document doc) {
		doc.getDocumentElement().normalize();
		//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("a");
	 	 
		for (int temp = 0; temp < nList.getLength(); temp++) {
	 
			Node nNode = nList.item(temp);
			System.out.println("\nCurrent Element :" + nNode.getNodeName());
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				System.out.println("Link : " + eElement.getAttribute("href"));
				String urlString = eElement.getAttribute("href");
				URL url = makeAbsolute(urlString);
				if (!db.checkUrlSeen(url.getHost()+url.getFile())) {
					//add to queue
					db.addUrl(url.getProtocol()+"://"+url.getHost()+url.getFile());
				}
			}
		}
	}
	
	private URL makeAbsolute(String extractedString) {
		URL base = null;
		URL extracted = null;
		try {
			base = new URL(urlString);
			extracted = new URL(base, extractedString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		/*if (urlString.startsWith("/")) {
			StringBuffer sb = new StringBuffer();
			sb.append(request.getProtocol()+"://"+request.getHost()+"/"+urlString.substring(1));
			return sb.toString();
		} else if (!urlString.startsWith("http")) {
			StringBuffer sb = new StringBuffer();
			//if file
			if (urlString.contains(".")) {
				/
			}
			sb.append(request.getProtocol()+"://"+request.getHost()+"/"+urlString);
			return sb.toString();
		}*/
		System.out.println(base.getHost()+base.getPath());
		System.out.println(extracted.getHost()+extracted.getPath());

		return extracted;
	}

	public synchronized void doStop() {
		this.isStopped = true;
		if(this.getState() == Thread.State.RUNNABLE) {
			Thread.currentThread().interrupt();
		}
		//this.interrupt();
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
	
}
