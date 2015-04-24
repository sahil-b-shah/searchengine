package mapreduce.InvertedIndexWorker;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;

public class InvertedIndexInputMapReader {

	DocumentData document;
	private String[] words;
	private int index;
	private boolean done;
	private DocumentDBWrapper documentDB;
	private int maxKeys;
	private int keysRead;
	
	public InvertedIndexInputMapReader(String documentDirectiory) throws FileNotFoundException{
		maxKeys = 100;
		keysRead = 0;
		documentDB = DocumentDBWrapper.getInstance(documentDirectiory);
		documentDB.initIterator();
		document = documentDB.getNextDocument();

		done = false;
		if(document != null){
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
			return null;
		}

		if(index >= words.length){
			words = null;
			document = documentDB.getNextDocument();
			System.out.println("Database size " + documentDB.getSize());
			if(document != null){
				index = 0;
				words = cleanDocument(document.getContent()).split("\\s+");	
				keysRead++;
				System.out.println("Mapping doc " + document.getUrl() + "----keysRead: " + keysRead);
			}
			else{
				documentDB.close();
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
