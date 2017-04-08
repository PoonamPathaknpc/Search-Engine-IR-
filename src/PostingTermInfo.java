import java.util.ArrayList;

public class PostingTermInfo {
	
	byte[] TermFreq; // total occurrence of the term in Cranfield collection
	byte[] DocFreq; // total documents where the term appears..
	ArrayList<PostingFile> ps;
	
	public class PostingFile 
	{

		byte[] gap;// gap
		byte[] byFrequency; //Frequency in the document
		byte[] max_tf; //frequency of max occuring term in that document doc
		byte[] doclen; // total terms in the document
		
		
	}//End of class PostingFile


	public PostingTermInfo(byte[] TermFreq,	byte[] DocFreq) {
		// TODO Auto-generated constructor stub
		
		this.TermFreq = TermFreq;
		this.DocFreq = DocFreq;
		ps = new ArrayList<PostingFile>();
		
		
	}
	
	public void addPostingFile(byte[] byDocumentId, byte[] byFrequency, byte[] max_tf , byte[] doclen)
	{
		PostingFile ps1 = new PostingFile();
		ps1.gap = byDocumentId;
		ps1.byFrequency = byFrequency;
		ps1.max_tf = max_tf;
		ps1.doclen = doclen;
		this.ps.add(ps1);
	}

}

