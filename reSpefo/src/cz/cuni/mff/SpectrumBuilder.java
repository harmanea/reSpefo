package cz.cuni.mff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

public class SpectrumBuilder {
	
	/**
	 * Imports the spectrum from an ordinary ASCII file mediated by the {@code FileReader} fr.
	 * <p>
	 * Note: This function does NOT close the provided {@code FileReader}.
	 * 
	 * @param fr mediates the export
	 * @return returns the spectrum data as an array of {@code Point[]} or {@code null} if it encounters any errors
	 */
	public static Point[] importFromASCIIFile(FileReader fr) {
		Vector<Point> data = new Vector<>();
		
		try(BufferedReader br = new BufferedReader(fr)) {
			
			double X, Y;
			
		    String line = br.readLine();
		    while (line != null) {
		    	String[] tokens = line.trim().replaceAll(" +", " ").split(" ");
		    	
		    	X = Double.valueOf(tokens[0]);
		    	Y = Double.valueOf(tokens[1]);
		    	
		    	data.add(new Point(X,Y));
		    	
		        line = br.readLine();
		    }
		} catch  (Exception e) {
			return null;
		}
		return data.toArray(new Point[data.size()]);
	}
}