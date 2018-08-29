import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
public class KNNWeka {
	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
 
		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}
 
		return inputReader;
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader datafile = readDataFile("knnj.arff"); //Read arff file which contains the dataset
		Instances data = new Instances(datafile); //Create instance of the file
		data.setClassIndex(0);
		Classifier knn = new IBk();// Create classifier object to implement Knn classification
		Instance data1 = data.instance(120);
		data.delete(0);
		Evaluation eval = new Evaluation(data); // Create evaluation object for cross validation
		eval.crossValidateModel(knn, data, 10, new Random(1)); // Perform 10-fold cross validation
		
        String output = eval.toSummaryString(); //Convert the output summary to string
        System.out.println(output);//Print summary of cross validation
		
	}

}