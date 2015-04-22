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


	public InvertedIndexInputMapReader(String documentDirectiory) throws FileNotFoundException{
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
		if(done){
			return null;
		}

		if(index >= words.length){
			words = null;
			document = documentDB.getNextDocument();
			if(document != null){
				index = 0;
				words = cleanDocument(document.getContent()).split("\\s+");		
			}
			else{
				documentDB.close();
				done = true;
			}
		}
		else{
			currentWord = words[index];
			index++;
		}

		return currentWord;
	}

	public String cleanDocument(String docString){
		Document doc = Jsoup.parse(docString);
		return doc.body().text();
	}

}
