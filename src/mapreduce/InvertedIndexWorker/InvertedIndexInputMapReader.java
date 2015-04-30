package mapreduce.InvertedIndexWorker;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;
import crawler.storage.IndexDocumentDBWrapper;

public class InvertedIndexInputMapReader {

	DocumentData document;
	private String[] words;
	private int index;
	private boolean done;
	private DocumentDBWrapper documentDB;
	private IndexDocumentDBWrapper indexDocumentDB;
	private int maxKeys;
	private int keysRead;
	
	public InvertedIndexInputMapReader(String documentDirectiory, String indexedDocumentDirectory) throws FileNotFoundException{
		maxKeys = 5;
		keysRead = 0;
		documentDB = DocumentDBWrapper.getInstance(documentDirectiory);
		indexDocumentDB = IndexDocumentDBWrapper.getInstance(indexedDocumentDirectory);
		documentDB.initIterator();
		document = documentDB.getNextDocument();

		done = false;
		if(document != null){
			indexDocumentDB.addContent(document.getUrl(), document.getContent(), Long.parseLong(document.getLastSeen()), document.getLinks());
			System.out.println("New doc size " + indexDocumentDB.getSize());
			index = 0;
			words = cleanDocument(document.getContent()).split("\\s+");
		}
		else
			done = true;
	}

	/**
	 * Gets next line
	 * @return line read, or null if done
	 * @throws IOException
	 */
	public synchronized String readLine() throws IOException{
		String currentWord = null;
		if(done | keysRead >= maxKeys){
			documentDB.close();
			indexDocumentDB.close();
			System.out.println("done: " + done + "-----keysRead: " + keysRead + "-----maxKeys: " + maxKeys);
			return null;
		}

		if(index >= words.length){
			keysRead++;
			if(keysRead >= maxKeys){
				documentDB.close();
				indexDocumentDB.close();
				System.out.println("done: " + done + "-----keysRead: " + keysRead + "-----maxKeys: " + maxKeys);
				return null;
			}
			words = null;
			document = documentDB.getNextDocument();
			System.out.println("Database size " + documentDB.getSize());
			if(document != null){
				indexDocumentDB.addContent(document.getUrl(), document.getContent(), Long.parseLong(document.getLastSeen()), document.getLinks());
				System.out.println("New doc size " + indexDocumentDB.getSize());
				index = 0;
				words = cleanDocument(document.getContent()).split("\\s+");	
				System.out.println("Mapping doc " + document.getUrl() + "----keysRead: " + keysRead);
				currentWord = words[index] + " " +document.getUrl();
				index++;
			}
			else{
				System.out.println("Doc was null");
				documentDB.close();
				indexDocumentDB.close();
				
				done = true;
			}
		}
		else{
			currentWord = words[index] + " " +document.getUrl();
			index++;
		}

		return currentWord;
	}

	public String cleanDocument(String docString){
		Document doc = Jsoup.parse(docString);
		return doc.body().text();
	}

}
