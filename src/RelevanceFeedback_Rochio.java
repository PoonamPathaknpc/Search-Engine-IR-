import java.math.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;


public class RelevanceFeedback_Rochio {
	
	public static HashMap<Integer,HashMap<String, Weight>> documentWeights = new HashMap<Integer, HashMap<String, Weight>>();
	
	public RelevanceFeedback_Rochio() {
		
		for(Entry<String, HashMap<Integer, Weight>> Term : RelevanceCalculation.docweights.entrySet())
		{	
			
		
		 HashMap<Integer,Weight> weights12 = Term.getValue();
		 for(Entry<Integer,Weight> posting : weights12.entrySet())
		  {
			 if(documentWeights.containsKey(posting.getKey()))
			 {
				 HashMap<String,Weight> lemmas = documentWeights.get(posting.getKey());
				 lemmas.put(Term.getKey(),posting.getValue());
				 documentWeights.put(posting.getKey(), lemmas);
			 }
			 else
			 {
				 HashMap<String,Weight> lemmas = new HashMap<String,Weight>();
				 lemmas.put(Term.getKey(),posting.getValue());
				 documentWeights.put(posting.getKey(), lemmas);
			 }
		  }
		}
			
	}
	
	
	public List<Map.Entry<String, BigDecimal>> getFeedback(String Query , HashMap<Integer, BigDecimal> finalValues, HashMap<String ,Integer>qwords)
	{
		int[] relevantD = {12,486,14,51,184,13};
		//int[] nonrelevantD= {573,1268,329,878,195,576,78,141,1180,293,92,1218,1255,172};
		HashMap<String, BigDecimal> ExpandedQuery = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> RDWeights = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> NRDWeights = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> QueryWeights = new HashMap<String, BigDecimal>();
		
		int i=0;
		System.out.println(" For query --- " + Query);
		
		for(Entry<String ,Integer> entry : qwords.entrySet())
		{
			QueryWeights.put(entry.getKey(),new BigDecimal(Math.log10(entry.getValue()+1)));
			
		}

	    double alpha = 0.25;
	    double  Beta = 0.75;
		int rdlen=0,nrdlen=0;
		
		for(Entry<Integer,BigDecimal> entry : finalValues.entrySet())
		{				
				// Initially setting relevant and non relevant Queries..
				//System.out.println("Document#" + entry.getKey() + "--- Cosine values--- " + entry.getValue());	
			
				for(int j=0;j<relevantD.length;j++)
				{
					HashMap<String,Weight> lemmas = documentWeights.get(entry.getKey());
					if(entry.getKey().equals(relevantD[j]))
					{						
						for(Entry<String,Weight> entry1 : lemmas.entrySet())
						{
							if(RDWeights.containsKey(entry1.getKey()))
							{
								BigDecimal value = RDWeights.get(entry1.getKey()).add(new BigDecimal(entry1.getValue().doc_weightW1*Beta));
								RDWeights.put(entry1.getKey(),value);
								
							}
							else
								RDWeights.put(entry1.getKey(),new BigDecimal(entry1.getValue().doc_weightW1*Beta));
						}
						
						rdlen++;
					}
					else
					{
						
						for(Entry<String,Weight> entry1 : lemmas.entrySet())
						{
							if(NRDWeights.containsKey(entry1.getKey()))
							{
								BigDecimal value = NRDWeights.get(entry1.getKey()).add(new BigDecimal(entry1.getValue().doc_weightW1*alpha));
								NRDWeights.put(entry1.getKey(),value);
								
							}
							else
								NRDWeights.put(entry1.getKey(),new BigDecimal(entry1.getValue().doc_weightW1*alpha));
						}
						
						nrdlen++;
					}
					
				}
				
			
		}
		
		//adding the Query vector and relevant doc vectors with beta..
		MathContext mc = new MathContext(2, RoundingMode.HALF_UP);
		
		for(Entry<String, BigDecimal> rdw : RDWeights.entrySet())
		{
			// Q + beta*rd/rdlen
			if(QueryWeights.containsKey(rdw.getKey()))
				ExpandedQuery.put(rdw.getKey(), QueryWeights.get(rdw.getKey()).add(rdw.getValue().divide(new BigDecimal(rdlen),mc)));
			else
				ExpandedQuery.put(rdw.getKey(), rdw.getValue().divide(new BigDecimal(rdlen)));
		}
		
		
		for(Entry<String, BigDecimal> nrdw : NRDWeights.entrySet())
		{
			// Q - alpha*nrd/nrdlen
			if(ExpandedQuery.containsKey(nrdw.getKey()))				
				ExpandedQuery.put(nrdw.getKey(), ExpandedQuery.get(nrdw.getKey()).subtract(nrdw.getValue().divide(new BigDecimal(nrdlen),mc)));
			
		}
		
		List<Map.Entry<String, BigDecimal>> Exqueries = new LinkedList<Map.Entry<String, BigDecimal>>(ExpandedQuery.entrySet());
	    Collections.sort( Exqueries, new Comparator<Map.Entry<String, BigDecimal>>()
	        {
	           public int compare( Map.Entry<String, BigDecimal> o1, Map.Entry<String, BigDecimal> o2 )
	            {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	            
	        } );
		
		
		System.out.println("The expanded query terms are:: ");
		Iterator<Entry<String, BigDecimal>> it = Exqueries.iterator();
		int k=0;
		while(it.hasNext())
		{
			if(k<15)
			{Entry<String, BigDecimal> EQ = it.next();
			BigDecimal val = EQ.getValue().setScale(5, RoundingMode.HALF_DOWN);
			if(EQ.getValue().signum() != -1)
			   System.out.println(EQ.getKey() + "::" + val);
			k++;
			}
			else
				break;
		}
		return Exqueries;		
		
		
	}

}
