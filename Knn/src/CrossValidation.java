import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import doctovec.TFIDFCalculator;


public class CrossValidation {
	
	public double[][] trainingdata, testdata;
	public HashMap<String, Integer> classFreq = new HashMap<String, Integer>();
	
	public double[][] shuffle(double[][]data, Knearest obj){
		Random rand = new Random();
		for(int i = 0;i< obj._nrows;i++){
			int n = rand.nextInt(obj._nrows);
			String tempdoc = obj._docs[i];
			obj._docs[i] = obj._docs[n];
			obj._docs[n] = tempdoc;
			for(int j = 0;j<obj._ndims;j++){
				double temp = data[i][j];
				data[i][j] = data[n][j];
				data[n][j]= temp;
			}
		}
		return data;
	}
	
	public void split(double[][] data, Knearest obj){
		Random rand = new Random();
		int n = rand.nextInt(obj._nrows/3)+1;
		
		//System.out.println("Split at: " + n);
		testdata = new double[n][obj._ndims];
		trainingdata = new double[obj._nrows-n][obj._ndims];
				
		for(int i = 0;i<n;i++){
			testdata[i] = data[i];
		}
		for(int i = n;i < obj._nrows;i++){
			trainingdata[i-n] = data[i];
		}
	}
	
	public void updateClassFreq(HashMap<String,String> folder, String [] docs){
		for(Entry<String,String> entry : folder.entrySet()){
			
		}
		for(String doc:docs){
			if(classFreq.containsKey(folder.get(doc))){
				classFreq.put(folder.get(doc), classFreq.get(folder.get(doc)) + 1);
			}
			else{
				classFreq.put(folder.get(doc),1);
			}
		}
	}
	
	public void crossValidate(Knearest obj, HashMap<String,String> folder){
		int correctPrediction, bestk=0;
		double accuracy, maxacc=0.0;
		//Cross-Validation Process
		for(int k = 0;k<10;k++){
			//Foreach K, shuffle the data first and then split the data to testdata and trainingdata
			correctPrediction = 0;
			obj._data = shuffle(obj._data, obj);//shuffles the dataset so that every fold has random labels
			split(obj._data,obj);//splits the dataset randomly to trainingdata[][] and testdata[][]
			//Computing predicted class label of each test data vector
			for(int i = 0;i<testdata.length;i++){
				String[] neighbors = obj.findKNearestNeighbors(testdata[i],trainingdata, k+1, 0);
				for(int x = 0;x<neighbors.length;x++){
					neighbors[x] = folder.get(neighbors[x]);
				}
				String label = obj.classify(neighbors);
				//Checking if predicted label is correct or not
				if(obj._labels.get(folder.get(obj._docs[i])).equals(label)){
					correctPrediction++;
				}
			}
			accuracy = (double)correctPrediction/testdata.length;//Computing accuracy of this K value
			System.out.println("K: "+(k+1)+" Accuracy: "+ accuracy);
			//find K with max accuracy
			if(maxacc < accuracy){
				maxacc = accuracy;
				bestk = k + 1;
			}
		}
		System.out.println("Best k: " + bestk);
		System.out.println("Max accuracy: " + maxacc);
	}
	
	public static void main(String[] args) {
		Knearest obj = new Knearest();
		TFIDFCalculator d2v = new TFIDFCalculator();
		d2v.getTFIDF();//creates the training data by converting the whole dataset to tfidf 
		obj._data=obj.getData("knntfidf.xlsx");// stores the training data in a 2d array for further processing
		CrossValidation cv = new CrossValidation();
		cv.crossValidate(obj, d2v.folder); //Perform Cross Validation
	}
}
