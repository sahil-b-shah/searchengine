package indexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import mapreduce.MyHttpClient;
import crawler.storage.DocumentDBWrapper;
import crawler.storage.DocumentData;


public class IndexerThread extends Thread {
	private static String master = "52.10.8.98:80";
	private DocumentDBWrapper documentDB;
	private HashSet<String> stopWords;

	public IndexerThread(DocumentDBWrapper documentDB) {
		this.documentDB = documentDB;
		stopWords = getHash();
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

	public DocumentIndex indexDocument(DocumentData document){
		DocumentIndex index = new DocumentIndex();
		String content = document.getContent().replaceAll("[^a-zA-Z\\s]+", "");
		String words []  = content.split("\\s+");
		for(String word: words){
			if(word.length() > 2 && word.length() < 15  && !stopWords.contains(word)){
				//System.out.println("Word: " + word);
				index.addWord(word.toLowerCase());
			}
		}
		return index;
	}


	private void sendIndex(DocumentIndex indexToSend, String url) throws IOException {
		MyHttpClient client = new MyHttpClient(master, "/InvertedIndexMaster/pushdata");

		String body = url + " " + indexToSend.getMaxOccurence() + "\n";
		System.out.println("Url sending: " + url);
		HashMap<String, Integer> map = indexToSend.getWords();
		for(String wordToAdd: map.keySet()){
			body += wordToAdd + " " + map.get(wordToAdd) + "\n";
		}
		client.setBody(body);
		//System.out.println("Body sent" + body);
		client.sendPost();
		client.getResponse();
	}
	
	public HashSet<String> getHash(){
		HashSet<String> hs = new HashSet<String>();
		hs.add("a");
		hs.add("about");
		hs.add("above");
		hs.add("after");
		hs.add("again");
		hs.add("against");
		hs.add("all");
		hs.add("am");
		hs.add("an");
		hs.add("and");
		hs.add("any");
		hs.add("are");
		hs.add("arent");
		hs.add("as");
		hs.add("at");
		hs.add("be");
		hs.add("because");
		hs.add("been");
		hs.add("before");
		hs.add("being");
		hs.add("below");
		hs.add("between");
		hs.add("both");
		hs.add("but");
		hs.add("by");
		hs.add("cant");
		hs.add("cannot");
		hs.add("could");
		hs.add("couldnt");
		hs.add("did");
		hs.add("didnt");
		hs.add("do");
		hs.add("does");
		hs.add("doesnt");
		hs.add("doing");
		hs.add("dont");
		hs.add("down");
		hs.add("during");
		hs.add("each");
		hs.add("few");
		hs.add("for");
		hs.add("from");
		hs.add("further");
		hs.add("had");
		hs.add("hadnt");
		hs.add("has");
		hs.add("hasnt");
		hs.add("have");
		hs.add("havent");
		hs.add("having");
		hs.add("he");
		hs.add("he'd");
		hs.add("he'll");
		hs.add("hes");
		hs.add("her");
		hs.add("here");
		hs.add("heres");
		hs.add("hers");
		hs.add("herself");
		hs.add("him");
		hs.add("himself");
		hs.add("his");
		hs.add("how");
		hs.add("how's");
		hs.add("i");
		hs.add("id");
		hs.add("ill");
		hs.add("im");
		hs.add("ive");
		hs.add("if");
		hs.add("in");
		hs.add("into");
		hs.add("is");
		hs.add("isnt");
		hs.add("it");
		hs.add("its");
		hs.add("its");
		hs.add("itself");
		hs.add("lets");
		hs.add("me");
		hs.add("more");
		hs.add("most");
		hs.add("mustnt");
		hs.add("my");
		hs.add("myself");
		hs.add("no");
		hs.add("nor");
		hs.add("not");
		hs.add("of");
		hs.add("off");
		hs.add("on");
		hs.add("once");
		hs.add("only");
		hs.add("or");
		hs.add("other");
		hs.add("ought");
		hs.add("our");
		hs.add("ours");
		hs.add("ourselves");
		hs.add("out");
		hs.add("over");
		hs.add("own");
		hs.add("same");
		hs.add("shant");
		hs.add("she");
		hs.add("she'd");
		hs.add("she'll");
		hs.add("she's");
		hs.add("should");
		hs.add("shouldnt");
		hs.add("so");
		hs.add("some");
		hs.add("such");
		hs.add("than");
		hs.add("that");
		hs.add("thats");
		hs.add("the");
		hs.add("their");
		hs.add("theirs");
		hs.add("them");
		hs.add("themselves");
		hs.add("then");
		hs.add("there");
		hs.add("there's");
		hs.add("these");
		hs.add("they");
		hs.add("theyd");
		hs.add("theyll");
		hs.add("theyre");
		hs.add("theyve");
		hs.add("this");
		hs.add("those");
		hs.add("through");
		hs.add("to");
		hs.add("too");
		hs.add("under");
		hs.add("until");
		hs.add("up");
		hs.add("very");
		hs.add("was");
		hs.add("wasnt");
		hs.add("we");
		hs.add("wed");
		hs.add("well");
		hs.add("were");
		hs.add("weve");
		hs.add("were");
		hs.add("werent");
		hs.add("what");
		hs.add("whats");
		hs.add("when");
		hs.add("whens");
		hs.add("where");
		hs.add("where's");
		hs.add("which");
		hs.add("while");
		hs.add("who");
		hs.add("whos");
		hs.add("whom");
		hs.add("why");
		hs.add("whys");
		hs.add("with");
		hs.add("wont");
		hs.add("would");
		hs.add("wouldnt");
		hs.add("you");
		hs.add("youd");
		hs.add("youll");
		hs.add("youre");
		hs.add("youve");
		hs.add("your");
		hs.add("yours");
		hs.add("yourself");
		hs.add("yourselves");
		return hs;
	}
}
