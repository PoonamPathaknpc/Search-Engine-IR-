import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;



public class IndexEngine {
	
	static ArrayList<Integer> AvglistToken = new ArrayList<Integer>();
	static HashMap<String,Integer> map = new HashMap<String,Integer>();
	public static Map<String, LemmaT> termIndexstem = new TreeMap<String, LemmaT>();//version 1
	public static Map<String, LemmaT> tempIndexstem = new TreeMap<String, LemmaT>();
	public static Map<String, LemmaT> termIndex = new TreeMap<String, LemmaT>();//version2
	public static Map<String, LemmaT> tempIndex = new TreeMap<String, LemmaT>();	
	public static Map<Integer, String> docHeadline = new TreeMap<Integer, String>(); // doc headlines...
	public static Map<String,TermDoc> UncompressedindexListV1 = new TreeMap<String,TermDoc>();
	public static Map<String,TermDoc> UncompressedindexListV2 = new TreeMap<String,TermDoc>();
	public static Map<Integer, Document> documents = new TreeMap<Integer, Document>();
	public static long timeelapsed=0 ,timeelapsed1=0,timeelapsed2=0,timeelapsed3=0,timeelapsed4=0;
	public static int Collectionsize =0 , avgdoclen = 0;
	public static HashMap<String , HashMap<Integer, BigDecimal>> RelevantDocs = new HashMap<String , HashMap<Integer, BigDecimal>>();

