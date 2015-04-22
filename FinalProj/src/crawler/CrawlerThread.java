package crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;
import crawler.storage.RobotsDBWrapper;
import crawler.storage.RobotsTxtData;
import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.URLFrontierData;
import crawler.storage.UnseenLinksDBWrapper;

public class CrawlerThread extends Thread {

	private boolean isStopped = false;
	private DocumentDBWrapper docDB;
	private URLFrontierDBWrapper frontierDB;
	private RobotsDBWrapper robotsDB;
	private UnseenLinksDBWrapper unseenLinksDB;
	private URLRequest request;
	private int maxLength = -1; //in bytes
	private String urlString;

	public CrawlerThread(DocumentDBWrapper docDB, URLFrontierDBWrapper frontierDB, RobotsDBWrapper robotsDB, UnseenLinksDBWrapper unseenLinksDB) {
		this.docDB = docDB;
		this.frontierDB = frontierDB;
		this.robotsDB = robotsDB;
		this.unseenLinksDB = unseenLinksDB;
	}

	public CrawlerThread(DocumentDBWrapper docDB, URLFrontierDBWrapper frontierDB, RobotsDBWrapper robotsDB, UnseenLinksDBWrapper unseenLinksDB, int maxLength) {
		this.docDB = docDB;
		this.frontierDB = frontierDB;
		this.maxLength = maxLength*1000000;
		this.robotsDB = robotsDB;
		this.unseenLinksDB = unseenLinksDB;
	}

	@Override
	public void run() {
		while(!isStopped) {
			
			try {
				Entry<Integer, URLFrontierData> entry = frontierDB.getNextUrl();
				if (entry != null) {
	
					//System.out.println("not null");
					urlString = entry.getValue().getUrl();
					request = new URLRequest(urlString);
	
					if(Crawler.addCurrentHost(request.getHost())){
						
						boolean check = false;
						try {
							check = checkRequest();
						} catch (UnsupportedEncodingException e1) {
						}
	
						if (check) {
							try {
								parseRequest(request.sendGetRequest());
							} catch (IOException e) {
								/*frontierDB.close();
							docDB.close();
							unseenLinksDB.close();
							robotsDB.close();
							System.err.println("Error sending GET request to server");
							e.printStackTrace();
							System.exit(-1);*/
							}
						}
						Crawler.deleteCurrentHost(request.getHost());
					}
					else{
						System.out.println("penis");
						frontierDB.addUrl(urlString);
					}
				} else {
					try {
						//Thread.sleep(300000);
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(frontierDB.isEmpty()){
						System.out.println("Crawl on thread " + Thread.currentThread().getId()+" is ending");
						frontierDB.close();
						docDB.close();
						unseenLinksDB.close();
						robotsDB.close();
						isStopped = true;
					}
				}
			} catch (Exception e) {
				System.err.println("Error processing link "+request.getUrlString());
				e.printStackTrace();
			}
			//db.close();
		}
	}

	private boolean checkRequest() throws UnsupportedEncodingException{
		boolean makeRequest = true;
		RobotsTxtData robots = null;
		try {
			robots = request.checkRobots();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		//Get set of disallowed paths from robots.txt
		ArrayList<String> disallowed = robots.getDisallowedLinks();	
		ArrayList<String> allowed = robots.getAllowedLinks();

		String allowedTestURL = decodeURL(request.getFilePath());

		if(!allowed(allowedTestURL, disallowed, allowed))
			return false;

		//System.out.println(disallowed);
		System.out.println("filepath is not disallowed");

		DocumentData ce = docDB.getContentById(request.getHost()+request.getFilePath());
		//Date lastSeen = checkModifiedDate(ce.getLastSeen());

		if (ce == null) {
			makeRequest = true & makeRequest;
			try {
				if (!request.sendHead()) {
					System.err.println("Error sending head request");
				}
			} catch (IOException e) {
				System.err.println("Error sending head request: IO Exception caught");
				e.printStackTrace();
			}
		} else {
			System.out.println("content is not null");
			//Date lastSeen = new Date(Long.valueOf(ce.getLastSeen()));
			boolean modified = false;

			try {
				modified = request.checkModified(Long.valueOf(ce.getLastSeen()));
			} catch (NumberFormatException | IOException e) {
				makeRequest = false;
				e.printStackTrace();
			}
			//System.out.println(request.getHost()+request.getFilePath()+ " last modified"+lastModified);
			//Check if last seen is less than last modified
			if (!modified) {
				System.out.println(request.getFilePath()+" Last modified is before last seen");
				return false;
			} else {
				System.out.println(request.getFilePath()+" Last modified is after last seen");
				makeRequest = true & makeRequest;
			}
		}
		//Check content length
		if (maxLength == -1) {
			makeRequest = true & makeRequest;
		} else if (request.getContentLength() == -1) {
			System.out.println(request.getContentLength()+" is not specified");
			return false;
			//makeRequest = false;
		} else if(request.getContentLength() <= maxLength) {
			makeRequest = true & makeRequest;
		} else if (request.getContentLength() > maxLength) {
			//makeRequest = false;
			System.out.println(request.getContentLength()+" is more than max");
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
			ArrayList<String> links = extractUrls(document);
			addContent(is2, links);
		} else if(contentType.contains("xml")) {
			addContent(is3, null);
		}
	}



	private void addContent(InputStream is, ArrayList<String> links) {
		byte b[] = new byte[request.getContentLength()];
		try {
			is.read(b);
		} catch (IOException e) {
			System.err.println("Error reading bytes from input stream");
			e.printStackTrace();
			System.exit(-1);
		}
		docDB.addContent(request.getHost()+request.getFilePath(),
				new String(b), System.currentTimeMillis(), links);
	}

	private ArrayList<String> extractUrls(Document doc) {
		ArrayList<String> links = new ArrayList<String>();
		doc.getDocumentElement().normalize();
		//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("a");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			System.out.println("\nCurrent Element :" + nNode.getNodeName());

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				System.out.println("Link : " + eElement.getAttribute("href"));
				String extractedString = eElement.getAttribute("href");
				URL url = makeAbsolute(request.getUrlString(), extractedString);
				//add to queue
				unseenLinksDB.addURL(url.getProtocol()+"://"+url.getHost()+url.getFile());
				links.add(url.getProtocol()+"://"+url.getHost()+url.getFile());

			}
		}
		return links;
	}

