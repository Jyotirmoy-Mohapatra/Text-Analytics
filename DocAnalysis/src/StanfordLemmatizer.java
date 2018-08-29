import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer 
{
	public void lemmatize(String text, StanfordCoreNLP pipeline, List<String> tokenWords) 
	{
		String ne = null;
		int index = 0;
		Annotation annotation = new Annotation(text);		
		pipeline.annotate(annotation);    //System.out.println("LamoohAKA");
		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		
		for(CoreMap sentence: sentences) 
		{
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) 
			{
				String temp_ne = token.get(NamedEntityTagAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				if(!(temp_ne.equals("O")) && temp_ne.equals(ne)){
					String temp_lemma = tokenWords.get(index - 1)+" "+lemma;
					tokenWords.set(index - 1, temp_lemma);
				}
				else{
					tokenWords.add(lemma);
					index ++;
				}	
				ne = temp_ne;
			}
		}
    }
	
	public StanfordCoreNLP getPipeline(){
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		return pipeline;
	}
}
