import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import doctovec.*;


public class Knearest {
	double [][] _data; // Array of all records in dataset
	String [] _docs;
	HashMap<String,String> _labels = new HashMap<String,String>();
	
	int _nrows, _ndims;
	
	public Knearest(){
		_labels.put("C1", "Airline Safety");
		_labels.put("C2", "Amphertamine");
		_labels.put("C3", "China and Spy Plan and Captives");
		_labels.put("C4", "Hoof and Mouth Desease");
		_labels.put("C5", "Iran Nuclear");
		_labels.put("C6", "Korea and Nuclear Capability");
		_labels.put("C7", "Mortrage Rates");
		_labels.put("C8", "Ocean and Pollution");
		_labels.put("C9", "Satanic Cult");
		_labels.put("C10", "Store Irene");
		_labels.put("C11", "Volcano");
		_labels.put("C12", "Saddam Hussein");
		_labels.put("C13", "Kim Jong-un");
		_labels.put("C14", "Predictive Analytics");
		_labels.put("C15", "Irma & Harvey");
	}
	
	public double[][] getData(String filename){
	    double[][] dat;
	    _nrows = 88;
		 dat = new double[_nrows][];
		FileInputStream file = null;
		try{
			 file = new FileInputStream(filename);
			 XSSFWorkbook workbook = new XSSFWorkbook(file);
			 XSSFSheet sheet = workbook.getSheetAt(0);
			 Iterator<Row> rowIterator = sheet.iterator();
			 
			 _docs = new String[_nrows];
			 
			 int nrow = 0;
			 while (rowIterator.hasNext()) {
				 Row row = rowIterator.next();
				 if (row.getRowNum() == 0) {
					 continue;
				 }
				 Iterator cells = row.cellIterator();
				 ArrayList<String> tfidf = new ArrayList();
				 while (cells.hasNext()) {
					 XSSFCell cell = (XSSFCell) cells.next();
					 tfidf.add(cell.toString());
				 }
				 
				 _docs[nrow] = tfidf.get(0).toString();
				 
				 _ndims = tfidf.size() -1;
				/* for (int i=0; i<_nrows; i++)
				 {
					 _data[i] = new double[_ndims];
				 }*/
				 double[] data_instance = new double[_ndims];
				 for(int i = 1; i<_ndims;i++){
					 data_instance[i - 1] = Double.parseDouble(tfidf.get(i));
					 //_data[nrow][i - 1] = Double.parseDouble(tfidf.get(i));
				 }
				 dat[nrow] = data_instance;
				 nrow++;
			 }
				workbook.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
		}
		return dat;
	}
	
	public double cosineSimilarity(double[] vectorA, double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    for (int i = 0; i < vectorA.length; i++) {
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }   
	    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
	
	public String[] findKNearestNeighbors(double[] testVector,double[][] trainingdata,int K, int metric){
		//Update KNN: take the case when testVector has multiple neighbors with the same distance into consideration 
		//Solution: Update the size of container holding the neighbors 
		String[] neighbors = new String[K];
		double[] distance = new double[trainingdata.length]; 
		//initialization, put the first K training Records into the neighbors array 
		int index;
		
		for(index = 0; index < K; index++){
			distance[index] = cosineSimilarity(trainingdata[index], testVector);
			neighbors[index] = _docs[index];
		} 
		 
		//go through the remaining records in the trainingSet to find K nearest neighbors 
		for(index = K; index < trainingdata.length; index ++){
			distance[index] = cosineSimilarity(trainingdata[index], testVector);
			//get the index of the neighbor with the largest distance to testVector 
			int maxIndex = 0;
			for(int i = 1; i < K; i ++){
				if(distance[i] < distance[maxIndex])
					maxIndex = i;
			} 
			 
			//add the current trainingData[index] into neighbors if applicable 
			if(distance[maxIndex] < distance[index])
			{
				distance[maxIndex] = distance[index];
				neighbors[maxIndex] = _docs[index];
			}
		} 
		return neighbors;
	}
	
	public String classify(String[] neighbors){
		//Stores the number of occurrences of each class label in the K nearest neighbors
		HashMap<String,Integer> freq = new HashMap<String,Integer>();
		
		//Calculate the number of occurrences of each class label in the K nearest neighbors
		for(int i = 0;i< neighbors.length;i++){
			if(freq.containsKey(_labels.get(neighbors[i]))){
				freq.put(_labels.get(neighbors[i]),freq.get(_labels.get(neighbors[i]))+1);
			}
			else{
				freq.put(_labels.get(neighbors[i]),1);
			}	
		}
		
		//Calculate the class with the maximum number of occurrences in the K-nearest neighbors
		
		int max = 0;
		String class_label="";
		for(Entry<String,Integer> entry : freq.entrySet())
		{
			if(max < entry.getValue()){
				max = entry.getValue();
				class_label = entry.getKey();
			}
		}
		return class_label;
	}
	
	/*public static void main(String[] args) {
		Knearest obj = new Knearest();
		TFIDFCalculator d2v = new TFIDFCalculator();
		d2v.getTFIDF();//creates the training data by converting the whole dataset to tfidf 
		obj._data = obj.getData("knntfidf.xlsx");// stores the training data in a 2d array for further processing
		
		//double[][] testvec = obj.getData("testtfidf.xlsx");
		//The below lineConverts the raw document to a vector and stores it in a double array
		double [] docvec = d2v.doc2vec("C:\\Users\\JYOTIRMOY\\Desktop\\Predictive Analytics\\HW3_Posted\\testdata\\C15\\c15_2.txt");
		
		//The below line computes the distance between the test vector with the training dataset
		//and returns the k nearest neighbors
		String[] docs = obj.findKNearestNeighbors(docvec,obj._data, 4, 0);
		//Printing the K nearest neighbors
		for(String doc:docs){
			System.out.println(doc);
		}
		for(int i = 0;i<docs.length;i++){
			docs[i] = d2v.folder.get(docs[i]);
			System.out.println(docs[i]);
		}
			
			//The K nearest neighbors are then passed as a parameter to the clasify method
			//The classify function returns the predicted class label of the test document
		System.out.println(obj.classify(docs));
		
		
		
		
	}*/

}
