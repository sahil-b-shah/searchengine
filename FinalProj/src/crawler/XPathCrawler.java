package crawler;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.crawler.info.URLInfo;
import edu.upenn.cis455.storage.Content;
import edu.upenn.cis455.storage.DBWrapperContent;


public class XPathCrawler {

	private static DBWrapperContent dbc;
	private static int limit;
	private static int scanned = 0;
	
	private static LinkedList<String> urlstocrawl;
	private static Set<String> crawled;
	
	private static boolean has455agent;
	
	private static RobotsTxtInfo rbtxt;
	private static int del;
	
	public static void main(String[] args) throws IOException, InterruptedException{
		if (args.length < 3 || args.length > 5) {
			System.err.println(
					"ERROR: WRONG NUMBER OF ARGS");
			System.exit(1);
		}

		String hostName = args[0];
		String directory = args[1];
		int maxsize = Integer.parseInt(args[2]) * 1000000; //convert given number to Bytes
		limit = -1;
		scanned = 0;
		if (args.length == 4){
			limit = Integer.parseInt(args[3]);
		}
		rbtxt = new RobotsTxtInfo();
		
		String host = null;
		if (hostName.contains("http")){
			host = hostName.split("/")[2];
		}
		else{
			host = hostName.split("/")[0];
		}
		
		System.out.println("Host is :" + host);
		
		//System.out.println(hostName.split("/")[2]);
		
		scanRobots(hostName);
		if (rbtxt.containsUserAgent("cis455crawler")){
			has455agent = true;
		}
		else{
			System.out.println("nocis455crawler");
			has455agent = false;
		}
		
		dbc = new DBWrapperContent(directory);
		
		//dbc.clearDatabase();
		
		//dbc.deleteContent(hostName);
		
		urlstocrawl = new LinkedList<String>();
		urlstocrawl.add(hostName);
		del = 0;
		if(has455agent){
			if (rbtxt.crawlContainAgent("cis455crawler")){
				del = rbtxt.getCrawlDelay("cis455crawler");
			}
		}
		else{
			if (rbtxt.crawlContainAgent("*")){
				del = rbtxt.getCrawlDelay("*");
			}
		}
		while(!urlstocrawl.isEmpty() && limit != scanned){
			String someurl = urlstocrawl.getFirst();
			urlstocrawl.remove();
			if (!someurl.contains(host)){
				rbtxt = new RobotsTxtInfo();
				scanRobots(someurl);
			}
			System.out.println("LOOKING: " + someurl);
			Crawl(someurl, maxsize);
			//System.out.println(dbc.getCount());
		}
		System.out.println("Downloaded: " + scanned + " pages");
	}

