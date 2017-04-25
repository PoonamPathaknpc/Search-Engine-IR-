import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;


public class RelevanceCalculation {	
	
	public IndexConstruction indexlist  = new IndexConstruction();
	public static HashMap<String,HashMap<Integer, Weight>>docweights = new HashMap<String , HashMap<Integer, Weight>>();
	public HashMap<String ,Integer>qwords = new HashMap<String ,Integer>();// query string and tf
	public HashMap<Integer ,Double> WSQValues = new HashMap<Integer ,Double>();// docid , cosine values
	public HashMap<Integer ,BigDecimal> CosineValues = new HashMap<Integer ,BigDecimal>();// docid , cosine values
	public String docpath = "E:\\documents\\UTDallasMasters\\Semester-2\\IR\\ppp160130_Assignment2\\";
	public HashMap<String , HashMap<Integer, BigDecimal>> RelevantDocs = new HashMap<String , HashMap<Integer, BigDecimal>>();
	public Double QuerySQ =0.0;

	public RelevanceCalculation() {
		
		
		/*
		 * **********Calculating Tdf-Idf as per the given metric for all the terms in documents...****************
		  
		 * W1 = (0.4 + 0.6 * log (tf + 0.5) / log (maxtf + 1.0))
         * (log (collectionsize / df)/ log (collectionsize))
         *  W2 = (0.4 + 0.6 * (tf / (tf + 0.5 + 1.5 *
         * (doclen / avgdoclen))) * log (collectionsize / df)/
         * log (collectionsize))
		 */
		
		for(Entry<String , TermDoc> Term : IndexEngine.UncompressedindexListV1.entrySet())
		{		 
		 TermDoc TermInfo = Term.getValue();	
		 int df = TermInfo.dF;
		 double idf = Math.log10((IndexEngine.Collectionsize/df));
		 int tf = TermInfo.tf;
		 HashMap<Integer,Weight> weights12 = new HashMap<Integer,Weight>();
		
		 for(Entry<Document , Integer> posting : TermInfo.PostingF.entrySet())
		  {
			Integer docid = posting.getKey().docid;
			int doclen = posting.getKey().doclen;
			int max_tf = posting.getKey().max_tf;
			
			
			Double weight1 = (0.4 + 0.6 + Math.log10(tf + 0.5)/Math.log10(max_tf + 1.0))*
					(Math.log10(IndexEngine.Collectionsize/df)/Math.log10(IndexEngine.Collectionsize));
			
			Double TDF_IDF1 = weight1;
			
			
			Double weight2 = (0.4 + 0.6 + (tf / (tf + 0.5 + 1.5 * (doclen/IndexEngine.avgdoclen)))*
					Math.log10(IndexEngine.Collectionsize/df)/Math.log10(IndexEngine.Collectionsize));
			
			
			//System.out.println(Term.getKey() + " w1 " + weight2 + " tf " + tf);
			 
			Double TDF_IDF2 = weight2;
			
			
			Weight w= new Weight(TDF_IDF1 , TDF_IDF2);
			weights12.put(docid, w);
			
		  }
		 
		 docweights.put(Term.getKey(), weights12);
		 
		 
		} 
        
		
	}
	
