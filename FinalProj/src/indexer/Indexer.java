package indexer;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import mapreduce.MyHttpClient;
import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;

public class Indexer{
	
	
	private static HashMap<String, HashMap<String, Integer>> msgData;
	private static String master = "54.213.18.16:80";

	public static void main(String args[]){
		msgData = new HashMap<String, HashMap<String, Integer>>();
		String documentDirectory = args[0];
		DocumentDBWrapper documentDB = DocumentDBWrapper.getInstance(documentDirectory);
		documentDB.initIterator();
		
		DocumentData document = documentDB.getNextDocument();
		
		while(document != null){
			DocumentIndex indexToSend = indexDocument(document);
			
			sendIndex(indexToSend, document.getUrl());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			document = documentDB.getNextDocument();
		}
		
		//printIndex();
		
		documentDB.closeIterator();
		documentDB.close();
	}
	
	
	private static void sendIndex(DocumentIndex indexToSend, String url) {
		/*
		for(String wordToAdd: indexToSend.getWords().keySet()){
			HashMap<String, Integer> wordMap = msgData.get(wordToAdd);
			if(wordMap == null){
				wordMap = new HashMap<String, Integer>();
			}
			int occur = 0;
			if(wordMap.get(url) != null){
				occur = wordMap.get(url);
			}
			
			occur += indexToSend.getWords().get(wordToAdd);
			
			wordMap.put(url, occur);
			
			msgData.put(wordToAdd, wordMap);
			
		}
		*/
		
		MyHttpClient client = new MyHttpClient(master, "/InvertedIndexMaster/pushdata");
		
		String body = indexToSend.getMaxWord() + " " + indexToSend.getMaxOccurence() + "\n";
		HashMap<String, Integer> map = indexToSend.getWords();
		for(String wordToAdd: map.keySet()){
			body += url + " " + wordToAdd + " " + map.get(wordToAdd) + "\n";
		}
		
		client.setBody(body);
		client.sendPost();

	}


	public static DocumentIndex indexDocument(DocumentData document){
		DocumentIndex index = new DocumentIndex();
		String words []  = document.getContent().split("\\s+");
		for(String word: words){
			index.addWord(word);
		}
		
		
		return index;
		
		
	}
	
	public static void printIndex(){
		for(String word: msgData.keySet()){
			System.out.println("Word: " + word);
			System.out.println("Size" + msgData.get(word).size());
			int i = 0;
			for(String url: msgData.get(word).keySet()){
				if(i > 10){
					break;
				}
				System.out.println("URL: " + url + " Occurences" + msgData.get(word).get(url));
				i++;	
			}
		}
	}
	
}
