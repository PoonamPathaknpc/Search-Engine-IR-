
import java.util.*;

public class LemmaT {
	String term = "";
	int tf = 0; //term frequency in the document collection
	int dF = 0; //number of documents where the lemma occurs
	//List of documents in which the token occurs in the collection and its frequency in that document
	Map<Integer, Integer> PostingF = new TreeMap<Integer, Integer>();
	
	public LemmaT(int df , int tf, String lemma) {
		this.dF = df;
		this.tf= tf;
		this.term = lemma;
		
	}

}


