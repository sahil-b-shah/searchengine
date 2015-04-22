package mapreduce.InvertedIndexWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;

public class InvertedIndexInputMapReader {

	DocumentData document;
	private int fileIndex;
	private BufferedReader in;
	private boolean done;
	private DocumentDBWrapper documentDB;


	public InvertedIndexInputMapReader(String documentDirectiory) throws FileNotFoundException{
		documentDB = DocumentDBWrapper.getInstance(documentDirectiory);
		documentDB.initIterator();
		document = documentDB.getNextDocument();

		done = false;
		if(document != null){
			in = new BufferedReader(new StringReader(cleanDocument(document.getContent())));
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

		if(done){
			return null;
		}

		String line = in.readLine();

		if(line == null){
			in.close();  //close previous stream
			document = documentDB.getNextDocument();
			if(document != null){
				in = new BufferedReader(new StringReader(cleanDocument(document.getContent())));
			}
			else{
				documentDB.close();
				done = true;
				line = null;
			}
		}

		return line;
	}

	public String cleanDocument(String docString){

		String regex = "";
		
		docString = docString.replaceAll(regex, "");
		
		return docString;
	}



}
