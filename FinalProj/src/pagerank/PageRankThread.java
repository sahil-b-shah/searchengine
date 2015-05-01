package pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mapreduce.MyHttpClient;
import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;


public class PageRankThread extends Thread {
	private static String master = "52.10.8.98:80";
	private DocumentDBWrapper documentDB;

	public PageRankThread(DocumentDBWrapper documentDB) {
		this.documentDB = documentDB;
	}

	public void run(){
		DocumentData document = documentDB.getNextDocument();

		while(document != null){
			ArrayList<String> outurls = document.getLinks();

			try {
				sendList(outurls, document.getUrl());
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			document = documentDB.getNextDocument();
		}


	}

	private static void sendList(ArrayList<String> outurls, String url) throws IOException {
		MyHttpClient client = new MyHttpClient(master, "/PageRankMaster/pushdata");

		if(outurls == null) return;
		int outgoingCount = outurls.size();


		String body = "";
		for(String outgoing: outurls){
			//Incoming -- Outgoing count for Incoming Link -- URL
			body += url + " " + outgoingCount + " " + outgoing + "\n";
		}
		client.setBody(body);
		client.sendPost();
		client.getResponse();
	}
}
