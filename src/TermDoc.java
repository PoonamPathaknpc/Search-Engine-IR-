import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class TermDoc {
	String term = "";
	int tf = 0; //term frequency in the document collection
	int dF = 0; //number of documents where the lemma occurs
	//List of documents in which the token occurs in the collection and its frequency in that document
	Map<Document, Integer> PostingF;
	
	public TermDoc() {
		// TODO Auto-generated constructor stub
	}

	
	public TermDoc(int df , int tf,String term) {
		this.dF = df;
		this.tf= tf;
		this.term = term;
		PostingF = new TreeMap<Document, Integer>(new Comparator<Document>()
	               {
	                  public int compare(Document D1, Document D2)
	                   {
	                       //comparison logic goes here
	                	  return new Integer(D1.docid).compareTo(new Integer(D2.docid)) ;
	                   } 
	});
		
	}
}
