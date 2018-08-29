import java.io.*;
import java.util.*;
import java.util.Map.Entry;


public class Corrall {
	HashMap<String, Integer> finalTokens = new HashMap<String, Integer>();
	public  List<String> ngrams(int n, List<String> words) {
        List<String> ngrams = new ArrayList<String>();
        for (int i = 0; i < words.size() - n + 1; i++)
            ngrams.add(concat(words, i, i+n));
        return ngrams;
    } 
 
    public  String concat(List<String> words, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++)
            sb.append((i > start ? " " : "") + words.get(i));
        return sb.toString();
    } 
 
	public  void corrall(List<String> tokenWords) {
		
		List<String> ngram_tokens = new ArrayList<String>();
		for (int n = 1; n <= 3; n++) {
            ngram_tokens.addAll(ngrams(n, tokenWords));
        }
        
		HashMap<String, Integer> wordCountMap = new HashMap<String, Integer>();
		 
                for (String word : ngram_tokens)
                { 
                    if(wordCountMap.containsKey(word))
                    {     
                        wordCountMap.put(word, wordCountMap.get(word)+1);
                    } 
                    else
                    { 
                        wordCountMap.put(word, 1);
                    } 
                } 
            Set<Entry<String, Integer>> entrySet = wordCountMap.entrySet();
            List<Entry<String, Integer>> list = new ArrayList<Entry<String,Integer>>(entrySet);
            Collections.sort(list, new Comparator<Entry<String, Integer>>() 
            { 
                @Override 
                public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) 
                { 
                    return (e2.getValue().compareTo(e1.getValue()));
                } 
            }); 
            for (Entry<String, Integer> entry : list) 
            { 
            	if(entry.getValue() > 1)
            		finalTokens.put(entry.getKey(),entry.getValue());
            } 
	}

}

