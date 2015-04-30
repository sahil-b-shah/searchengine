package search;

import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.URLMetrics;
import indexer.storage.InvertedIndexData;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {

	static HashSet<String> stopList = new HashSet<String>();
	
	HashMap<String, SearchData> searchMap = new HashMap<String, SearchData>();
	
	int wordsInQuery = 0;
	
	static {
        fillHash(stopList);
    }
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		InvertedIndexDBWrapper database = InvertedIndexDBWrapper.getInstance(getServletContext().getInitParameter("InvertedIndexDBstore"));
		String query = request.getParameter("search");
		String[] elements = query.split("\\s+");
		wordsInQuery = elements.length;
		PrintWriter out;
		
		try{
			out = response.getWriter();
			out.println("<html><body>");
			out.println("<h1>You searched for:" + query +  "</h1>");
			out.println("<h2>Here are your results:</h2>");
			for(String token : elements){
				if(stopList.contains(token)){
					//System.out.println("Ignoring:" + token);
				}else{
					InvertedIndexData data;
					if((data = database.getContentById(token)) != null){
						HashMap<String, URLMetrics> hm = data.getUrls();
						for(String url : hm.keySet()){
							if(searchMap.containsKey(url)){
								System.out.println("Document Found Again:" + url);
								searchMap.get(url).incrementQueryHits();
							}else{
								SearchData newData = new SearchData(0.0,1);
								double tempTF = hm.get(url).getTF();
								double tempIDF = hm.get(url).getIDF();
								newData.setScore(tempTF * tempIDF);
								// Some way to set PageRank as well
								searchMap.put(url, newData);
							}
						}
					}else{
						System.out.println(token + " not in database");
					}
				}
			}
			int numFound = 0;
			while(wordsInQuery > 0){
				HashMap<String,Double> tempMap = new HashMap<String,Double>();
				for(String url : searchMap.keySet()){
					if(searchMap.get(url).getQueryHits() == wordsInQuery){
						tempMap.put(url, searchMap.get(url).getScore());
					}
				}
				wordsInQuery--;
				
				tempMap = sortMapByScore(tempMap);
				Iterator it = tempMap.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
					out.println("<h3>" + pair.getKey() +  "</h3>");
			        //System.out.println(wordsInQuery + ":" + pair.getKey() + " = " + pair.getValue());
			        //it.remove(); // avoids a ConcurrentModificationException
			    }
			}
			out.println("<form action='searchResults' method='POST'>");
	    	out.println("Try Another Search:<br>");
	    	out.println("<input type='text' name='search' value=''>");
	    	out.println("<br>");
	    	out.print("<input type='submit' value='Submit'>");
			out.println("</body></html>");
			database.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		InvertedIndexDBWrapper database = InvertedIndexDBWrapper.getInstance(getServletContext().getInitParameter("InvertedIndexDBstore"));
		PrintWriter out;
		try {
			// Adding stuff to Database
			HashMap<String,URLMetrics> hm1 = new HashMap<String,URLMetrics>();
			URLMetrics temp1 = new URLMetrics(100,1,3);
			URLMetrics temp2 = new URLMetrics(50,2,5);
			URLMetrics temp3 = new URLMetrics(75,5,2);
			URLMetrics temp4 = new URLMetrics(30,4,8);
			URLMetrics temp5 = new URLMetrics(44,3,2);
			
			hm1.put("Document1", temp1);
			hm1.put("Document2", temp2);
			hm1.put("Document8", temp3);
			database.addWord("carlie", hm1);
			
			// Adding stuff to Database
			HashMap<String,URLMetrics> hm2 = new HashMap<String,URLMetrics>();
			hm2.put("Document2", temp1);
			hm2.put("Document3", temp2);
			hm2.put("Document7", temp3);
			hm2.put("Document4", temp4);
			database.addWord("aadu", hm2);
			
			// Adding stuff to Database
			HashMap<String,URLMetrics> hm3 = new HashMap<String,URLMetrics>();
			hm3.put("Document2", temp1);
			hm3.put("Document3", temp2);
			hm3.put("Document1", temp3);
			hm3.put("Document7", temp4);
			hm3.put("Document4", temp5);
			database.addWord("vig", hm3);
			
			out = response.getWriter();
			out.println("<html><body>");
			//out.println("<h1> Search page </h1>");
	    	out.println("<form action='searchResults' method='POST'>");
	    	out.println("Search Terms:<br>");
	    	out.println("<input type='text' name='search' value=''>");
	    	out.println("<br>");
	    	out.print("<input type='submit' value='Submit'>");
	    	out.println("</body></html>");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String,Double> sortMapByScore(HashMap<String, Double> hm){
		List list = new LinkedList(hm.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
	        }
		});
	    HashMap<String, Double> sortedHashMap = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	    	Map.Entry entry = (Map.Entry) it.next();
	        sortedHashMap.put((String)entry.getKey(), (double) entry.getValue());
	    } 
	    return sortedHashMap;
	}
	
	public static void fillHash(HashSet<String> hs){
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
	}
}