	public static void Crawl(String searchurl, int max) throws InterruptedException{

		Socket crawlSocket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		InputStream ids = null;
		OutputStream ods = null;
		
		
		HttpsURLConnection hpage = null;
		boolean ishttps = true;
		//System.out.println("Looking at: " + searchurl);
		Thread.sleep(del * 1000); //TODO

		try{
			if(searchurl.contains("https")){
				URL rl = new URL(searchurl);
				hpage = (HttpsURLConnection)rl.openConnection();
				//hpage.setFollowRedirects(true);
				hpage.setInstanceFollowRedirects(true);

				hpage.setRequestMethod("HEAD");
				hpage.setDoInput(true);
				hpage.setDoOutput(true);
				hpage.addRequestProperty("User-Agent", "cis455crawler");
				//ids = hpage.getInputStream();
				
				
				
				hpage.setRequestMethod("GET");
				ods = hpage.getOutputStream();
				
				out = new PrintWriter(ods, true);
				out.flush();
				
				int status = hpage.getResponseCode();
				//System.out.println("Response code: " + status);
				searchurl = hpage.getURL().toString();
				
				ArrayList<String> nonos = null;
				ArrayList<String> oks = null;
				if (has455agent){
					nonos = rbtxt.getDisallowedLinks("cis455crawler");
					oks = rbtxt.getAllowedLinks("cis455crawler");
				}
				else{
					nonos = rbtxt.getDisallowedLinks("*");
					oks = rbtxt.getAllowedLinks("*");
				}
				for (String s: nonos){
					//System.out.println("Disallowe: " + s);

					if (searchurl.contains(s)){
						if (oks != null){
							for (String sok : oks){
							if(searchurl.contains(sok)){
								continue;
							}
						}
						}
						return; //Don't do anything else with this link
					}
				}
				
				
				if (hpage.getHeaderField("Location") != null){
					String redirect = hpage.getHeaderField("Location");
					System.out.println("REDIRECTED BITCHES!!!!" + redirect);
				}
				
				//System.out.println(hpage.getHeaderField("Content-Length"));
				
				if (Integer.valueOf(hpage.getHeaderField("Content-Length")) > max){ //get Content length of request
					System.out.println("Skipping(due to size): " + searchurl);
					return;
				}
				
				
				Thread.sleep(del * 1000); //TODO

				out.print("GET / HTTP/1.1\r\n" + "Host: " + searchurl + "\r\n" + "User-Agent: cis455crawler\r\n");
				
				Content c = DBWrapperContent.getContent(searchurl);
				//System.out.println("Looking for primary key: " + searchurl);
				Document page = null;
				if (c == null){
				//	System.out.println("URL is not in database yet");
			/*		Connection con = Jsoup.connect(searchurl); //not supposed to use JSOUP as GET request
					page = con.userAgent("cis455crawler").get();*/
					out.print("\r\n");
					out.flush();
				}else{
					//System.out.println("URL IS IN DATABSE! TIME TO CHECK MOD DATE");
						SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
						dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
						//SimpleDateFormat dateFormatLocal = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
						Date current = new Date(c.getModified());
						String moddate = dateFormatGmt.format(current);
						if (hpage.getHeaderField("Last-Modified") != null){
							String modified = hpage.getHeaderField("Last-Modified");
							Date d = dateFormatGmt.parse(modified);
							long modd = d.getTime();
							if(current.getTime() > modd){
								System.out.println(searchurl + ": Not Modified");
								return;
							}

						}
						
						out.print("If-Modified-Since: " + moddate + "\r\n");
						//hpage.addRequestProperty("If-Modified-Since", moddate);	
				}
				
				
				
				
				ids = hpage.getInputStream();
				in = new BufferedReader(new InputStreamReader(ids));
				
				String temp = "";
				StringBuilder raw = new StringBuilder();
				while ((temp = in.readLine()) != null){ //print out contents of page to console
					raw.append(temp);
					//System.out.println(temp);
				}
				
				//System.out.println("Size: " + raw.toString().getBytes().length);

				//System.out.println("Got: " + raw.toString().getBytes().length);
				page = Jsoup.parse(raw.toString(), searchurl);
				
				System.out.println(searchurl + ": Downloading");
				//System.out.println(page.outerHtml());
				Content basic = new Content(System.currentTimeMillis(), raw.toString(), searchurl);
				dbc.putContent(basic);
				dbc.synchronize();
				scanned++;
				
				//System.out.println("Getting links");
				Elements links = page.select("a[HREF]");
				
				for (Element link: links){ //add links to 
					//System.out.println(link.attr("href"));
					String linkurl = link.absUrl("href");
					//System.out.println("URL to add to crawl: " + linkurl);
					urlstocrawl.add(linkurl);
										
				}
				
				ods.close();
				ids.close();
			}else{ //REGULAR HTTP
				
				URLInfo uifo = new URLInfo(searchurl);
				//System.out.println(uifo.getHostName());
				//System.out.println(uifo.getFilePath());
				
				crawlSocket = new Socket(uifo.getHostName(), 80);

				//ids = crawlSocket.getInputStream();
				ods = crawlSocket.getOutputStream();
				
				out = new PrintWriter(ods);
				out.println("HEAD " + uifo.getFilePath() + " HTTP/1.1");
				out.println("Host: " + uifo.getHostName());
				out.println("User-Agent: cis455crawler\r\n");
				out.flush();
				
				ids = crawlSocket.getInputStream();
				in = new BufferedReader(new InputStreamReader(ids));
				
				String filepath = uifo.getFilePath();
				
				String temp = "";
				String lastmod = null;
				StringBuilder raw = new StringBuilder();
				while ((temp = in.readLine()) != null){ //print out contents of page to console
					if (temp.toLowerCase().contains("content-length:")){
						String len = temp.split(": ")[1];
						if (Integer.valueOf(len) > max){
							System.out.println("Skipping(due to size): " + searchurl);
							return;
						}
					}
					if (temp.toLowerCase().contains("last-modified:")){
						lastmod = temp.split(": ")[1];
					}
					if (temp.toLowerCase().contains("location:")){
						filepath = temp.split(": ")[1];
						System.out.println(filepath);
					}
					raw.append(temp);
					//System.out.println(temp);
					
				}
				
				//System.out.println("Finished reading HEad");
				//Copied code from HTTP section
				ArrayList<String> nonos = null;
				ArrayList<String> oks = null;
				if (has455agent){
					nonos = rbtxt.getDisallowedLinks("cis455crawler");
					oks = rbtxt.getAllowedLinks("cis455crawler");
				}
				else{
					nonos = rbtxt.getDisallowedLinks("*");
					oks = rbtxt.getAllowedLinks("*");
				}
				//System.out.println(nonos.get(0));
				for (String s: nonos){
				//	System.out.println("Disallowe: " + s);

					if ((uifo.getHostName()+filepath).contains(s)){
						if (oks != null){
							for (String sok : oks){
							if((uifo.getHostName()+filepath).contains(sok)){
								continue;
							}
						}
						}
						return; //Don't do anything else with this link
					}
				}
				
				
				crawlSocket.close();
				ods.close();
				ids.close();
				
				crawlSocket = new Socket(uifo.getHostName(), 80);
				
				
				Thread.sleep(del * 1000);
				ods = crawlSocket.getOutputStream();
				out = new PrintWriter(ods);

				
				out.println("GET " + filepath + " HTTP/1.1");
				out.println("Host: " + uifo.getHostName());
				out.println("User-Agent: cis455crawler\r\n");
				out.flush();

				ids = crawlSocket.getInputStream();
				BufferedReader newin = new BufferedReader(new InputStreamReader(ids));
				
				Content c = DBWrapperContent.getContent(uifo.getHostName()+filepath);
				//System.out.println("Looking for primary key: " + searchurl);
				Document page = null;
				if (c == null){
					//System.out.println("URL is not in database yet");
			/*		Connection con = Jsoup.connect(searchurl); //not supposed to use JSOUP as GET request
					page = con.userAgent("cis455crawler").get();*/
					out.print("\r\n");
					out.flush();
				}else{
					//System.out.println("URL IS IN DATABSE! TIME TO CHECK MOD DATE");
						SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
						dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
						//SimpleDateFormat dateFormatLocal = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
						Date current = new Date(c.getModified());
						if (lastmod != null){
							Date d = dateFormatGmt.parse(lastmod);
							long modd = d.getTime();
							if(current.getTime() > modd){
								System.out.println(searchurl + ": Not Modified");
								return;
							}

						}
				}
				
				
				raw = new StringBuilder();
				while ((temp = newin.readLine()) != null){ //print out contents of page to console
					raw.append(temp);
					//System.out.println(temp);
					if (temp.toLowerCase().contains("content-length")){
						String len = temp.split(": ")[1];
						if (Integer.valueOf(len) > max){
							System.out.println("Skipping(due to size): " + searchurl);
							return;
						}
					}
				}
				
				page = Jsoup.parse(raw.toString(), searchurl);
				
				System.out.println(searchurl + ": Downloading");
				//System.out.println(page.outerHtml());
				Content basic = new Content(System.currentTimeMillis(), raw.toString(), uifo.getHostName()+filepath);
				dbc.putContent(basic);
				dbc.synchronize();
				scanned++;
				
				//System.out.println("Getting links");
				Elements links = page.select("a[HREF]");
				
				for (Element link: links){ //add links to 
					//System.out.println(link.attr("href"));
					String linkurl = link.absUrl("href");
				//	System.out.println("URL to add to crawl: " + linkurl);
					urlstocrawl.add(linkurl);
										
				}
				
				
				ods.close();
				ids.close();
			}
			
			
		}catch(IOException ioe){
			System.out.println(ioe);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	public static void scanRobots(String url){
		String[] root = url.split("/");
		//System.out.println(root[0]);
		System.out.println("Scanning Robots.txt");
		String robots = root[0] + "//" + root[2] + "/robots.txt";
		boolean hasagent = false;
		URL roboturl = null;
		try {
			roboturl = new URL(robots);
		    BufferedReader in = new BufferedReader(new InputStreamReader(roboturl.openStream()));
		    String line = "";
		    String uagent = "";
		    while((line = in.readLine()) != null) {
		    	//System.out.println(line);
		        if (line.toLowerCase().contains("user-agent:")){
		        	String agent = line.split(":")[1].trim();
		        	uagent = agent;
	        		rbtxt.addUserAgent(agent);
		        }
		        else if(line.toLowerCase().contains("disallow:")){
		        		String disallowed = line.split(":")[1].trim();
		        		rbtxt.addDisallowedLink(uagent, disallowed);
		        }
		        else if(line.toLowerCase().contains("allow:")){
		        		String allowed = line.split(":")[1].trim();
		        		rbtxt.addAllowedLink(uagent, allowed);
		        }
		        else if(line.toLowerCase().contains("crawl-delay:")){
		        		String delay = line.split(":")[1].trim();
		        		rbtxt.addCrawlDelay(uagent, Integer.valueOf(delay));
		        }
		   
		    }
		    in.close();
		} catch (IOException e1) {
			System.out.println("Robots failed");
		}
	}
	
	 
}
