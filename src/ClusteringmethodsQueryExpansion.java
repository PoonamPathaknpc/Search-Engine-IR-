import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.ArrayStringFilter;
import edu.stanford.nlp.util.CoreMap;



public class ClusteringmethodsQueryExpansion {

	// three types of clustering methods for Psuedo relevance feedback..
	
	public int[][] M , trans_M ;
	public HashMap<Integer, Map<String, Integer>> documentWeights = new HashMap<Integer, Map<String,Integer>>();
	public HashMap<String ,Integer>qwords = new HashMap<String ,Integer>();// query string and tf
	public ArrayList<String> stems = new ArrayList<String>();
	public static HashMap<String ,List<String>>ExpandQ = new HashMap<String ,List<String>>();// Expanded Query.....
	public HashMap<String ,List<String>>Vocab = new HashMap<String ,List<String>>();// Vocabulory for stems.....
	Properties propProperties = new Properties();
	StanfordCoreNLP scnLemmatizer;
	ArrayList<String> tokens  = new ArrayList<>();
	
	
	public static void main(String[] args)
	{
		// creating list of document ids... and Query..
		ArrayList<Integer> docs = new ArrayList<Integer>();
		docs.add(2001);
		docs.add(2002);
		docs.add(2003);
		docs.add(2004);
		docs.add(2005);
		docs.add(2006);
		String Query = "Netherlands Dutch";
		
		// passing to clustering methods..
		ClusteringmethodsQueryExpansion cluster = new ClusteringmethodsQueryExpansion(docs); 		
		
		// calling Associative clustering
		cluster.associativeCluster(Query, docs); 
		System.out.println("Expanded Query with Associative clustering is::");
		for(Entry<String,List<String>> entry : ExpandQ.entrySet())
		    {
			System.out.println(entry.getKey() + "-- main word");
			Iterator<String> list = entry.getValue().iterator();
			while(list.hasNext())
				System.out.println(list.next());
		    }
		
		
		// calling Metric clustering
		/*cluster.MetricCluster(Query, docs); 		
		System.out.println("Expanded Query with Metric clustering is::");
		for(Entry<String,List<String>> entry : ExpandQ.entrySet())
		    {
			System.out.println(entry.getKey());
			Iterator<String> list = entry.getValue().iterator();
			while(list.hasNext())
				System.out.println(list.next());
		    }
		*/
		//cluster.ScalerCluster(Query, docs); // calling Scaler clustering
		
	}
	
	
	public ClusteringmethodsQueryExpansion(List<Integer>docs) {
		
		propProperties.put("annotators", "tokenize, ssplit, pos, lemma");
		scnLemmatizer = new StanfordCoreNLP(propProperties);
		
		
		// creating the indexing of docs based on the TF value for Lemma and creating the vocabulary...
		try{
		     File file = new File(".\\Relevant");
		     Iterator<Integer> it = docs.iterator();
				while(it.hasNext())
				{
					Integer docid = it.next();
					String docname = docid.toString() + ".html";
					//System.out.println(docname);					
					File document = new File(file.getName(),docname);					
					ArrayList<String> docwords = parseFile(document);			
					
					Iterator<String> it1 = docwords.iterator();
					while(it1.hasNext())
					{
						String word = Tokenize(it1.next());						
						tokens.clear();
						if(word!=null)
						{split(word,1);
						 Iterator<String> ittr = tokens.iterator();
						 while(ittr.hasNext())						
						 {
							// putting the frequency to document weights...
							String lemma = ittr.next();
							if(documentWeights.containsKey(docid))
						
						    {							   
							    Map<String,Integer> postings = documentWeights.get(docid);
							    if(postings.containsKey(lemma))
							    {
							    	int freq = postings.get(lemma) + 1;
							    	postings.put(lemma, freq);							    	
							    }
							    else
                                {
							    	postings.put(lemma, 1);
							    }
							    
							    documentWeights.put(docid, postings);	
							
						    }
						
						else
						   {
							Map<String,Integer> postings = new HashMap<String, Integer>();
							postings.put(lemma, 1);							
							documentWeights.put(docid, postings);
							
						    }
						  
						
						// Building the vocabulary
						   if(Vocab.containsKey(lemma))
							 {							   
								    List<String> postings = Vocab.get(lemma);
								    if(!postings.contains(word)) // the word isn't there in vocabulary .. add it 								    
								    	{postings.add(word);								    
								        Vocab.put(lemma, postings);	}								
							 }
							
							else
							{
								 List<String> postings = new ArrayList<String>();
								 postings.add(word);							
								 Vocab.put(lemma, postings);								
							}
						 }
					   }
				 }	
				}
		     
		}
		catch(Exception e){
			e.printStackTrace();
			
		}
		
	
	}
	
	
     /*
      * Creates the matrix and its transpose for top k ranked documents for clustering..
      */		
	private void createMatrix(List<Integer>docs)
	{
	  try{
		
		// consolidating the stems of all the documents...		
		Iterator<Integer> it = docs.iterator();		
		while(it.hasNext())
		{
			int docid = it.next();
			if(documentWeights.containsKey(docid))
			{
				Map<String,Integer> lemmas = documentWeights.get(docid);
				for(Entry<String,Integer> stem:lemmas.entrySet())
				{
					if(!stems.contains(stem.getKey()))  // to make sure there are no duplicate stems...
						stems.add(stem.getKey());
				}
				     
			}
			
			
			
		}		
		
		// creating the matrix of documents and stems
		M = new int[stems.size()][docs.size()];
        for (int i = 0; i < stems.size(); i++) {
            for (int j = 0; j < docs.size(); j++) {  
            	if(documentWeights.get(docs.get(j)).containsKey(stems.get(i)))
            	   M[i][j] = documentWeights.get(docs.get(j)).get(stems.get(i));    
            	else
            		M[i][j]=0;	            	
            	
            }
            
        }
        
        //Transpose of the Matrix
        trans_M = new int[docs.size()][stems.size()];
        for (int i = 0; i < docs.size(); i++) {
            for (int j = 0; j < stems.size(); j++) {            	
            	trans_M[i][j] = M[j][i];
            }
        }
        
       	
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        
        
	}
	
	/*
	 *  Expand Query using Associative clustering ...
	 */
	public void associativeCluster(String Query , List<Integer>docs)
	{
		
		ExpandQ.clear();
		// process the query ..		
		String[] tokens = Query.split(" |\t");
		for(int i=0;i<tokens.length;i++)
		{
			String token = Tokenize(tokens[i]);
			split(token,0);
		}
		
		
		// call the matrix creation method..
		createMatrix(docs);
		
		//get cuv value for..M and transM
		   int rowsM = M.length;
	       int columnsM = M[0].length; // same as rows in B
	       int columnstransM = trans_M[0].length;
	       int[][] CUV = new int[rowsM][columnstransM];
	       for (int i = 0; i < rowsM; i++) {
	           for (int j = 0; j < columnstransM; j++) {
	               for (int k = 0; k < columnsM; k++) {
	            	   CUV[i][j] = CUV[i][j] + M[i][k] * trans_M[k][j];
	               }
	               
	           }
	       }
		
	       // Calculate suv's....
	       float[][] SUV = new float[CUV.length][CUV[0].length];
	       
	       for (int j = 0; j < CUV.length; j++) {
	    	   if(qwords.containsKey(stems.get(j)))
	    	   {
	    		System.out.println(stems.get(j) + "  query term");  
	    	    HashMap<String,Float> sortedQstem = new HashMap<String,Float>();
	    		
               for (int k = 0; k < CUV[0].length; k++)
               {            	   
            	   if(CUV[j][k]!=0)
            	    {
            	    SUV[j][k]= (float)CUV[j][k]/(CUV[j][j] + CUV[j][k] + CUV[k][k]);            	     
            	    //System.out.println(SUV[j][k] + "for stems" + stems.get(j) + " " +  stems.get(k)); 
            	    }
            	   
            	   else
            		 SUV[j][k]=(float) 0.0;             	   
            	   
    			   sortedQstem.put(stems.get(k), SUV[j][k]);
    		    
               }
	            
               SortedSet<Map.Entry<String,Float>> Set = entriesSortedByValues(sortedQstem);
               int k=0;
    		   List<String> synonyms = new ArrayList<String>();
    		   Iterator<Entry<String,Float>> result = Set.iterator();
    		   while(result.hasNext())
    		   {
    			   Entry<String,Float> value = result.next();
    			   if(k<20)
    			   {
    				   //System.out.println("Stem " + value.getKey() + " with value  " + value.getValue());
    				   synonyms.add(value.getKey());
    			   }
    			   else
    				   break;
    			   
    			   k++;			    				   
    		   }
    		       		   
    		   ExpandQ.put(stems.get(j), synonyms);
               
	    	   }
				
               
	       }
		
	}


	/*
	 * Expand the query through metric clustering
	 */
	public void MetricCluster(String Query , List<Integer>docs)
	{
		      ExpandQ.clear();		
		      // process the query ..		
				String[] tokens = Query.split(" |\t");
				for(int i=0;i<tokens.length;i++)
				{
					String token = Tokenize(tokens[i]);
					split(token,0);
				}
				
								
				// call the matrix creation method..
				createMatrix(docs);
				
				//get cuv value for..M and transM
				   int rowsM = M.length;
			       int columnsM = M[0].length; // same as rows in B
			       int columnstransM = trans_M[0].length;
			       float[][] SUV = new float[rowsM][columnstransM]; // directly calculating the normalized value..
			       for (int i = 0; i < rowsM; i++) {
			           for (int j = 0; j < columnstransM; j++) {
			        	   String stemi = stems.get(i);
		            	   String stemj = stems.get(j);
		            	    
			               for (int k = 0; k < columnsM; k++) {            	   
			            	    String docname = docs.get(k).toString() + ".html";	
			            	    if(CalcDistanceVocab(stemi, stemj,docname)!=0)
			            	      SUV[i][j] = SUV[i][j] + 1/CalcDistanceVocab(stemi, stemj,docname);		            	    
			            	    	
			            	    }
			               
			               SUV[i][j] = SUV[i][j]/(Vocab.get(stemi).size()*Vocab.get(stemj).size()); // normalizing the value..
			               
			           }
			       }
				  
			       
			       
			       // get stems as per top n SUV's... n=3 for test	      
			       for(int i=0;i<SUV.length;i++)
			       {
			    	   if(qwords.containsKey(stems.get(i)))
			    	   {
			    		   TreeMap<String,Float> sortedQstem = new TreeMap<String,Float>();
			    		   for(int j=0;j<SUV[0].length;j--)
			    		   {
			    			   sortedQstem.put(stems.get(j), SUV[i][j]);
			    		   }
			    		   
			    		   int k=0;
			    		   List<String> synonyms = new ArrayList<String>();
			    		   for(Entry<String,Float> value:  sortedQstem.entrySet())
			    		   {
			    			   if(k<3)
			    			   {
			    				   System.out.println("Stem " + value.getKey() + " with value  " + value.getValue());
			    				   synonyms.add(value.getKey());
			    			   }
			    			   else
			    				   break;
			    			   
			    			   k++;			    				   
			    		   }
			    		   
			    		   
			    		   ExpandQ.put(stems.get(i), synonyms);
			    		   
			            }
			       }  
			    
			       
		
	}
	
	
	/*
	 * Calculates the distance between two keywords from vocabulary of two stems 
	 * for metric clustering
	 * Assumption : the distance calculated only with first occurence of both the keywords is considered..
	 */
	
	public int CalcDistanceVocab(String stemi, String stemj, String docname)
	{
		int distance=0;
		try{
		File file = new File(".\\Relevant");
		List<String> vocabei = Vocab.get(stemi);
		List<String> vocabej = Vocab.get(stemj);
		
		
		for(int i=0;i<vocabei.size();i++)
		{
			for(int j=0;j<vocabej.size();j++)
			{
				int flag=0;
				File document = new File(file.getName(),docname);	
				FileReader fr = new FileReader(document);
                BufferedReader br = new BufferedReader(fr);
                String line;
                int counter = 3,localdist=0;
                
                outerloop:
                while ((line = br.readLine()) != null) {
                    if (counter > 0) {
                        counter--;
                        continue;
                    }
                    line = line.replaceAll("\\t+", " ");
                    line = line.replaceAll("[^\\p{ASCII}]", "");

                    String[] words = line.split(" ");
                    for (String word : words) {
                    
                    	if(word.equals(stemi))
                    	{
                    		if(flag==0)// calculation hasnt started yet..
                    		{
                    			localdist++;   //start calculation from stemi...
                    			flag=1;
                    		}
                    		else if(flag==2) // total distance is calculated...
                    		{
                    			break outerloop;
                    		}
                    		
                    		else // reset the local distance to 1..
                    			localdist=1;
                    	}
                    	
                    	else if(word.equals(stemj))
                    	{
                    		if(flag==0)// calculation hasn't started yet..
                    		{
                    			localdist++; // start calculation from stemj  
                    			flag=2;
                    		}
                    		else if(flag==1) // total distance is calculated...
                    		{
                    			break outerloop;
                    		}
                    		
                    		else // reset the local distance to 1..
                    			localdist=1;
                    	}
                    	
                    	else
                    	{
                    		if(flag!=0)
                    			localdist++;  // continue calculating the distance...
                    	}
                    }
				
                }
                
                
                distance+=localdist;
			}
		}
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		System.out.print(" the distance is : " + distance + " between stems " + stemi + "::" + stemj);
		return distance;
	}
		
	
	
	/*
	 * parsing the words in the document..used for metric clustering 
	 */	
	private ArrayList<String> parseFile(File f) { 		
		ArrayList<String> Words = new ArrayList<String>();
           try {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String line;
                int counter = 3;
                while ((line = br.readLine()) != null) {
                    if (counter > 0) {
                        counter--;
                        continue;
                    }
                    line = line.replaceAll("\\t+", " ");
                    line = line.replaceAll("[^\\p{ASCII}]", "");

                    String[] words = line.split(" ");
                    for (String word : words) {
                    	Words.add(word);
                    	
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		          
		      return Words;
            
        
    }
	
		
	/*
	 * Tokenizes the query words...
	 */
	public static String Tokenize(String token)
	{
		
		//stem characters [,)/] at the end....
	    while (token.endsWith(",") || token.endsWith(")") || token.endsWith("-"))
	 	   token = token.substring(0, token.length()-1);
	
	    //stem characters [(/] at the starting....
	     while (token.startsWith("("))							  
	       token = token.substring(1);
	     
	     // removing  Possessives
	     if(token.endsWith("'s"))
	      token = token.substring(0, token.length()-3);
	  
	     // Stemming the characters [/+=()']...
		  token = token.replaceAll("/","");
		  token = token.replaceAll("\\+","");
		  token = token.replaceAll("=","");
		  token = token.replaceAll("\\(","");	
		  token = token.replaceAll("\\)","");
		  token = token.replaceAll("'","");
		  token = token.replaceAll("\\?","");
		  token = token.replaceAll("\\*","");
		  token = token.replaceAll("\\%","");
		  token = token.replaceAll("\\!","");
		  
		  if(token.length()==1 || token.matches("-?\\d+(\\.\\d+)?") || token.equals(""))
			  return null;
		  else		  
	         return token;
		  
	}
	
	
	/*
	 * Splits the words..
	 */
	public void split(String token,int flag1)
	{		
		  
			 if(token.contains("-"))
				 {
					 for(int i=0;i<token.split("-").length;i++)										 
						 split(token.split("-")[i],flag1);					
		         }
			
			 // Handling Acronyms with . in between...
			else if (token.contains("."))
			     {
				   int flag=0;
				   String[] acr = token.split("."); 
				   
				   // Checking for Acronyms...
				   for(int i=0;i<acr.length;i++)					 
					 {					   
					   if(acr[i].length()==1 && Character.isUpperCase(acr[i].charAt(0)))
						 flag=1;
					   else
						 flag=0;						   
					  }	   
						
				     if(flag==0) // not an acronym...
				       {
				    	 for(int i=0;i<acr.length;i++)
						   split(token.split(".")[i],flag1);						 
					   }
					   
					 else // an Acronym needs to be added as whole
						 {
						// Convert all uppercase to lowercase .. Acronym
						 token = token.toLowerCase();
						 String lemma  = lemmatize(token);
						 if(lemma != null  && !lemma.equals(":"))					       
							 {
							   if(flag1==0)
							    {if(qwords.containsKey(lemma))
								   qwords.put(lemma, qwords.get(lemma)+1);
							      else
						           qwords.put(lemma,1);}
							   else
								   tokens.add(lemma);
						    }
						 }
				
			     }
			 
			else if(token.contains(","))
			    { 
				   for(int i=0;i<token.split(",").length;i++)					 
					 split(token.split(",")[i],flag1);
			     }
			
			else if(token.startsWith("non"))
			     {	
				   if(qwords.containsKey("non"))
					   qwords.put("non", qwords.get("non")+1);
				   else
					   qwords.put("non",1);
				   String lemma = lemmatize(token.substring(3));
				   
				   if(lemma != null  && !lemma.equals(":"))
				   { 					   
					 if(flag1==0)
					   if(qwords.containsKey(lemma))
				   
						   qwords.put(lemma, qwords.get(lemma)+1);
					   else
				           qwords.put(lemma,1);	
				   
				     else
					   tokens.add(lemma);
				   }
			     }
			 
			else
			    {
				 
				 if(token.matches(".*\\d+.*"))				  								
					token = token.replaceAll("\\d","");
				  
				   if(token.length()>1)				
				     {
					   // Convert all uppercase to lowercase
					   token = token.toLowerCase();
					   String lemma  = lemmatize(token);
					   if(lemma != null  && !lemma.equals(":"))
					   { 					   
						 if(flag1==0)
						   if(qwords.containsKey(lemma))
							   qwords.put(lemma, qwords.get(lemma)+1);
						   else
					           qwords.put(lemma,1);	
						 else
							tokens.add(lemma); 
					   
				        }
				     }
			    }
		 
	   }
	
	/*
	 * Lemmatizes  the words to stems/lemmas	 * 
	 */
	public String lemmatize(String stem)
	{
		String Lemma = null;
		ArrayList<String> stopwords = new ArrayList<String>(); 
		String[] sStopWords = {"a", "all", "an", "and", "any", "are", "as", "be", "been", "but", "by ","on", "few", "for", "have", "he", "her", "here", "him", "his", "how", "i", "in", "is", "it", "its", "many", "me", "my", "none", "of", "on ", "or", "our", "she", "some", "the", "their", "them", "these", "there", "they", "that", "this", "us", "was", "what", "when", "where", "which", "who", "why", "will", "with", "you", "your"};
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


	
	static <String,Float extends Comparable<? super Float>>
	SortedSet<Map.Entry<String,Float>> entriesSortedByValues(Map<String,Float> map) {
	    SortedSet<Map.Entry<String,Float>> sortedEntries = new TreeSet<Map.Entry<String,Float>>(
	        new Comparator<Map.Entry<String,Float>>() {
	            @Override public int compare(Map.Entry<String,Float> e1, Map.Entry<String,Float> e2) {
	                int res = e2.getValue().compareTo(e1.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	

}