	public void QueryCalculation( String Query, int flag, BufferedWriter brfw)
	{
		// Tokenizing  and Lemmatizing the query words - filtering out the stop words...
	try{
			
		
		String[] tokens = Query.split(" |\t");
		for(int i=0;i<tokens.length;i++)
		{
			String token = Tokenize(tokens[i]);
			split(token);
		}
		
		if(flag==0)
		{
		brfw.write("Query: " +  Query);
		brfw.newLine();
		}	
	
		Query tempQuery = new Query();
		tempQuery.dQueryLen = qwords.size();
		tempQuery.dAvgQueryLen = Query.length()/qwords.size();
		
		List<Map.Entry<String,Integer>> queries = new LinkedList<Map.Entry<String,Integer>>( qwords.entrySet() );
	    Collections.sort( queries, new Comparator<Map.Entry<String,Integer>>()
	        {
	           public int compare( Map.Entry<String,Integer> o1, Map.Entry<String,Integer> o2 )
	            {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	            
	        } );
		
		tempQuery.dMaxFreqLemmaFrequency = queries.get(0).getValue();		
		
		/* Calculate the weights of query lemmas...
		 * Calculate the normalized weight of Query lemma..
		 * Calculate the normalized weight of document lemmas.. 
		 */
		for(Entry<String , Integer> queryw : qwords.entrySet())
		  {
			int tf = queryw.getValue();
			int maxtf = tempQuery.dMaxFreqLemmaFrequency;
			int doclen = tempQuery.dQueryLen;
			double avgdoclen = tempQuery.dAvgQueryLen;
			
			// As per the design decision .. we assume idf =1 and log(collection size =1) for queries...
			
			double weight1 = (0.4 + 0.6 + Math.log10(tf + 0.5)/Math.log10(maxtf + 1.0));					
			double weight2 = (0.4 + 0.6 * (tf / (tf + 0.5 + 1.5 * (doclen / avgdoclen))));				
			tempQuery.addWeight(queryw.getKey(), weight1,weight2);	
		
			// printing the Query Lemmas..
			if(flag==0)
			{
			brfw.write("Lemmas::");
			brfw.newLine();			
			brfw.write("Lemma: " + queryw.getKey());
			brfw.newLine();
			brfw.write("Max TF Term Weight (Un-normalized): " + weight1);
			brfw.newLine();
			brfw.write("Okapi Weight (Un-normalized): " + weight2);
			brfw.newLine();
			}
			
			
			//Calculating the normalized value of query lemmas..
			if(flag==0)
			   QuerySQ+=Math.pow(weight1, 2);  // calculating the square value	
			else
			   QuerySQ+=Math.pow(weight2, 2);

			//QuerySQ+= weight1*weight2;
			
			// Calculating the weights for Document lemmas....
			if(!docweights.containsKey(queryw.getKey()))
				continue;//System.out.println("the word " + queryw.getKey() + " is not present in the collection");
			else
			{				
			
			 HashMap<Integer, Weight> weights = docweights.get(queryw.getKey());		
			 
			 // Calculating normalized values for document lemmas..
			 for(Entry<Integer, Weight> weightsvalue : weights.entrySet())
			  {
				if(flag==0){
					
				    //Q="Weight 1: ";					
					if(WSQValues.containsKey(weightsvalue.getKey()))							
						WSQValues.put(weightsvalue.getKey(), WSQValues.get(weightsvalue.getKey()) + Math.pow(weightsvalue.getValue().doc_weightW1,2));
					else
						WSQValues.put(weightsvalue.getKey(), Math.pow(weightsvalue.getValue().doc_weightW1,2));	
					
				
				}
				
				else{
					
				    //Q="Weight 2: ";	
				
					if(WSQValues.containsKey(weightsvalue.getKey()))							
						WSQValues.put(weightsvalue.getKey(), WSQValues.get(weightsvalue.getKey()) + Math.pow(weightsvalue.getValue().doc_weightW2,2));
					else
						WSQValues.put(weightsvalue.getKey(), Math.pow(weightsvalue.getValue().doc_weightW2,2));	
					
				  
				}
			}
					
		  }
		
		}		
		// Calculating the final cosine values...
		for(Entry<String , Integer> queryw : qwords.entrySet())
		  {
			
			if(!docweights.containsKey(queryw.getKey()))
				continue;//System.out.println("the word " + queryw.getKey() + " is not present in the collection");
			else
			{				
			 String lemma = queryw.getKey();
			 HashMap<Integer, Weight> weights = docweights.get(lemma);		
			
			
			 for(Entry<Integer, Weight> weightsvalue : weights.entrySet())
			  {
				if(flag==0){
				
					double cosineval1 = weightsvalue.getValue().doc_weightW1*tempQuery.w.get(lemma).weight1;
					cosineval1 = cosineval1 / Math.sqrt(QuerySQ);
					
					BigDecimal cosineval;
					if(WSQValues.get(weightsvalue.getKey()).intValue()!=0)
					   {
					   BigDecimal c = new BigDecimal(Math.sqrt(WSQValues.get(weightsvalue.getKey())));
					   cosineval = new BigDecimal(cosineval1).divide(c, RoundingMode.DOWN);
					   cosineval = cosineval.setScale(10, RoundingMode.HALF_DOWN);
					   
					   }
					else
					   {						
						cosineval = new BigDecimal(0);
                       }
					
					
					
					
					if(CosineValues.containsKey(weightsvalue.getKey()))							
						CosineValues.put(weightsvalue.getKey(), CosineValues.get(weightsvalue.getKey()).add(cosineval));
					else
						CosineValues.put(weightsvalue.getKey(), cosineval);
				
				
				}
				
				else{	
					
					double cosineval2 = weightsvalue.getValue().doc_weightW2*tempQuery.w.get(lemma).weight2;
					cosineval2 = cosineval2 / QuerySQ;
					//cosineval2 = cosineval2 /Math.sqrt(WSQValues.get(weightsvalue.getKey()));
					
					BigDecimal cosineval;
					if(!WSQValues.get(weightsvalue.getKey()).equals(0))
					   {
					   BigDecimal c = new BigDecimal(Math.sqrt(WSQValues.get(weightsvalue.getKey())));
					   cosineval = new BigDecimal(cosineval2).divide(c, RoundingMode.DOWN);
					   cosineval = cosineval.setScale(10, RoundingMode.HALF_DOWN);
					   }
					else
					   {
						//System.out.println("The term " + lemma + " appears in all docs");
						cosineval = new BigDecimal(0);
                       }
					
					
					if(CosineValues.containsKey(weightsvalue.getKey()))							
						CosineValues.put(weightsvalue.getKey(), CosineValues.get(weightsvalue.getKey()).add(cosineval));
					else
						CosineValues.put(weightsvalue.getKey(), cosineval);
					
				 
				  }
			   
			 }
			}	 
					
		  }
		  
		
	    
		List<Map.Entry<Integer,BigDecimal>> finalValues =
	            new LinkedList<Map.Entry<Integer,BigDecimal>>( CosineValues.entrySet() );
	        Collections.sort( finalValues, new Comparator<Map.Entry<Integer,BigDecimal>>()
	        {
	            public int compare( Map.Entry<Integer,BigDecimal> o1, Map.Entry<Integer,BigDecimal> o2 )
	            {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );

	        brfw.newLine();
	        
	        if(flag==0)
	         {brfw.write("Top Ranked documents as per Max_TF weighting:");
	         //System.out.println("Top Ranked documents as per Max_TF weighting:");
	         }
	        else
	         {brfw.write("Top Ranked documents as per OKAPI term weighting:");
	        // System.out.println("Top Ranked documents as per OKAPI term weighting:");
	         }
	        
			brfw.newLine();			 
			int i=0;
			
	        
	        for (Map.Entry<Integer,BigDecimal> entry : finalValues)	
	        {	
	        	if(i<5) 
	        	{
	        		
	        		
	        		brfw.write("Rank# " + (i+1) + ":" );
	        		//System.out.println("Rank# " + (i+1) + ":" );
	        		brfw.newLine();
	        		brfw.write("Cosine Similarity: " + entry.getValue());
	        		//System.out.println("Cosine Similarity: " + entry.getValue());
	        		brfw.newLine();	        		
	        		brfw.write("Doc#" + entry.getKey() + "  - Document Headline: " + IndexEngine.docHeadline.get(entry.getKey()));	
	        		//System.out.println("Doc#" + entry.getKey() + "  - Document Headline: " + IndexEngine.docHeadline.get(entry.getKey()));	     
	        		brfw.newLine();
	        		brfw.write("Document Vector: ");
					brfw.newLine();
					
					if(flag==0)
					{	
					for(Entry<String , Integer> queryw : qwords.entrySet())
					  {
						brfw.write(" Lemma: " + queryw.getKey());
						brfw.newLine();
						if(docweights.containsKey(queryw.getKey()))
						 {
							if(docweights.get(queryw.getKey()).containsKey(entry.getKey()))
							 {	
						      Weight w = docweights.get(queryw.getKey()).get(entry.getKey());						
						      brfw.write("Max TF Term Weight (Un-normalized):" + w.doc_weightW1);
							 }
							else
								brfw.write("Max TF Term Weight (Un-normalized):" + 0.00);	
						 }
						else
						 brfw.write("Max TF Term Weight (Un-normalized):" + 0.00);
						brfw.newLine();
					  }
					}
					else
					{
						for(Entry<String , Integer> queryw : qwords.entrySet())
						  {
							brfw.write("Lemma: " + queryw.getKey());
							brfw.newLine();
							if(docweights.containsKey(queryw.getKey()))
							 {
								if(docweights.get(queryw.getKey()).containsKey(entry.getKey()))
								 {
							      Weight w = docweights.get(queryw.getKey()).get(entry.getKey());						
							      brfw.write("OKAPI Term Weight (Un-normalized):" + w.doc_weightW2);
							     
								 }
								else
									brfw.write("OKAPI Term Weight (Un-normalized):" + 0.00);
							 }
							else
							 brfw.write("OKAPI Term Weight (Un-normalized):" + 0.00);
							
							brfw.newLine();
						  }
					}
						
					
					brfw.newLine();	
					
	        	}
				 else
				   break;
				i++;
	        	
	        }
	        
	        if(Query.equals("what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft"));

	        {	RelevanceFeedback_Rochio relevance = new RelevanceFeedback_Rochio();	    		
	    		HashMap<Integer,BigDecimal> fValues = new HashMap<Integer,BigDecimal>();
	    		fValues.put(485, new BigDecimal(0.72));
	    		fValues.put(573, new BigDecimal(0.6667));
	    		fValues.put(51, new BigDecimal(0.6604));
	    		fValues.put(14, new BigDecimal(0.6584));
	    		fValues.put(1268, new BigDecimal(0.6567));
	    		fValues.put(329, new BigDecimal(0.6556));
	    		fValues.put(184, new BigDecimal(0.5990));
	    		fValues.put(878, new BigDecimal(0.5935));
	    		fValues.put(195, new BigDecimal(0.5932));
	    		fValues.put(576, new BigDecimal(0.5918));
	    		fValues.put(78, new BigDecimal(0.5907));
	    		fValues.put(141, new BigDecimal(0.9055));
	    		fValues.put(172, new BigDecimal(0.9052));
	    		fValues.put(12,new BigDecimal(0.5889));
	    		fValues.put(13, new BigDecimal(0.5219));
	    		fValues.put(1180, new BigDecimal(0.51977));
	    		fValues.put(1255, new BigDecimal(0.51977));
	    		fValues.put(293, new BigDecimal(0.51977));
	    		fValues.put(92, new BigDecimal(0.51976));
	    		fValues.put(1218, new BigDecimal(0.51976));
	    		List<Map.Entry<String, BigDecimal>> Expqueries = relevance.getFeedback(Query,fValues,qwords);
	    		ReprocessQuery( Query,0, Expqueries);
	    		for(int j=0;j<2;j++)
	    		{    			
	    			Expqueries = relevance.getFeedback(Query,fValues,qwords);
	    			ReprocessQuery( Query,0, Expqueries);
	    		}
	    		

	            //RelevantDocs.put(Query , );
	    		
	       }
	        qwords.clear();
	        WSQValues.clear();
	        CosineValues.clear();
	        
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	        
	}
	
	
	public HashMap<Integer,BigDecimal> ReprocessQuery( String Query, int flag, List<Map.Entry<String, BigDecimal>> Exqueries)
	{
		HashMap<Integer,BigDecimal> fvalues = new HashMap<Integer,BigDecimal>();
		// Tokenizing  and Lemmatizing the query words - filtering out the stop words...
	try{			
		
		 double QuerySQ1 = 0.0;	
		/* Calculate the weights of query lemmas...
		 * Calculate the normalized weight of Query lemma..
		 * Calculate the normalized weight of document lemmas.. 
		 */
		Iterator<Entry<String, BigDecimal>> it = Exqueries.iterator();
		int k=0;
		while(it.hasNext())
		{			
			QuerySQ1+=it.next().getValue().pow(2).doubleValue();  // calculating the square value	
			
		}
			//QuerySQ+= weight1*weight2;
			
		Iterator<Entry<String, BigDecimal>> it1 = Exqueries.iterator();
		
		while(it1.hasNext())
		{
			String s = it1.next().getKey();
			 //System.out.println(s);
			// Calculating the weights for Document lemmas....
			if(!docweights.containsKey(s))
				continue;//System.out.println("the word " + queryw.getKey() + " is not present in the collection");
			else
			{				
			
			 
			 HashMap<Integer, Weight> weights = docweights.get(s);		
			 
			 // Calculating normalized values for document lemmas..
			 for(Entry<Integer, Weight> weightsvalue : weights.entrySet())
			  {
				if(flag==0){
					
				    //Q="Weight 1: ";					
					if(WSQValues.containsKey(weightsvalue.getKey()))							
						WSQValues.put(weightsvalue.getKey(), WSQValues.get(weightsvalue.getKey()) + Math.pow(weightsvalue.getValue().doc_weightW1,2));
					else
						WSQValues.put(weightsvalue.getKey(), Math.pow(weightsvalue.getValue().doc_weightW1,2));	
					
				
				}
				
				else{
					
				    //Q="Weight 2: ";	
				
					if(WSQValues.containsKey(weightsvalue.getKey()))							
						WSQValues.put(weightsvalue.getKey(), WSQValues.get(weightsvalue.getKey()) + Math.pow(weightsvalue.getValue().doc_weightW2,2));
					else
						WSQValues.put(weightsvalue.getKey(), Math.pow(weightsvalue.getValue().doc_weightW2,2));	
					
				  
				}
			}
					
		  }
		
		}		
		// Calculating the final cosine values...
       Iterator<Entry<String, BigDecimal>> it2 = Exqueries.iterator();
		
		while(it2.hasNext())
		{
			Entry<String, BigDecimal> lemma = it2.next();
			if(!docweights.containsKey(lemma.getKey()))
				continue;//System.out.println("the word " + queryw.getKey() + " is not present in the collection");
			else
			{				
			 HashMap<Integer, Weight> weights = docweights.get(lemma.getKey());			
			
			 for(Entry<Integer, Weight> weightsvalue : weights.entrySet())
			  {
					double cosineval1 = weightsvalue.getValue().doc_weightW1*lemma.getValue().doubleValue();
					cosineval1 = cosineval1 / Math.sqrt(QuerySQ1);
					
					BigDecimal cosineval;
					if(WSQValues.get(weightsvalue.getKey()).intValue()!=0)
					   {
					   BigDecimal c = new BigDecimal(Math.sqrt(WSQValues.get(weightsvalue.getKey())));
					   cosineval = new BigDecimal(cosineval1).divide(c, RoundingMode.DOWN);
					   cosineval = cosineval.setScale(10, RoundingMode.HALF_DOWN);
					   
					   }
					else
					   {						
						cosineval = new BigDecimal(0);
                       }
					if(CosineValues.containsKey(weightsvalue.getKey()))							
						CosineValues.put(weightsvalue.getKey(), CosineValues.get(weightsvalue.getKey()).add(cosineval));
					else
						CosineValues.put(weightsvalue.getKey(), cosineval);
				
				
				}
				
				
			 }
			}	
	    
		List<Map.Entry<Integer,BigDecimal>> finalValues =
	            new LinkedList<Map.Entry<Integer,BigDecimal>>( CosineValues.entrySet() );
	        Collections.sort( finalValues, new Comparator<Map.Entry<Integer,BigDecimal>>()
	        {
	            public int compare( Map.Entry<Integer,BigDecimal> o1, Map.Entry<Integer,BigDecimal> o2 )
	            {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        } );

	          System.out.println("Top Ranked documents as per Max_TF weighting:");
	        	int i=0;			
	        
	        for (Map.Entry<Integer,BigDecimal> entry : finalValues)	
	        {	
	        	if(i<20) 
	        	{
	        		fvalues.put(entry.getKey(), entry.getValue());
	        		System.out.println("Rank# " + (i+1) + ":" );	        		
	        		System.out.println("Cosine Similarity: " + entry.getValue());	        		
	        		System.out.println("Doc#" + entry.getKey() + "  - Document Headline: " + IndexEngine.docHeadline.get(entry.getKey()));	     
	        						
	        	}
				 else
				   break;
				i++;
	        	
	        }
	        
	        WSQValues.clear();
	        CosineValues.clear();
	        
	        
	        
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	   
	return fvalues;
	}
	
	public void processQuery(File queryFiles, BufferedWriter brfw)
	{
		
	  try {			
			
			FileInputStream queryFile = new FileInputStream(queryFiles);
			DataInputStream queryInput = new DataInputStream(queryFile);
			BufferedReader bufferedQueryInput = new BufferedReader(new InputStreamReader(queryInput));
			String inputString = null;
			int iQuery = 0;
			String sQuery = null;
			String sQuestion = "";
			inputString = bufferedQueryInput.readLine();

			while ((inputString) != null) {

				iQuery = iQuery + 1;
				//int length = (int)(Math.log10(iQuery)+1);
				sQuery = "Q" + Integer.toString(iQuery) + ":";
				
				if (inputString.equals(sQuery)) {

					sQuestion = "";	
					inputString = bufferedQueryInput.readLine();

					while (!inputString.contains("Q")) {

						
						sQuestion = sQuestion + inputString + " ";
						inputString = bufferedQueryInput.readLine();

						if (inputString == null) {

							break;

						}

					} 

					//System.out.println("Question:  " + sQuestion);
					QueryCalculation( sQuestion, 0,brfw);
					QueryCalculation( sQuestion, 1,brfw);
					
                    
				} 

			}
			
			bufferedQueryInput.close();

		} // End of try block
		catch (Exception e) {

			e.printStackTrace();

		} // End of catch block
		
	}
	
	
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
		  
		  //tokens[i] = tokens[i].replaceAll("\\.","");
	      return token;
		  
	}
	
	
	public void split(String token)
	{		
			 if(token.contains("-"))
				 {
					 for(int i=0;i<token.split("-").length;i++)										 
						 split(token.split("-")[i]);					
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
						   split(token.split(".")[i]);						 
					   }
					   
					 else // an Acronym needs to be added as whole
						 {
						// Convert all uppercase to lowercase .. Acronym
						 token = token.toLowerCase();
						 String lemma  = indexlist.lemmatize(token);
						 if(lemma != null  && !lemma.equals(":"))					       
							 if(qwords.containsKey(lemma))
								   qwords.put(lemma, qwords.get(lemma)+1);
							   else
						           qwords.put(lemma,1);	 
						 }
				
			     }
			 
			else if(token.contains(","))
			    { 
				   for(int i=0;i<token.split(",").length;i++)					 
					 split(token.split(",")[i]);
			     }
			
			else if(token.startsWith("non"))
			     {	
				   if(qwords.containsKey("non"))
					   qwords.put("non", qwords.get("non")+1);
				   else
					   qwords.put("non",1);
				   String lemma  = indexlist.lemmatize(token.substring(3));
				   if(lemma != null  && !lemma.equals(":"))
					   if(qwords.containsKey(lemma))
						   qwords.put(lemma, qwords.get(lemma)+1);
					   else
				           qwords.put(lemma,1);				   
			     }
			 
			else
			    {
				 
				 if(token.matches(".*\\d+.*"))				  								
					token = token.replaceAll("\\d","");
				  
				   if(token.length()>1)				
				     {
					   // Convert all uppercase to lowercase
					   token = token.toLowerCase();
					   String lemma  = indexlist.lemmatize(token);
					   if(lemma != null  && !lemma.equals(":"))
						   if(qwords.containsKey(lemma))
							   qwords.put(lemma, qwords.get(lemma)+1);
						   else
					           qwords.put(lemma,1);	
					   
				     }
			    }
		 
	   }
	

}
