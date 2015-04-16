package crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.storage.ContentEntity;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.QueueEntity;


public class Crawler {
	
	private static String urlString;
	private static String envDirectory;
	private static int maxSize;
	private static int maxFiles;
	
	private static ThreadPool pool;
	
	private static void setup() {
		pool = new ThreadPool(1, envDirectory, maxSize);
	}
	
	public static void main(String [] args) {
		if((args.length != 4) && (args.length != 3)) {
			System.err.println("Incorrect number of arguments");
			System.exit(-1);
		}
		
		urlString = args[0];
		//Directory for store
		envDirectory = args[1];
		urlString = "https://dbappserv.cis.upenn.edu/crawltest/marie/tpc/part.xml";
		
		maxSize = Integer.parseInt(args[2]);
		if (args.length == 4) {
			maxFiles = Integer.parseInt(args[3]);
		}
		DBWrapper db = DBWrapper.getInstance(envDirectory);
		db.addUrl(urlString);
		db.getAllContent();
		db.close();
		setup();
	}
}
