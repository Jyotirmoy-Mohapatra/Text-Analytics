import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.distance.CosineSimilarity;
import net.sf.javaml.distance.DistanceMeasure;
import net.sf.javaml.tools.data.FileHandler;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.commons.*;

public class LibCluster {
	/**
     * Tests the k-means algorithm with default parameter settings.
     */
    public static void main(String[] args) throws Exception {

        /* Load a dataset */
    	Dataset data = new DefaultDataset();
    	String filename = "docvec2.xlsx";
    	FileInputStream file = null;
    	try{
    		 file = new FileInputStream(filename);
    		 XSSFWorkbook workbook = new XSSFWorkbook(file);
    		 XSSFSheet sheet = workbook.getSheetAt(0);
    		 Iterator<Row> rowIterator = sheet.iterator();
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
    			 double[] data_instance = new double[tfidf.size()-2];
    			 for(int i = 1; i<tfidf.size()-2;i++){
    				 data_instance[i] = Double.parseDouble(tfidf.get(i));
    			 }
    			 Instance instance = new SparseInstance(data_instance);
    			 data.add(instance);
    		 }
 			workbook.close();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	finally {
    		if (file != null) {
    			file.close();
    			}
    	}
        /*
         * Create a new instance of the KMeans algorithm, with no options
         * specified. By default this will generate 4 clusters.
         */
    	CosineSimilarity cs = new CosineSimilarity();
    	System.out.println("Reached");
        Clusterer km = new KMeans(15,10,cs);
        /*
         * Cluster the data, it will be returned as an array of data sets, with
         * each dataset representing a cluster
         */
        Dataset[] clusters = km.cluster(data);
        System.out.println("Cluster count: " + clusters.length);
        for(int i=0;i<15;i++){
        	System.out.println(clusters[i].size());
        }

    }
}

