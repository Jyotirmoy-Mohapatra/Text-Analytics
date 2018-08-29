import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class StopWord {
    

 public static Boolean compareWords(String word1, String word2)
    {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();
        if(word1.equals(word2)){
            return true;
        }
        else{
            return false;
        }

    }



   public static Boolean isStopWord(String word, List<String> stopWords)
   {
for(int i=0;i<stopWords.size();i++){
    if(compareWords(word,stopWords.get(i)))
    {
        return true;
    }
}



return false;
 }

   public void convertarray(String fileName,List<String> stopWords){
    String word;
    try{
        Scanner textFile = new Scanner(new File(fileName));
        textFile.useDelimiter(Pattern.compile("[ \r\n]+"));
        
        while (textFile.hasNext())
            {
            word = textFile.next();
            stopWords.add(word);
            }
    }
    catch (FileNotFoundException e)
            {
            System.err.println(e.getMessage());
            System.exit(-1);
            }

   }
 
   
   
   public  List<String> removeStopWords(List<String> tokens)
   {
	   List<String> clean_tokens = new ArrayList<String>();
	   List<String> stopWords = new ArrayList<String>();
	   convertarray("C:\\Users\\JYOTIRMOY\\workspace\\DocAnalysis\\src\\stopwords_en.txt",stopWords);
	   
       for (String word : tokens)
	    {
    	   if (!(isStopWord(word, stopWords))){

    		   clean_tokens.add(word);
    	   }
	    }
       return clean_tokens;

   }
}

