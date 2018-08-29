import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;


public class ApplySVD {
	  // Data members
	  double [][] _data; // Array of all records in dataset
	  private int _nrows, _ndims; // the number of rows and dimensions
	  String [] _docs = new String[122];
	
	  public void getData(){
		String filename = "docvec2.xlsx";
		FileInputStream file = null;
		try{
			 file = new FileInputStream(filename);
			 XSSFWorkbook workbook = new XSSFWorkbook(file);
			 XSSFSheet sheet = workbook.getSheetAt(0);
			 Iterator<Row> rowIterator = sheet.iterator();
			 _nrows = 122;
			 _data = new double[_nrows][];
			 int nrow = 0;
			 while (rowIterator.hasNext()) {
				 Row row = rowIterator.next();
				 if (row.getRowNum() == 0) {
					 continue;
				 }
				 Iterator cells = row.cellIterator();
				 ArrayList<String> tfidf = new ArrayList<String>();
				 while (cells.hasNext()) {
					 XSSFCell cell = (XSSFCell) cells.next();
					 tfidf.add(cell.toString());
				 }
				 _docs[nrow] = tfidf.get(0).toString();
				 _ndims = tfidf.size() -2;
				 double[] data_instance = new double[_ndims];
				 for(int i = 1; i<_ndims;i++){
					 data_instance[i - 1] = Double.parseDouble(tfidf.get(i));
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
	  
	public void writeToFile(SimpleMatrix dat, KMeans KM){
		File[] directories = new File("C:\\Users\\JYOTIRMOY\\Desktop\\Predictive Analytics\\HW1\\HW1\\DataSet\\DataSet").listFiles(File::isDirectory);
		int x = 1;
		HashMap<String, String> docmap = new HashMap<String, String>();
		for(File directory:directories){
			for(File s :directory.listFiles()){
				docmap.put("Document" + x, directory.getName());
				x++;
			}
		}
		PrintWriter writer;
		try {
			writer = new PrintWriter("plot.txt", "UTF-8");
			for(int i = 0; i < KM._label.length; i++){
				writer.println(docmap.get(_docs[i])+", "+ _docs[i]+", "+KM._label[i] +", "+dat.get(i,0) + ", " + dat.get(i,1));
			}
			writer.close();
		}catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (UnsupportedEncodingException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}
	
	public void writeSVD(SimpleMatrix dat){
		PrintWriter writer;
		try {
			writer = new PrintWriter("SVD.txt", "UTF-8");
			//writer.println("Label, PCA1, PCA2");
			/*for(int i = 0; i < 122; i++){
				writer.println(dat.get(i,0) + ", " + dat.get(i,1));
			}*/
			writer.println(dat);
			writer.close();
		}catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (UnsupportedEncodingException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}
	
	public void writeDistMatrix(double[][] dm, KMeans KM){
		PrintWriter writer;
		try{
			writer = new PrintWriter("cosine.txt", "UTF-8");
			for(int i=0;i<KM._numClusters;i++){
				writer.print(", "+i);
			}
			writer.println();
			for(int i=0;i < dm.length; i++){
				writer.print(_docs[i] + ", ");
				int j;
				for(j=0;j<KM._numClusters - 1;j++){
					writer.print(dm[i][j] +", ");
				}
				writer.println(dm[i][j]);
			}
			writer.close();
		}catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (UnsupportedEncodingException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}
	
	public SimpleMatrix impSVD(){
		SimpleMatrix matA = new SimpleMatrix(_data);
		SimpleSVD<SimpleMatrix> s = matA.svd();
		SimpleMatrix U=s.getU(),W=s.getW(),V=s.getV();
		int count = 0;
		for(int i=0;i<W.numRows();i++){
			if(W.get(i, i)>0.1){
				count++;
			}
		}
		double[][] reducedW = new double[count][count];
		for(int i=0;i<count;i++){
			for(int j=0;j<count;j++){
				reducedW[i][j] = W.get(i, j);
			}
		}
		SimpleMatrix redW = new SimpleMatrix(reducedW);
		return (U.extractMatrix(0, U.numRows(), 0, count).mult(redW)).mult(V.extractMatrix(0, V.numRows(), 0, count).transpose());
	}
	
	public SimpleMatrix computeSVD(){
		SimpleMatrix matA = new SimpleMatrix(_data);
		
		SimpleSVD<SimpleMatrix> s = matA.svd();
		SimpleMatrix U=s.getU(),W=s.getW();
		double[][] reducedW = new double[2][2];
		for(int i=0;i<2;i++){
			for(int j=0;j<2;j++){
				reducedW[i][j] = W.get(i, j);
			}
		}
		SimpleMatrix redW = new SimpleMatrix(reducedW);
		return U.extractMatrix(0, 122, 0, 2).mult(redW);
	}
	
	public static void main(String args[]){
		ApplySVD a =new ApplySVD();
		a.getData();
		SimpleMatrix oData = a.impSVD();
		a.writeSVD(oData);
		SimpleMatrix rData = a.computeSVD();
		KMeans KM = new KMeans(null );
	    KM.clustering(15, 10, null);
	    a.writeToFile(rData, KM);
	    double[][] dm = KM.distanceMatrix("cosine");
	    a.writeDistMatrix(dm, KM);
	    
	}
}
