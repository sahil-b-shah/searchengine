package indexer.storage;

public class URLMetrics {
	private int numOccurrences;
	private double tf;
	private double idf;
	
	public URLMetrics(int occur, int tf, int idf){
		this.numOccurrences = occur;
		this.tf = tf;
		this.idf = idf;
	}
	
	public int getOccurences(){
		return numOccurrences;
	}
	
	public double getTF(){
		return tf;
	}
	
	public double getIDF(){
		return idf;
	}
	
	public void setTF(double tf){
		this.tf = tf;
	}
	
	public void setIDF(double idf){
		this.idf = idf;
	}

}
