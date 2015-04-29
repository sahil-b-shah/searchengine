package indexer;

import java.io.IOException;
import java.util.HashMap;

import mapreduce.MyHttpClient;
import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;


public class IndexerThread extends Thread {
	private static String master = "54.213.18.16:80";
	private DocumentDBWrapper documentDB;

	public IndexerThread(DocumentDBWrapper documentDB) {
		// TODO Auto-generated constructor stub
		this.documentDB = documentDB;
	}

	public void run(){
		DocumentData document = documentDB.getNextDocument();
		
		while(document != null){
			DocumentIndex indexToSend = indexDocument(document);
			
			try {
				sendIndex(indexToSend, document.getUrl());
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			document = documentDB.getNextDocument();
		}
		
		//printIndex();
		
		documentDB.closeIterator();
		documentDB.close();
	}
	
	public static DocumentIndex indexDocument(DocumentData document){
		DocumentIndex index = new DocumentIndex();
		String words []  = document.getContent().split("\\s+");
		for(String word: words){
			index.addWord(word);
		}
		return index;
		
		
	}
	
	
	private static void sendIndex(DocumentIndex indexToSend, String url) throws IOException {
		MyHttpClient client = new MyHttpClient(master, "/InvertedIndexMaster/pushdata");
		
		String body = indexToSend.getMaxWord() + " " + indexToSend.getMaxOccurence() + "\n";
		HashMap<String, Integer> map = indexToSend.getWords();
		for(String wordToAdd: map.keySet()){
			body += url + " " + wordToAdd + " " + map.get(wordToAdd) + "\n";
		}
		client.setBody(body);
		client.sendPost();
		client.getResponse();

	}
}