	public IndexEngine() {}
	
	
	public static void main(String[] args) {
		
		
		IndexCompression indcompression = new IndexCompression();
		
		File file = new File(".");		
		File funcompV1 = new File(file.getName() , "UncompressedIndexV1.txt");
		File funcompV2 = new File(file.getName() ,"UncompressedIndexV2.txt");
		File fcompV1 = new File(file.getName() ,"CompressedIndexV1.txt");
		File fcompV2 = new File(file.getName() , "CompressedIndexV2.txt");
		File progdesc = new File(file.getName() ,"ProgramStatistics.txt");
		File Results = new File(file.getName() ,"RankedDocumentInformation.txt");
		
		File f  = new File("E:\\documents\\UTDallasMasters\\Semester-2\\IR\\ppp160130_Assignment2");
		File QueryFile  = new File("E:\\documents\\UTDallasMasters\\Semester-2\\IR\\ppp160130_Assignment3\\hw3.queries");
		//File f  = new File("//people//cs//s//sanda//cs6322");
		//File QueryFile  = new File("//people//cs//s//sanda//cs6322//hw3.queries");
		
		
		// call the process to tokenize, lemmatize and index the whole cranfield collection without compression..
		long starttime = System.currentTimeMillis();		
		processCranfield(f);		
		long endtime = System.currentTimeMillis();		
	    timeelapsed  = endtime - starttime;
	    
	    
		
		// printing uncompressed list version 1	    		
	    //WritetoFile(1,funcompV1);		
	    //System.out.println("Uncompressed version 1 generated");
	    
	    
		// printing uncompressed list version 2	    		
	    //WritetoFile(2,funcompV2);		
		//System.out.println("Uncompressed version 2 generated");
		
	    //compressing the index as per version1 and printing it..
		long starttime1 = System.currentTimeMillis();
	    indcompression.Compression(1,fcompV1);	   
	    long endtime1 = System.currentTimeMillis();
	    timeelapsed3  = endtime1 - starttime1;
	    System.out.println("compressed version 1 generated");
	    
	    
	    //compressing the index as per version2 and printing it...
	    long starttime2 = System.currentTimeMillis();
	    indcompression.Compression(2,fcompV2);
	    long endtime2 = System.currentTimeMillis();
	    timeelapsed4  = endtime2 - starttime2;
	    System.out.println("compressed version 2 generated");
	   
	    // Enter the Statistics..
	    FileWriter fw,rfw;
		try {
			
			rfw = new FileWriter(Results);
			BufferedWriter brfw = new BufferedWriter(rfw);
			RelevanceCalculation calc = new  RelevanceCalculation();	    
		    calc.processQuery(QueryFile,brfw);
		    
		    brfw.close();
		    
			fw = new FileWriter(progdesc);
			BufferedWriter bfw = new BufferedWriter(fw);
			bfw.write("*************Program Statistics************");
			bfw.newLine();
			bfw.newLine();
			//1
			bfw.write("1.Time Elapsed:");
			bfw.newLine();
			bfw.newLine();
			
			bfw.write("Total time taken to build both indexes ( including lemmatization/tokenization/stemming: ");		    	    
		    bfw.write(new Integer((int)timeelapsed).toString() + " milliseconds");
		    bfw.newLine();
			
			bfw.write("Time taken to generate index version1 uncompressed: ");		    	    
		    bfw.write(new Integer((int)timeelapsed1).toString()+ " milliseconds");
		    bfw.newLine();
		    
		    bfw.write("Time taken to generate index version2 uncompressed: ");		    	    
		    bfw.write(new Integer((int)timeelapsed2).toString()+ " milliseconds");
		    bfw.newLine();
		    
		    bfw.write("Time taken to generate index version1 compressed: ");		    
		    bfw.write(new Integer((int)timeelapsed3).toString() + " milliseconds");;
		    bfw.newLine();
		    
		    bfw.write("Time taken to generate index version2 compressed: ");		    	   
		    bfw.write(new Integer((int)timeelapsed4).toString() + " milliseconds");
		    bfw.newLine();
		    bfw.newLine();
			
		    //2
		    bfw.write("2. The size of the index files:");
		    bfw.newLine();
		    bfw.newLine();
		    
		    bfw.write("The size of the index Version 1 uncompressed (in bytes): ");	
		    bfw.write(new Integer((int)funcompV1.length()).toString() + " bytes");
		    bfw.newLine();
		    
		    bfw.write("The size of the index Version 2 uncompressed (in bytes): ");
		    bfw.write(new Integer((int)funcompV2.length()).toString()+ " bytes");
		    bfw.newLine();
		    
		    bfw.write("The size of the index Version 1 uncompressed (in bytes): ");
		    bfw.write((new Integer((int)fcompV1.length()).toString())+ " bytes");
		    bfw.newLine();
		    
		    bfw.write("The size of the index Version 2 compressed (in bytes): ");		      
		    bfw.write((new Integer((int)fcompV2.length()).toString()) + " bytes");
		    bfw.newLine();
		    bfw.newLine();

		    //3
		    bfw.write("3. the number of inverted lists");
		    bfw.newLine();
		    bfw.newLine();
		    
		    bfw.write("The number of inverted lists in version1 uncompressed: ");		   
		    bfw.write(new Integer(UncompressedindexListV1.size()).toString());
		    bfw.newLine();
		    
		    bfw.write("The number of inverted lists in version2 uncompressed: ");		    
		    bfw.write(new Integer(UncompressedindexListV2.size()).toString());
		    
		    bfw.newLine();
		    bfw.write("The number of inverted lists in version1 compressed: ");
		    bfw.write(new Integer(UncompressedindexListV1.size()).toString());
		    bfw.newLine();
		    
		    bfw.write("the number of inverted lists in version2 compressed: ");		    
		    bfw.write(new Integer(UncompressedindexListV2.size()).toString());
		    bfw.newLine();
		    bfw.newLine();
		   
		    //4
		    int size=0;
		    bfw.write("The df, tf, and inverted list length (in bytes) for the terms: ");
		    bfw.newLine();
		    bfw.newLine();
		    bfw.write("Lemma: ");
		    bfw.newLine();
		    
		    //Reynolds
		    bfw.write("Term:Reynolds: ");		    
		    bfw.write("df: "); 
		    bfw.write((new Integer(UncompressedindexListV1.get("reynold").dF).toString())+ ", "); 
		    bfw.write("tf: ");
		    bfw.write(new Integer(UncompressedindexListV1.get("reynold").tf).toString()+ ", ");
		    bfw.write("Inverted List Length: ");
		    for(Entry<Document,Integer> tc : UncompressedindexListV1.get("reynold").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+=out.size();
			         	         
			   } 
		    bfw.write(new Integer(size).toString());
		    bfw.newLine();
		    
		    //NASA
		    
		    bfw.write("Term:NASA: "); 
		    bfw.write("df: "); 
		    bfw.write(new Integer(UncompressedindexListV1.get("nasa").dF).toString()+ ", ");		    
		    bfw.write("tf: ");
		    bfw.write(new Integer(UncompressedindexListV1.get("nasa").tf).toString()+ ", ");		    
		    bfw.write("Inverted List Length: ");
		    
		    for(Entry<Document,Integer> tc : UncompressedindexListV1.get("nasa").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+=out.size();
			         	         
			   } 
		    bfw.write(new Integer(size).toString());
		    bfw.newLine();
		    
		    //prandtl
		    bfw.write("Term:Prandtl: "); 
		    if(UncompressedindexListV1.containsKey("prandtl"))
		    {
		     bfw.write("df: "); 
		     bfw.write(new Integer(UncompressedindexListV1.get("prandtl").dF).toString() + ", ");		     
		     bfw.write("tf: ");
		     bfw.write(new Integer(UncompressedindexListV1.get("prandtl").tf).toString()+ ", ");		     
		     bfw.write("Inverted List Length: ");
		     for(Entry<Document,Integer> tc : UncompressedindexListV1.get("prandtl").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+= out.size();
		          
			         	         
			   } 
		     bfw.write(new Integer(size).toString());
		     
		    }
		    
		    //flow
		    bfw.newLine();
            bfw.write("Term:flow: ");            
            if(UncompressedindexListV1.containsKey("flow"))
            {
            bfw.write("df: ");            
            bfw.write(new Integer(UncompressedindexListV1.get("flow").dF).toString()+ ", ");            
		    bfw.write("tf: ");
		    bfw.write(new Integer(UncompressedindexListV1.get("flow").tf).toString()+ ", ");		   
		    bfw.write("Inverted List Length: ");
		    for(Entry<Document,Integer> tc : UncompressedindexListV1.get("flow").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+= out.size();
			         	         
			   } 
	        bfw.write(new Integer(size).toString());
            }
            
            
            //pressure
            bfw.newLine();
		    bfw.write("Term:pressure: ");		    
		    if(UncompressedindexListV1.containsKey("pressure"))
		    {
		     bfw.write("df: "); 
		     bfw.write(new Integer(UncompressedindexListV1.get("pressure").dF).toString()+ ", "); 
		     bfw.write("tf: ");
		     bfw.write(new Integer(UncompressedindexListV1.get("pressure").tf).toString()+ ", ");
		     bfw.write("Inverted List Length: ");	     
		     
		     for(Entry<Document,Integer> tc : UncompressedindexListV1.get("pressure").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+= out.size();
			         	         
			   } 
		     bfw.write(new Integer(size).toString());
		    }
		    
		    //boundary
		    bfw.newLine();
		    bfw.write("Term:boundary: "); 
		    if(UncompressedindexListV1.containsKey("boundary"))
		    {
		    bfw.write("df: "); 
		    bfw.write(new Integer(UncompressedindexListV1.get("boundary").dF).toString()+ ", ");
		    bfw.write("tf: ");
		    bfw.write(new Integer(UncompressedindexListV1.get("boundary").tf).toString()+ ", ");
		    bfw.write("Inverted List Length: ");
		    for(Entry<Document,Integer> tc : UncompressedindexListV1.get("boundary").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+=out.size();
			         	         
			   } 
		    bfw.write(new Integer(size).toString());
		    }
		    
		    // Shock..
		    bfw.newLine();
		    bfw.write("Term:shock: ");		    
		    if(UncompressedindexListV1.containsKey("shock"));
		    {
		     bfw.write("df: "); 
		     bfw.write(new Integer(UncompressedindexListV1.get("shock").dF).toString()+ ", ");		    
		     bfw.write("tf: ");
		     bfw.write(new Integer(UncompressedindexListV1.get("shock").tf).toString()+ ", ");
		     bfw.write("Inverted List Length: ");		    
		     for(Entry<Document,Integer> tc : UncompressedindexListV1.get("shock").PostingF.entrySet())
		       {	
		    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
		          ObjectOutputStream os = new ObjectOutputStream(out);
		          os.writeObject(tc.getKey());
		          size+= out.size();
			         	         
			   } 
		     bfw.write(new Integer(size).toString());
		    }
	    
		   
		    
		    // 5
		    bfw.newLine();
		    bfw.newLine();
		    bfw.write("5. The df,tf, the doclen and the max_tf, for the first 3 entries in its posting list for NASA: ");		     
		    bfw.newLine();
		    bfw.newLine();
		    bfw.write("Document Frequency (df): "); 
		    bfw.write(new Integer(UncompressedindexListV1.get("nasa").dF).toString()); 
		    bfw.newLine();
		    int count=1;
		    for(Entry<Document,Integer> tc : UncompressedindexListV1.get("nasa").PostingF.entrySet())
		    {
		    	if(count<4)
		    	{
		    	bfw.write("Entry " + count + ": " ); 			    
			    bfw.write("tf in the doc: ");
			    bfw.write(new Integer(tc.getValue()).toString());    
			    bfw.write("Max_tf: ");			    
			    bfw.write(new Integer(tc.getKey().max_tf).toString());			   
			    bfw.write("doclen: ");			   
			    bfw.write(new Integer(tc.getKey().doclen).toString()); 
			    bfw.newLine();			    
			    count++;
		    	}
		    	else
		    	 break;
		    } 
		    
		    //6
		    int maxdFV1=0;
		    int mindFV1=5000;	    
		    ArrayList<String> minterm= new ArrayList<String>();
		    ArrayList<String> maxterm=new ArrayList<String>();
		    int DocIddoclen =0 , DocIdmaxtf =0;
		    for(Entry<String, TermDoc> tc : UncompressedindexListV1.entrySet())
		    {
		    	if(tc.getValue().dF>maxdFV1)
		    		maxdFV1=tc.getValue().dF;
		    		    	
		    	if(tc.getValue().dF<mindFV1)
	    		  mindFV1=tc.getValue().dF;	   		 
		    	
		    }
		    
		    for(Entry<String, TermDoc> tc : UncompressedindexListV1.entrySet())
		    {
		    	if(tc.getValue().dF==maxdFV1)		    		
		    	   maxterm.add(tc.getKey());
		    	
		    		    	
		    	if(tc.getValue().dF==mindFV1)	    		
	    		   minterm.add(tc.getKey());
		     }
		    
		    bfw.newLine();		    
		    bfw.write("6: The dictionary terms from index 1 with the largest df(" + maxdFV1 + "): ");		    
		    bfw.newLine();
		    Iterator<String> it = maxterm.iterator();
		    while(it.hasNext())
		    {	
		       bfw.write(it.next());
		       bfw.write(",");
		    }
		    
		    bfw.newLine();
		    bfw.newLine();
		    bfw.write("The dictionary terms with the lowest df(" + mindFV1 + "):  ");
		    bfw.newLine();
		    Iterator<String> it1 = minterm.iterator();
		    while(it1.hasNext())
		    {	
		       bfw.write(it1.next());
		       bfw.write(",");
		    }
		    bfw.newLine();
		    bfw.newLine();
		    
		    
		    //7
		    maxdFV1=0;
		    mindFV1=5000;
		    ArrayList<String> minterm1= new ArrayList<String>();
		    ArrayList<String> maxterm1=new ArrayList<String>();
		    for(Entry<String, TermDoc> tc : UncompressedindexListV2.entrySet())
		    {
		    	if(tc.getValue().dF>maxdFV1)
		    		maxdFV1=tc.getValue().dF;
		    	
		    	if(tc.getValue().dF<mindFV1)
		    		mindFV1=tc.getValue().dF;
		    	
		    }
		    
		    for(Entry<String, TermDoc> tc : UncompressedindexListV2.entrySet())
		    {
		    	if(tc.getValue().dF==maxdFV1)		    		
		    	   maxterm1.add(tc.getKey());
		    	
		    		    	
		    	if(tc.getValue().dF==mindFV1)	    		
	    		   minterm1.add(tc.getKey());
		     }
		    
		    bfw.write("7. The stem from index 2 with the largest df(" + maxdFV1 + "): ");
		    bfw.newLine();		    
		    Iterator<String> it2 = maxterm1.iterator();
		    while(it2.hasNext())
		    {	
		       bfw.write(it2.next());
		       bfw.write(",");
		    }
		    bfw.newLine();
		    bfw.newLine();
		    bfw.write("The stem from index 2 with the lowest df(" + mindFV1 + "): ");
		    bfw.newLine();
		    Iterator<String> it3 = minterm1.iterator();
		    while(it3.hasNext())
		    {	
		       bfw.write(it3.next());
		       bfw.write(",");
		    }
		    bfw.newLine();
		    bfw.newLine();
		   
		    
		    //8
		    int max_tf=0;
		    int maxdoclen=0;
		    for(Entry<Integer, Document> tc : documents.entrySet())
		    {
		    	if(tc.getValue().max_tf>max_tf)
	    		{max_tf=tc.getValue().max_tf;
	    		 DocIdmaxtf = tc.getKey();}
	    	
		    	if(tc.getValue().doclen>maxdoclen)
	    		{maxdoclen=tc.getValue().doclen;
	    		 DocIddoclen = tc.getKey();}
	    	 }
		    
		    
		    bfw.write("8: The document with the largest max_tf( " + max_tf + ") in the collection: ");	
		    bfw.write("Cranfield" + new Integer(DocIdmaxtf).toString() + " maxtf : " + max_tf); 
		    bfw.newLine();		    
		    bfw.write("   The document with the largest doclen ( " + maxdoclen + ")in the collection: ");		   
		    bfw.write("Cranfield" + new Integer(DocIddoclen).toString() + " max doclen: " + maxdoclen); 
		    bfw.newLine();
		    
		  bfw.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	  
	  
	}

	public static void processCranfield(File f) {
		
		int documentID=0, maxtf=0 , totaldoclen=0;
		Stemmer Stem = new Stemmer();
		IndexConstruction indexlist  = new IndexConstruction();
		
	   // To open a file.. 
		try {	 
			 File[] files = f.listFiles();			 
			 int j=0,k=0;
			 while(k<files.length)
			 {
				 
				if(files[k].isDirectory() && files[k].getName().equals("Cranfield"))					 
				{
					
				 File[] subFiles = files[k].listFiles(); 	
			      while(j<subFiles.length)
			      {
			    	  Collectionsize++; // to be used in relevance model...
			   	    //System.out.println("the file is :: " + subFiles[j].getName());
			         FileReader fw = new FileReader(subFiles[j]);			  
			         int avgcount=0;
			         BufferedReader bfr = new BufferedReader(fw);	
			         String line = "";
			         Document doc = new Document();			         
			         Document docstem = new Document();
			         tempIndex.clear();
			         String headline ="";
			          // creating a temporary index for each doc..
			         while((line = bfr.readLine()) != null)
				     {
			        	  
			          // to capture the document id	
				      if(line.equals("<DOCNO>"))
				         {
				        	  documentID = Integer.parseInt(bfr.readLine());
				        	  doc.docid = documentID;
				        	  docstem.docid = documentID;
				        	  
				        	 // System.out.println("document id :: " + documentID);  
				         }
				       
				      
				    if(line.equals("<TITLE>"))
				    { 
				    	String title = bfr.readLine();
				    	while(!title.equals("</TITLE>"))
				    		 {				    		  
				    		  headline += title ;
				    	      title = bfr.readLine();
				    	      }
				       docHeadline.put(doc.docid, headline);
				    }	
				    	
				      
				     //System.out.println(line);
				     if (!((line.startsWith("<"))))
				     {
					  
					   String[] tokens = line.split(" |\t");
					
					   for(int i=0;i<tokens.length;i++)
					   {
						    ArrayList<String> toks = new ArrayList<String>();						 
						    avgcount++;
						      
						      String token = Tokenize(tokens[i]);
						      
							  // split the words if contain [-.] in between..
							  split(token,toks);
							  avgcount = avgcount + toks.size()-1;
					         		  
						      Iterator<String> itd = toks.iterator();
						      
						      // adding the token to hash map....						  
						      while(itd.hasNext())
						      {
						    	 String finaltokens = (String)itd.next();
						    	 
						    	 if(!finaltokens.matches("-?\\d+(\\.\\d+)?") && !finaltokens.equals(""))
									{
						    		
						    		
							          // adding the length of the document						    		  						    		  
						    		  doc.doclen = doc.doclen + 1;						    		  
						    		  docstem.doclen = docstem.doclen + 1;	
						    		  
						    		  //for lemmatization..uncompressed version 1
						    		    long starttime3 = System.currentTimeMillis();
						    		    String lemma  = indexlist.lemmatize(finaltokens);
						    		    if(lemma != null  && !lemma.equals(":"))
						    		       {
						    		        if(lemma.equals(":"))
						    		    	   System.out.println(": occured");
						    		        indexlist.addToIndex(lemma, documentID,2);
						    		        maxtf = termIndex.get(lemma).PostingF.get(documentID);
						    		     
						    		        //store the term frequency in that document in a variable		    		  
									        if(maxtf>doc.max_tf)										     
											  doc.max_tf = maxtf;					  
										   
									       }
						    		    long endtime3 = System.currentTimeMillis();
						    			timeelapsed1  += endtime3 - starttime3 ;
						    			
						    		    
						    		 // for stemmed list by porter stem..uncompressed version 2
						    			long starttime4 = System.currentTimeMillis();
							    		String stemmedW = Stem.constructStemmedList(finaltokens);		 
						    		  
						    		    if(stemmedW !="")
						    		    {
						    			   
						    		     indexlist.addToIndex(stemmedW, documentID,1);						    		  
					    		         int maxtf1 = termIndexstem.get(stemmedW).PostingF.get(documentID);
					    		     
					    		         //store the term frequency in that document in a variable		    		  
								         if(maxtf1>docstem.max_tf)									      
										    docstem.max_tf = maxtf1;											  
									      						    		 
						    		     }
						    		   
						    		   long endtime4 = System.currentTimeMillis();
						    		   timeelapsed2  += endtime4 - starttime4;
						    		   
						    		  }					    		  
						    		  
						          }					      
						      }  
					    }	
				   }				
				
			         
			    // after a document is parsed, storing the information then in the list of term doc..v1
			    totaldoclen+=doc.doclen;
			    long starttime3 = System.currentTimeMillis();    
		        for(Entry<String, LemmaT> entry : tempIndex.entrySet())
				{	    	
		        	if(UncompressedindexListV1.containsKey(entry.getKey()))
			         {
			        	TermDoc termdoc = UncompressedindexListV1.get(entry.getKey());
			        	termdoc.dF +=1;
			        	termdoc.tf=entry.getValue().tf;
			        	for(Entry<Integer, Integer> entry1 : entry.getValue().PostingF.entrySet())									    	
			        		termdoc.PostingF.put(doc, entry1.getValue());
			        	UncompressedindexListV1.put(entry.getKey(), termdoc);
			         }
			         
			         else
			         {	 
				      LemmaT lemma = entry.getValue();
				      TermDoc tdoc = new TermDoc(lemma.dF, lemma.tf,lemma.term);
				      for(Entry<Integer, Integer> entry1 : lemma.PostingF.entrySet())									    	
						tdoc.PostingF.put(doc, entry1.getValue());
				      
				      UncompressedindexListV1.put(entry.getKey(), tdoc);
					  }
				}
		        tempIndex.clear();    // clearing the map after a document is parsed.. 
		        long endtime3 = System.currentTimeMillis();
    			timeelapsed1  += endtime3 - starttime3 ;
		        
		        
		        
		        // after a document is parsed, storing the information then in the list of term doc.. v2   			 
    			long starttime4 = System.currentTimeMillis();  
		        for(Entry<String, LemmaT> entry : tempIndexstem.entrySet())
				{
		         if(UncompressedindexListV2.containsKey(entry.getKey()))
		         {
		        	TermDoc termdoc = UncompressedindexListV2.get(entry.getKey());
		        	termdoc.dF +=1;
		        	termdoc.tf=entry.getValue().tf;
		        	for(Entry<Integer, Integer> entry1 : entry.getValue().PostingF.entrySet())									    	
		        		termdoc.PostingF.put(docstem, entry1.getValue());
		        	UncompressedindexListV2.put(entry.getKey(), termdoc);
		         }
		         
		         else
		         {	 
			      LemmaT lemma = entry.getValue();
			      TermDoc tdoc = new TermDoc(lemma.dF, lemma.tf,lemma.term);
			      for(Entry<Integer, Integer> entry1 : lemma.PostingF.entrySet())									    	
					tdoc.PostingF.put(docstem, entry1.getValue());
			      
			      UncompressedindexListV2.put(entry.getKey(), tdoc);
				  }
				} 
		        tempIndexstem.clear();    // clearing the map after a document is parsed.. 
		        long endtime4 = System.currentTimeMillis();
	    		timeelapsed2  += endtime4 - starttime4;
		        
		        IndexEngine.documents.put(documentID, doc);
			   
			    j++; 			   
			    bfr.close(); 
			  }
			   
			    break;
		    }	    
		
		    k++;
		} 
	   
		
	    } catch (Exception e) {
				// TODO Auto-generated catch block    	  
				e.printStackTrace();
	      }
		
		avgdoclen = totaldoclen/Collectionsize;
		
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
	
	
	public static void split(String token,ArrayList<String> toks)
	{
		
			 if(token.contains("-"))
				 {
					 for(int i=0;i<token.split("-").length;i++)										 
						 split(token.split("-")[i],toks);					
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
						   split(token.split(".")[i],toks);						 
					   }
					   
					 else // an Acronym needs to be added as whole
						 {
						// Convert all uppercase to lowercase .. Acronym
						 token = token.toLowerCase();
						 toks.add(token); }
				
			     }
			 
			else if(token.contains(","))
			    { 
				   for(int i=0;i<token.split(",").length;i++)					 
					 split(token.split(",")[i],toks);
			     }
			
			else if(token.startsWith("non"))
			     {				   
				   toks.add("non");
				   toks.add(token.substring(3));
			     }
			 
			else
			    {
				 
				 if(token.matches(".*\\d+.*"))				  								
					token = token.replaceAll("\\d","");
				  
				   if(token.length()>1)				
				     {
					   // Convert all uppercase to lowercase
					   token = token.toLowerCase();
					   toks.add(token);
				     }
			    }
		 
	   }
	
	public static void WritetoFile(int ver , File f )
	{
		
		try {
			 // writing to  file
			  FileWriter fi= new FileWriter(f);
			  
			  if(ver==1)
			  {
				  for(Entry<String, TermDoc> entry : UncompressedindexListV1.entrySet())
					{  
					  
					   fi.write(entry.getKey());
					   fi.write("@");
					   fi.write(Integer.toString(entry.getValue().tf));					   
					   fi.write(",");
					   fi.write(Integer.toString(entry.getValue().dF));					   
					   fi.write("(");
					   //System.out.println("posting file size for " + entry.getKey() + " is " + entry.getValue().PostingF.size());
					   for(Entry<Document, Integer> entry1 : entry.getValue().PostingF.entrySet())
						{  
						   
						   fi.write(Integer.toString(entry1.getKey().docid));
						   fi.write(",");
						   fi.write(Integer.toString(entry1.getValue()));
						   fi.write(",");
						   fi.write(Integer.toString(entry1.getKey().max_tf));						   
						   fi.write(",");
						   fi.write(Integer.toString(entry1.getKey().doclen));
						   
						 }
						 fi.write(")"); 
					  
					}
				 fi.close();
				  
			  }
			  else
			  {
					  for(Entry<String, TermDoc> entry : UncompressedindexListV2.entrySet())
						{
						   //System.out.println(entry.getKey());
						   fi.write(entry.getKey());
						   fi.write("@");
						   fi.write(Integer.toString(entry.getValue().tf));					   
						   fi.write(",");
						   fi.write(Integer.toString(entry.getValue().dF));					   
						   fi.write("(");
						  //System.out.println("posting file size for " + entry.getKey() + " is " + entry.getValue().PostingF.size());
						   for(Entry<Document, Integer> entry1 : entry.getValue().PostingF.entrySet())
							{  
							   
							   fi.write(Integer.toString(entry1.getKey().docid));
							   fi.write(",");
							   fi.write(Integer.toString(entry1.getValue()));
							   fi.write(",");
							   fi.write(Integer.toString(entry1.getKey().max_tf));						   
							   fi.write(",");
							   fi.write(Integer.toString(entry1.getKey().doclen));
							   
							 }
							fi.write(")"); 
						  
						}
					 fi.close();
					  
			}
				  
			  
			  
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
