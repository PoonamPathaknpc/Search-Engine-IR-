
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


/*
 * ******* Program Description**************
 * the program lemmatizes and creates index list as per document frequency and term frequency
 * 
 */

public class IndexConstruction {
	
	Properties propProperties = new Properties();
	StanfordCoreNLP scnLemmatizer;
	//Map<String, LemmaT> termIndex = new TreeMap<String, LemmaT>();
	

	public IndexConstruction() {
		// TODO Auto-generated constructor stub
		propProperties.put("annotators", "tokenize, ssplit, pos, lemma");
		scnLemmatizer = new StanfordCoreNLP(propProperties);
		
	}
	
	public String lemmatize(String stem)
	{
		String Lemma = null;
		ArrayList<String> stopwords = new ArrayList<String>(); 
		String[] sStopWords = {"a", "all", "an", "and", "any", "are", "as", "be", "been", "but", "by ", "few", "for", "have", "he", "her", "here", "him", "his", "how", "i", "in", "is", "it", "its", "many", "me", "my", "none", "of", "on ", "or", "our", "she", "some", "the", "their", "them", "there", "they", "that", "this", "us", "was", "what", "when", "where", "which", "who", "why", "will", "with", "you", "your"};
		//Ignoring all tokens of length less or or equal to 1

		for(int i=0; i<sStopWords.length;i++)	 	    
	 		  stopwords.add(sStopWords[i]);
	 	  
		   
		if(stem.length() > 1)
		{
			Annotation aAnnotation = scnLemmatizer.process(stem);
			
			for(CoreMap cmSentence: aAnnotation.get(SentencesAnnotation.class))
			{				
				for (CoreLabel clToken: cmSentence.get(TokensAnnotation.class)) 
				{					
					Lemma = clToken.get(LemmaAnnotation.class);
					Lemma = Lemma.toLowerCase();
					//System.out.println(sTempWord + "   " + sLemma);					
				}
			}
		}
		//System.out.println("the lemma is :: " + Lemma);
		if(!stopwords.contains(Lemma))               
        	return Lemma;
		else
			return null;
	}
		
	public void addToIndex(String Lemma, int DocId , int ver)
	{
		
		    if(ver==1)
		    {	
			//Now that we have the lemma - add it to index		
			if(IndexEngine.termIndexstem.containsKey(Lemma))
			{
				
				LemmaT post = IndexEngine.termIndexstem.get(Lemma); 
				post.tf = post.tf + 1;				
					
				
				if(!post.PostingF.containsKey(DocId))
				{	
					post.dF = post.dF + 1;					
					post.PostingF.put(DocId, 1);
				
				}
				
				else
				{
					// adding the freq  of term in the document..
					int OldF = post.PostingF.get(DocId);
					post.PostingF.put(DocId,OldF+1);
				
				}
				
				
				IndexEngine.termIndexstem.put(Lemma, post);	
				IndexEngine.tempIndexstem.put(Lemma, post);
			}
		
			else
			{
				
				//System.out.println("first time..");
				// adding to the term frequency tf and document frequency df
				LemmaT term = new LemmaT(1, 1,Lemma);
				term.PostingF.put(DocId, 1);
				IndexEngine.termIndexstem.put(Lemma, term);		
				IndexEngine.tempIndexstem.put(Lemma, term);
			}
			//System.out.println("added to index :: " + IndexEngine.termIndex.get(Lemma).term);
			
		    }
		    
		    
		    else
		    {
		    	//Now that we have the lemma - add it to index		
				if(IndexEngine.termIndex.containsKey(Lemma))
				{
					
					LemmaT post = IndexEngine.termIndex.get(Lemma); 
					post.tf = post.tf + 1;				
						
					
					if(!post.PostingF.containsKey(DocId))
					{	
						post.dF = post.dF + 1;					
						post.PostingF.put(DocId, 1);
					
					}
					
					else
					{
						// adding the freq  of term in the document..
						int OldF = post.PostingF.get(DocId);
						post.PostingF.put(DocId,OldF+1);
					
					}
					
					
					IndexEngine.termIndex.put(Lemma, post);	
					IndexEngine.tempIndex.put(Lemma, post);
				}
			
				else
				{
					
					//System.out.println("first time..");
					// adding to the term frequency tf and document frequency df
					LemmaT term = new LemmaT(1, 1,Lemma);
					term.PostingF.put(DocId, 1);
					IndexEngine.termIndex.put(Lemma, term);		
					IndexEngine.tempIndex.put(Lemma, term);
				}
				//System.out.println("added to index :: " + IndexEngine.termIndex.get(Lemma).term);
				
				
		    }
		    
	}
	
	
	
}
