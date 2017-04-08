import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.BitSet;
import java.io.*;
import java.util.*;


public class IndexCompression {
	
	// map of compressed posting with dictionary pointing with  gaps..
	Map<Integer, CompressedPSFiles> Compressedversion1 = new TreeMap<Integer, CompressedPSFiles>();
	Map<Integer, CompressedPSFiles> Compressedversion2 = new TreeMap<Integer, CompressedPSFiles>();
	int indexunMatch=7;
	boolean bFullMatch=true;
	public IndexCompression() {
		// TODO Auto-generated constructor stub
	}
	
	public void Compression(int version , File f)
	{		
	
		TermDoc[] Tokens = new TermDoc[8];
		PostingTermInfo[] termpostings = new PostingTermInfo[8]; 
		String CompDictionary = "";
		int count = 0;	
		
		if(version==1 )
		{
			// compression version 1 : Blocking and Gamma with k=8
		  try{
			//File f = new File("E:\\documents\\UTDallasMasters\\Semester-2\\IR\\ppp160130_Assignment2\\");			
			RandomAccessFile fi= new RandomAccessFile(f,"rw");	
			int position=0;
			for(Entry<String,TermDoc> entry : IndexEngine.UncompressedindexListV1.entrySet())
			{	
				
			    String term = entry.getKey();
				if(count < 8)
				{	
				  CompDictionary += Integer.toString(term.length()) + term;// appending the dictionary with 8 continues terms..
				  Tokens[count] = entry.getValue(); 
				  byte[] tf = getGammaCode(entry.getValue().tf); // compressing total term frequency..
				  byte[] df = getGammaCode(entry.getValue().dF); // compressing number of doc containing the term..
				  PostingTermInfo  psFile = new PostingTermInfo(tf,df);
				  termpostings[count] = psFile;
				  
				  // gamma compression of other metrics..
				  count++;
				}
				else
				{
					
					CompressedPSFiles cmp = new CompressedPSFiles();					
					int iStartDocId = 0;
					
					for(int i=0;i<8;i++)
				    {							
						
						for(Entry<Document,Integer> docentry : Tokens[i].PostingF.entrySet())
						{
							/* fetching and compressing the following
							 1)  gaps, 
							 2)  max_tf ,
							 3)  freq of term in the doc 
							 4)  doclen							
							*/
							
							int Gap = docentry.getKey().docid - iStartDocId;
							int Freq = docentry.getValue();
							int max_tf = docentry.getKey().max_tf;
							int doclen = docentry.getKey().doclen;
							termpostings[i].addPostingFile(getGammaCode(Gap), getGammaCode(Freq),getGammaCode(max_tf),getGammaCode(doclen));						
							iStartDocId = docentry.getKey().docid;
							
						}				
						
				    }
					cmp.ListTerms = termpostings;
					Compressedversion1.put(position, cmp);
					
					fi.writeBytes(CompDictionary + "@" + position + "=(");
					  
					  for(int k=0;k<8;k++)
					  {
						  fi.write(termpostings[k].TermFreq);
						  fi.writeBytes(",");
						  fi.write(termpostings[k].DocFreq);
						  fi.writeBytes("(");
						  Iterator<PostingTermInfo.PostingFile> pss = termpostings[k].ps.iterator();
						  while(pss.hasNext())
						  {
						   PostingTermInfo.PostingFile pf = pss.next(); 
						   fi.write(pf.gap);
						   fi.writeBytes(",");
						   fi.write(pf.byFrequency);
						   fi.writeBytes(",");
						   fi.write(pf.max_tf);
						   fi.writeBytes(",");
						   fi.write(pf.doclen);
						   
						  }
						  fi.writeBytes(")"); 
						  
					  }
					  fi.writeBytes(")");
					  
					position += CompDictionary.length(); // to capture the position of the term pointer in dictionary
				    CompDictionary="";
					count=0; 
				}
					
			}
			fi.close();
			
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		else
		{
		  try{
				//File f = new File("E:\\documents\\UTDallasMasters\\Semester-2\\IR\\ppp160130_Assignment2\\CompressedIndexV2.txt");
				RandomAccessFile fi= new RandomAccessFile(f,"rw");
				int position=0;
			          // compression version 2 : FrontCoding and Delta						
						int start=0;
						for(Entry<String,TermDoc> entry : IndexEngine.UncompressedindexListV2.entrySet())
						{	
										    
							
							if((count+start) < 8)
							{					
							  if (count==0)
							  {
								  // to capture the position of the term pointer in dictionary
								  position = CompDictionary.length(); 
								  
							  }
							  
							  
							  Tokens[count+start] = entry.getValue(); 
							  byte[] tf = getDeltaCode(entry.getValue().tf); // compressing total term frequency..
							  byte[] df = getDeltaCode(entry.getValue().dF);// compressing number of doc containing the term..
							  PostingTermInfo  psFile = new PostingTermInfo(tf,df);
							  termpostings[count+start] = psFile;
							  
							  // gamma compression of other metrics..
							  count++;
							}
							
						else
						  {
							
								start = 0;
								// adding to dictionary as per front coding..
								
								CompressedPSFiles cmp = new CompressedPSFiles();								
								String Prefix = Tokens[0].term;
								
								String Match = findCommonPrefix(Tokens, Prefix);
								
								if(Match != null)
								{
									CompDictionary = Match.length() + Match + "*";
									int PrefixLength = Match.length();									
									int Start = 0;
									//System.out.println("terms matched " + this.indexunMatch + " :: " + PrefixLength) ; 
								  for(int i=0;i<=this.indexunMatch;i++)
								  {	
									  									  
									CompDictionary = CompDictionary + Tokens[i].term.substring(PrefixLength).length() + "|" + Tokens[i].term.substring(PrefixLength);
									
									for(Entry<Document,Integer> docentry : Tokens[i].PostingF.entrySet())
									{
										/* fetching and compressing the following
										 1)  gaps, 
										 2)  max_tf ,
										 3)  frequency of term in the doc 
										 4)  document length							
										*/
										int Gap = docentry.getKey().docid - Start;
										int Freq = docentry.getValue();
										int max_tf = docentry.getKey().max_tf;
										int doclen = docentry.getKey().doclen;
										termpostings[i].addPostingFile(getDeltaCode(Gap), getDeltaCode(Freq),getDeltaCode(max_tf), getDeltaCode(doclen));						
										Start = docentry.getKey().docid;
										
									}				
									
							       }
								 
								  cmp.ListTerms = termpostings;
								  Compressedversion2.put(position,cmp);
								  
								 // writing to  file
								  fi.writeBytes(CompDictionary + "@" + position + "=(");
								  
								  for(int k=0;k<8;k++)
								  {
									  fi.write(termpostings[k].TermFreq);
									  fi.writeBytes(",");
									  fi.write(termpostings[k].DocFreq);
									  fi.writeBytes("(");
									  Iterator<PostingTermInfo.PostingFile> pss = termpostings[k].ps.iterator();
									  while(pss.hasNext())
									  {
									   PostingTermInfo.PostingFile pf = pss.next(); 
									   fi.write(pf.gap);
									   fi.writeBytes(",");
									   fi.write(pf.byFrequency);
									   fi.writeBytes(",");
									   fi.write(pf.max_tf);
									   fi.writeBytes(",");
									   fi.write(pf.doclen);
									   
									  }
									  fi.writeBytes(")"); 
									  
								  }
								  fi.writeBytes(")");
								  
								  if(this.indexunMatch!=7)
								  {
									int c=0;   
									for(int k=this.indexunMatch+1;k<8;k++)
									{
										//System.out.println(this.indexunMatch);
										Tokens[c] = Tokens[k];
										termpostings[c]=termpostings[k];
										c++;
									}
									start = c; // to make sure the rest of the other new terms start after the last unmatched terms are taken..
									  
								  }
								   
							    }
								
								else // if nothing matches ( a very rare scenario.. we will the first token like blocking and give rest in next iteration)..
								{
									CompDictionary = "0null*"  +  Tokens[0].term.length() + "|" + Tokens[0].term ;
									
									int Start = 0;
									for(Entry<Document,Integer> docentry : Tokens[0].PostingF.entrySet())
									 {
										int Gap = docentry.getKey().docid - Start;
										int Freq = docentry.getValue();
										int max_tf = docentry.getKey().max_tf;
										int doclen = docentry.getKey().doclen;
										termpostings[0].addPostingFile(getDeltaCode(Gap), getDeltaCode(Freq),getDeltaCode(max_tf), getDeltaCode(doclen));											
										Start = docentry.getKey().docid;
										
									 }
									cmp.ListTerms = termpostings;
									Compressedversion2.put(position, cmp);	
									
									// writing to  file
									  fi.writeBytes(CompDictionary + "@" + position + "=(");
									  
									  fi.write(termpostings[0].TermFreq);
									  fi.write(termpostings[0].DocFreq);
									  fi.writeBytes(",");
									  Iterator<PostingTermInfo.PostingFile> pss = termpostings[0].ps.iterator();
									  while(pss.hasNext())
									  {
										   PostingTermInfo.PostingFile pf = pss.next(); 
										   fi.write(pf.gap);
										   fi.writeBytes(",");
										   fi.write(pf.byFrequency);
										   fi.writeBytes(",");
										   fi.write(pf.max_tf);
										   fi.writeBytes(",");
										   fi.write(pf.doclen);
										   
									  }
									
									  fi.writeBytes(")");
									
									for(int k=1,c=0;k<8;k++)
									{
										Tokens[c] = Tokens[k];
										//System.out.println(Tokens[c].term);
										termpostings[c]=termpostings[k];
										c++;
									}
																		
									//count=6;
									start = 7;
								}
								 
								count=0;	
								this.indexunMatch=7;
								position += CompDictionary.length();
						  
		              }
							
		         }
			fi.close();			
		   }catch(Exception e){
				e.printStackTrace();}
		   	
	      }
	
	}	
	
		
	public byte[] getDeltaCode(int Gap)
			{
						
				//Convert the gap value to its binary representation 
				String Binary = Integer.toBinaryString(Gap);
				int BinaryCodeLength = Binary.length();
						
				//Get Gamma code for the length of the binary code
				byte[] GammaCode = getGammaCode(BinaryCodeLength);
						
						//Remove the first bit from the binary representation and take the rest as offset
						String sOffset = Binary.substring(1);
						
						String GammaAndOffset = GammaCode.toString().concat(sOffset);
						int GammaAndOffsetLen = GammaAndOffset.length();
						
						BitSet bsDeltaCode = new BitSet(GammaAndOffsetLen);
						
						//Now converting the String gamma code to bits - compression
						for(int j = 0; j < GammaAndOffsetLen; j++)
						{							
							if(GammaAndOffset.charAt(j) == '1')
							{
								bsDeltaCode.set(j, true);
							}
							else
							{
								bsDeltaCode.set(j, false);
							}			
					  }						
						return bsDeltaCode.toByteArray();						
					}
					
					
	public byte[] getGammaCode(int Gap)
		{						
						//Convert the gap value to its binary representation 
						String sBinaryRep = Integer.toBinaryString(Gap);
						//int iBinaryCodeLen = sBinaryRep.length();
						
						//Remove the first bit from the binary representation and take the rest as offset
						String sOffset = sBinaryRep.substring(1);
						int iOffsetLength = sOffset.length();
						
						//Now get the unary code for the length of the offset
						String UnaryCode = "";
						
						for(int i=0; i < iOffsetLength; i++)
						{
							UnaryCode = UnaryCode + "1";
						}	
						
						UnaryCode = UnaryCode + "0";
						
						//We now have the unary code of the offset length and also the offset - get the String value of the gamma code for the gap
						String GammaCode = UnaryCode + sOffset;
						int GammaCodeLength = GammaCode.length();
						
						BitSet bsGammaCode = new BitSet(GammaCodeLength);
						
						//Now converting the String gamma code to bits - compression
						for(int j = 0; j < GammaCodeLength; j++)
						{
							
							if(GammaCode.charAt(j) == '1')
							{
								bsGammaCode.set(j, true);								
							}
							else
							{
								bsGammaCode.set(j, false);
							}		
							
						}
						
						return bsGammaCode.toByteArray();
					}
	
	
	
	public String findCommonPrefix(TermDoc tokens[], String prefix)
	{
		  int index=-1;		 	
				for(int j=0; j<=this.indexunMatch;j++)
				{					
					if(!tokens[j].term.startsWith(prefix))
						{						
						index = j;
						
						this.bFullMatch = false;
						break;
						}					
				}
				
				
				if(prefix.length()==1)				
					 return null;				
				else
				{
					
				  if(this.bFullMatch==false && index==1)				 
				   {
					 	 prefix = prefix.substring(0, prefix.length()-1);	 
				         return findCommonPrefix(tokens, prefix);
					  
				   } 
				
				  else
					{
					  //System.out.println("final end point " + index);
					  if(index!=-1)
					     this.indexunMatch=index-1;
					  
					  this.bFullMatch=true;
					  return prefix;
					}
				}
	}
	
}
