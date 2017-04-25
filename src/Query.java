import java.util.HashMap;

public class Query {
	String sQuery;
	HashMap<String,weights> w = new HashMap<String,weights>();	
	int dMaxFreqLemmaFrequency = 0;	
	int dQueryLen = 0;
	double dAvgQueryLen = 0.00;
	
	public class weights{
		String lemma="";
		double weight1=0.0;
		double weight2=0.0;
	}
	
	public Query()
	{
		sQuery="";
	}
	
	public void addWeight(String q , double w1 , double w2)
	{
		weights w12 = new weights();
		w12.weight1 = w1;
		w12.weight2 = w2;
		w.put(q, w12);
	}
}
