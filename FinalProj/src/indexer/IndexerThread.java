package indexer;

import java.io.IOException;
import java.util.HashMap;

import mapreduce.MyHttpClient;
import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;


public class IndexerThread extends Thread {
	private static String master = "52.10.8.98:80";
	private DocumentDBWrapper documentDB;

	public IndexerThread(DocumentDBWrapper documentDB) {
		this.documentDB = documentDB;
	}

	public void run(){
		DocumentData document = documentDB.getNextDocument();
		
		while(document != null){
			DocumentIndex indexToSend = indexDocument(document);
			
			try {
				sendIndex(indexToSend, document.getUrl());
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
	
	public static DocumentIndex indexDocument(DocumentData document){
		DocumentIndex index = new DocumentIndex();
		String words []  = document.getContent().split("[^\\w']+");
		for(String word: words){
			index.addWord(word.toLowerCase());
		}
		return index;
	}
	
	
	private static void sendIndex(DocumentIndex indexToSend, String url) throws IOException {
		MyHttpClient client = new MyHttpClient(master, "/InvertedIndexMaster/pushdata");
		
		String body = url + " " + indexToSend.getMaxOccurence() + "\n";
		HashMap<String, Integer> map = indexToSend.getWords();
		for(String wordToAdd: map.keySet()){
			body += wordToAdd + " " + map.get(wordToAdd) + "\n";
		}
		client.setBody(body);
		System.out.println(body);
		client.sendPost();
		client.getResponse();
	}
}
