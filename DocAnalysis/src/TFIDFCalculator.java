import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.stream.Stream;





import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class TFIDFCalculator {
    
	static HashMap<String, Double> TFMatrix = new HashMap<String, Double>();
	static HashMap<String, HashMap<String, Double>> TFIDFMatrix = new HashMap<String, HashMap<String, Double>>();
    public static double tf(double frequency, int docsize) {
        
    	return frequency / docsize;
    }

    
    public static double idf(List<List<String>> docs, String corpus, String term) {
        double n = 0;
        int index=corpus.indexOf(term); // To find first occurrence
        while(index<corpus.length() && index != -1) 
        { 
            index=corpus.indexOf(term,index+1);/// to find next occurrences 
            n++;
        } 
        return Math.log(docs.size() / n);
    }

    
    
    private static List<String> fileLinesToList(final Path file, StanfordCoreNLP pipeline, StanfordLemmatizer tok) {
    	List<String> wordsInDoc = new ArrayList<String>();
    	List<String> tokenWords = new ArrayList<String>();
        Path textFile = Paths.get(file.getParent() + File.separator, file.getFileName().toString());
        	try {
        		byte[] lem_Array = Files.readAllBytes(textFile);
        		String lem_text = new String(lem_Array, "ISO-8859-1");
            	tok.lemmatize(lem_text, pipeline, tokenWords);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

    		StopWord sobj = new StopWord();
    		tokenWords = sobj.removeStopWords(tokenWords);
        	for(String word : tokenWords){
            	wordsInDoc.add(word);
        	}
        	Corrall obj = new Corrall();
            obj.corrall(tokenWords);
            for (Entry<String, Integer> entry : obj.finalTokens.entrySet()) {
            	if(TFMatrix.containsKey(entry.getKey()))
                {     
            		TFMatrix.put(entry.getKey(), TFMatrix.get(entry.getKey())+entry.getValue());
                } 
                else
                { 
                	TFMatrix.put(entry.getKey(), (double)entry.getValue());
                } 
            	
            } 
        return (wordsInDoc);
    }
    public static void main(String[] args) {

        List<List<String>> wordsInCorpus = new ArrayList<List<String>>();
        File[] directories = new File("C:\\Users\\JYOTIRMOY\\Desktop\\Predictive Analytics\\HW1\\HW1\\DataSet\\DataSet").listFiles(File::isDirectory);
        StanfordLemmatizer tok = new StanfordLemmatizer();
        StanfordCoreNLP pipeline = tok.getPipeline();
        for(File directory:directories)
        {
        	System.out.println(directory.getName());
            TFMatrix.clear();
        	try (Stream<Path> paths = Files.walk(Paths.get("C:\\Users\\JYOTIRMOY\\Desktop\\Predictive Analytics\\HW1\\HW1\\DataSet\\Dataset\\"+directory.getName()))) {
                paths 
                    .filter(Files::isRegularFile) 
                    .forEach(p -> wordsInCorpus.add(fileLinesToList(p, pipeline, tok)));
            } catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            //Calculating TF of each term in the folder
        	for (Entry<String, Double> entry : TFMatrix.entrySet()) {
                TFMatrix.put(entry.getKey(), tf(entry.getValue(), wordsInCorpus.size()));
            } 
            HashMap<String, Double> temp_TFMatrix = new HashMap<String, Double>();
            temp_TFMatrix.putAll(TFMatrix);
            TFIDFMatrix.put(directory.getName(), temp_TFMatrix);
        }
        
        String corpus="";
        for (List<String> doc : wordsInCorpus) {
        	for(String word : doc){
        		corpus +=word + " ";
        	}
        }
        
        //Calculating TFIDF accross the whole corpus
        String s="";
        for(Entry<String, HashMap<String, Double>> e :TFIDFMatrix.entrySet())
        {
        	for (Entry<String, Double> entry : e.getValue().entrySet()) 
        	{
        		e.getValue().put(entry.getKey(), idf(wordsInCorpus, corpus, entry.getKey()) * entry.getValue());
        	} 
        }
        
        PrintWriter writer, w2v;
		try {
			writer = new PrintWriter("tfidf_matrix.txt", "UTF-8");
			w2v = new PrintWriter("Final_Terms_per_Folder.txt", "UTF-8");
        for(Entry<String, HashMap<String, Double>> e :TFIDFMatrix.entrySet())
        {
        	Set<Entry<String, Double>> entrySet = e.getValue().entrySet();
            List<Entry<String, Double>> list = new ArrayList<Entry<String,Double>>(entrySet);
            Collections.sort(list, new Comparator<Entry<String, Double>>() 
            { 
                @Override 
                public int compare(Entry<String, Double> e1, Entry<String, Double> e2) 
                { 
                    return (e2.getValue().compareTo(e1.getValue()));
                } 
            }); 
            int i = 0;
            writer.println("");
            writer.print(e.getKey() + ": ");
            w2v.println("");
            w2v.print(e.getKey() + ": ");
            for (Entry<String, Double> entry : list) 
            { 
            	writer.print(entry.getKey() + ":" + entry.getValue() + " | ");
            	if(i<5)
            	{
            		w2v.print(entry.getKey() + " | ");
            	}
            	i++;
            }
        }
        writer.close();
        w2v.close();
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (UnsupportedEncodingException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		System.out.println("Task Completed. Check tfidf_matrix.txt and Final_Terms_per_Folder.txt");
    }

}

