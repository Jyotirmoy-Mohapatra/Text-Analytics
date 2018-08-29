
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.StringReader;

import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class KMeans 
{
  // Data members
  double [][] _data; // Array of all records in dataset
  int [] _label;  // generated cluster labels
  private int [] _tempLabel;
  private int [] _withLabel; // if original labels exist, load them to _withLabel
                              // by comparing _label and _withLabel, we can compute accuracy. 
                              // However, the accuracy function is not defined yet.
  private double [][] _centroids; // centroids: the center of clusters
  private int _nrows, _ndims; // the number of rows and dimensions
  int _numClusters; // the number of clusters;

  // Constructor; loads records from file <fileName>. 
  // if labels do not exist, set labelname to null
  public KMeans(String labelname) 
  {
    
    // Creates a new KMeans object by reading in all of the records that are stored in a csv file
    
    String filename = "docvec2.xlsx";
	FileInputStream file = null;
	try{
		 file = new FileInputStream(filename);
		 XSSFWorkbook workbook = new XSSFWorkbook(file);
		 XSSFSheet sheet = workbook.getSheetAt(0);
		 Iterator<Row> rowIterator = sheet.iterator();
		 _nrows = 122;
		 _data = new double[_nrows][];
		 _label = new int[_nrows];
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
			 _ndims = tfidf.size() -2;
			/* for (int i=0; i<_nrows; i++)
			 {
				 _data[i] = new double[_ndims];
			 }*/
			 double[] data_instance = new double[_ndims];
			 for(int i = 1; i<_ndims;i++){
				 data_instance[i - 1] = Double.parseDouble(tfidf.get(i));
				 //_data[nrow][i - 1] = Double.parseDouble(tfidf.get(i));
			 }
			 _data[nrow] = data_instance;
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
	
  }
  
  // Perform k-means clustering with the specified number of clusters and
  // Eucliden distance metric. 
  // niter is the maximum number of iterations. If it is set to -1, the kmeans iteration is only terminated by the convergence condition.
  // centroids are the initial centroids. It is optional. If set to null, the initial centroids will be generated randomly.
  public void clustering(int numClusters, int niter, double [][] centroids) 
  {
      _numClusters = numClusters;
      if (centroids !=null)
          _centroids = centroids;
      else{
        // randomly selected centroids
        _centroids = new double[_numClusters][];

        ArrayList idx= new ArrayList();
        for (int i=0; i<numClusters; i++){
          int c;
          do{
            c = (int) (Math.random()*_nrows);
          }while(idx.contains(c)); // avoid duplicates
          idx.add(c);

          // copy the value from _data[c]
          _centroids[i] = new double[_ndims];
          for (int j=0; j<_ndims; j++)
            _centroids[i][j] = _data[c][j];
        }
        System.out.println("selected random centroids");

      }

      double [][] c1 = _centroids;
      double threshold = 0.001;
      double costhres = 0.8;
      int round=0;

      while (true){
        // update _centroids with the last round results
        _centroids = c1;

        //assign record to the closest centroid
        _tempLabel = new int[_nrows];
        for (int i=0; i<_nrows; i++){
        	_tempLabel[i] = closest(_data[i]);
        }
        
        // recompute centroids based on the assignments  
        c1 = updateCentroids();
        round ++;
        if ((niter >0 && round >=niter) || converge(_centroids, c1, threshold))
          break;
        
        _label = _tempLabel;
      }

      System.out.println("Clustering converges at round " + round);
  }

  // find the closest centroid for the record v 
  private int closest(double [] v){
    double mindist = cosineSimilarity(v, _centroids[0]);
    int label =0;
    for (int i=1; i<_numClusters; i++){
      double t = cosineSimilarity(v, _centroids[i]);
      if (mindist<t){
        mindist = t;
        label = i;
      }
    }
    return label;
  }

  // compute Euclidean distance between two vectors v1 and v2
  private double dist(double [] v1, double [] v2){
    double sum=0;
    for (int i=0; i<_ndims; i++){
      double d = v1[i]-v2[i];
      sum += d*d;
    }
    return Math.sqrt(sum);
  }
  
  // compute cosine similarity between two vectors vectorA and vectorB
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


  // according to the cluster labels, recompute the centroids 
  // the centroid is updated by averaging its members in the cluster.
  // this only applies to Euclidean distance as the similarity measure.

  private double [][] updateCentroids(){
    // initialize centroids and set to 0
    double [][] newc = new double [_numClusters][]; //new centroids 
    int [] counts = new int[_numClusters]; // sizes of the clusters

    // intialize
    for (int i=0; i<_numClusters; i++){
      counts[i] =0;
      newc[i] = new double [_ndims];
      for (int j=0; j<_ndims; j++)
        newc[i][j] =0;
    }


    for (int i=0; i<_nrows; i++){
      int cn = _tempLabel[i]; // the cluster membership id for record i
      for (int j=0; j<_ndims; j++){
        newc[cn][j] += _data[i][j]; // update that centroid by adding the member data record
      }
      counts[cn]++;
    }

    // finally get the average
    for (int i=0; i< _numClusters; i++){
      for (int j=0; j<_ndims; j++){
        newc[i][j]/= counts[i];
      }
    } 

    return newc;
  }

  // check convergence condition
  private boolean converge(double [][] c1, double [][] c2, double threshold){
    for(int i = 0;i<_nrows;i++){
    	if(_tempLabel[i] != _label[i]){
    		return false;
    	}
    }
    return true;
  }
  
  public double[][] distanceMatrix(String distanceMeasure){
	  double[][] distMatrix = new double[_data.length][_numClusters];
	  if(distanceMeasure.equals("euclidean"))
	  {
		  for(int i = 0; i < _data.length;i++){
			  for(int j=0;j<_numClusters;j++){
				  distMatrix[i][j] = dist(_data[i],_centroids[j]);
			  }
		  }
	  }
	  else{
		  for(int i = 0; i < _data.length;i++){
			  for(int j=0;j<_numClusters;j++){
				  distMatrix[i][j] = cosineSimilarity(_data[i],_centroids[j]);
			  }
		  }
	  }
	  return distMatrix;
  }
  public double[][] getCentroids()
  {
    return _centroids;
  }

  public int [] getLabel()
  {
    return _label;
  }

  public int nrows(){
    return _nrows;
  }

  public void printResults(){
      System.out.println("Label:");
      HashMap<Integer,Integer> lab= new HashMap<Integer, Integer>(); 
     for (int i=0; i<_nrows; i++){
    	 if(lab.containsKey(_label[i])){
    		 lab.put(_label[i], lab.get(_label[i]) + 1);
    	 }
    	 else{
    		 lab.put(_label[i], 1);
    	 }
     }
     System.out.println(lab);
  }
}