	private URL makeAbsolute(String urlStrin, String extractedString) {

		if (!urlString.endsWith(".html") && !urlString.endsWith("/")) {
			urlString = urlString.concat("/");
		}

		URL base = null;
		URL extracted = null;
		try {
			base = new URL(urlString);
			extracted = new URL(base, extractedString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		System.out.print("Extracted ");
		System.out.print(extracted.getHost()+extracted.getPath());
		System.out.println(" from "+base.getHost()+base.getPath());

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

	/**
	 * Method to check if link is allowed
	 * @param filepath - path of file to check
	 * @param disallowed links
	 * @param allowed links
	 * @return true if allowed, else false
	 */
	public static boolean allowed(String filePath, ArrayList<String> disallowedLinks, ArrayList<String> allowedLinks) {
		while(filePath != null){

			if(disallowedLinks != null && disallowedLinks.contains(filePath)) {
				return false;
			}
			if(allowedLinks != null && allowedLinks.contains(filePath)){
				return true;
			}

			int lastIndex = filePath.length() -1;
			if(lastIndex == -1){
				return true;
			}
			filePath = filePath.substring(0, lastIndex);
		}
		return true;
	}

	public static String decodeURL(String url) throws UnsupportedEncodingException{
		String decodedURL = url;
		Pattern pattern = Pattern.compile("%[0-9a-fA-f]{2}");
		Matcher matcher = pattern.matcher(url);
		while(matcher.find()){
			String encoded = matcher.group();
			if(!encoded.equalsIgnoreCase("%2f")){
				String decoded = URLDecoder.decode(encoded, "UTF-8");
				decodedURL = decodedURL.replace(encoded, decoded);
			}
		}
		return decodedURL;
	}

}
