

import java.io.Serializable;


public class Document implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// TODO Auto-generated constructor stub	
	        int docid ;
			int max_tf; //frequency of max occuring term in doc
			int doclen; //number of lemmas in the document
	
	//constructor 1
	public Document(int docID,int max_tf , int doclen ) {
		
				this.docid=docID;
				this.max_tf= max_tf; //frequency of max occuring term in doc
				this.doclen = doclen; //number of lemmas in the document
		
	}
	
	// Constructor 2
	public Document() {
		this.docid=0;
		this.max_tf=0;
	}
		
	public void setmax_tf(int d)
	{
		this.max_tf = d;
	}
	
	public void setdoclen(int d)
	{
		this.doclen = d;
	}
	
	public int getmax_tf()
	{
		return this.max_tf;
	}
	public int getdoclen()
	{
		return this.doclen;
	}
	

}
