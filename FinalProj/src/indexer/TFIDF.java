package indexer;

import java.util.HashSet;

import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.WordCountDBWrapper;

public class TFIDF {

	public static void main(String args[]){
		String indexDirectory = args[0];
		String maxDirectory = args[1];
		InvertedIndexDBWrapper indexDB = InvertedIndexDBWrapper.getInstance(indexDirectory);
		WordCountDBWrapper maxOccurences = WordCountDBWrapper.getInstance(maxDirectory);
		indexDB.initIterator();
		
		Thread threadpool[] = new Thread[10];
		HashSet<String> stopWords = new HashSet<String>();
		stopWords = fillHash(stopWords);
        for(int i=0; i<threadpool.length; i++){
        	threadpool[i] = (new TFIDFThread(indexDB,maxOccurences, stopWords));
        }
        
        for(int i=0; i<threadpool.length; i++){
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
            threadpool[i].start();
        }
        
        
		//Wait until all threads done
        for(int i=0; i<threadpool.length; i++){
			try {
				threadpool[i].join();
			} catch (InterruptedException e) {
				System.err.println("Index thread ended unnaturally");
			}
		}
        
        System.out.println("TF/IDF done");
        indexDB.closeIterator();;
		indexDB.close();
		maxOccurences.close();
		
	}
	
	public static HashSet<String> fillHash(HashSet<String> hs){
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
		hs.add("aren't");
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
		hs.add("can't");
		hs.add("cannot");
		hs.add("could");
		hs.add("couldn't");
		hs.add("did");
		hs.add("didn't");
		hs.add("do");
		hs.add("does");
		hs.add("doesn't");
		hs.add("doing");
		hs.add("don't");
		hs.add("down");
		hs.add("during");
		hs.add("each");
		hs.add("few");
		hs.add("for");
		hs.add("from");
		hs.add("further");
		hs.add("had");
		hs.add("hadn't");
		hs.add("has");
		hs.add("hasn't");
		hs.add("have");
		hs.add("haven't");
		hs.add("having");
		hs.add("he");
		hs.add("he'd");
		hs.add("he'll");
		hs.add("he's");
		hs.add("her");
		hs.add("here");
		hs.add("here's");
		hs.add("hers");
		hs.add("herself");
		hs.add("him");
		hs.add("himself");
		hs.add("his");
		hs.add("how");
		hs.add("how's");
		hs.add("i");
		hs.add("i'd");
		hs.add("i'll");
		hs.add("i'm");
		hs.add("i've");
		hs.add("if");
		hs.add("in");
		hs.add("into");
		hs.add("is");
		hs.add("isn't");
		hs.add("it");
		hs.add("it's");
		hs.add("its");
		hs.add("itself");
		hs.add("let's");
		hs.add("me");
		hs.add("more");
		hs.add("most");
		hs.add("mustn't");
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
		hs.add("shan't");
		hs.add("she");
		hs.add("she'd");
		hs.add("she'll");
		hs.add("she's");
		hs.add("should");
		hs.add("shouldn't");
		hs.add("so");
		hs.add("some");
		hs.add("such");
		hs.add("than");
		hs.add("that");
		hs.add("that's");
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
		hs.add("they'd");
		hs.add("they'll");
		hs.add("they're");
		hs.add("they've");
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
		hs.add("wasn't");
		hs.add("we");
		hs.add("we'd");
		hs.add("we'll");
		hs.add("we're");
		hs.add("we've");
		hs.add("were");
		hs.add("weren't");
		hs.add("what");
		hs.add("what's");
		hs.add("when");
		hs.add("when's");
		hs.add("where");
		hs.add("where's");
		hs.add("which");
		hs.add("while");
		hs.add("who");
		hs.add("who's");
		hs.add("whom");
		hs.add("why");
		hs.add("why's");
		hs.add("with");
		hs.add("won't");
		hs.add("would");
		hs.add("wouldn't");
		hs.add("you");
		hs.add("you'd");
		hs.add("you'll");
		hs.add("you're");
		hs.add("you've");
		hs.add("your");
		hs.add("yours");
		hs.add("yourself");
		hs.add("yourselves");
		return hs;
	}
}
