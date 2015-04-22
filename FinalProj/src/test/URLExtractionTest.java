package test;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

public class URLExtractionTest extends TestCase {

	public void test1() {
		String urlString = "https://dbappserv.cis.upenn.edu/crawltest.html";
		String extractedString = "crawltest/marie/private";
		
		URL result = makeAbsolute(urlString, extractedString);
		assertTrue(result.getHost().equals("dbappserv.cis.upenn.edu"));
		assertTrue(result.getPath().equals("/crawltest/marie/private"));
	}
	
	public void test2() {
		String urlString = "https://dbappserv.cis.upenn.edu/crawltest/";
		String extractedString = "nytimes";
		
		URL result = makeAbsolute(urlString, extractedString);
		assertTrue(result.getHost().equals("dbappserv.cis.upenn.edu"));
		assertTrue(result.getPath().equals("/crawltest/nytimes"));
	}
	
	public void test3() {
		String urlString = "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/";
		String extractedString = "part.xml";
		
		URL result = makeAbsolute(urlString, extractedString);
		assertTrue(result.getHost().equals("dbappserv.cis.upenn.edu"));
		assertTrue(result.getPath().equals("/crawltest/marie/tpc/part.xml"));
	}

	private URL makeAbsolute(String urlString, String extractedString) {
		
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
		
		System.out.println(base.getPath());
		System.out.println(extracted.getPath());
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
}
