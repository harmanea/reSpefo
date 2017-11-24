package cz.cuni.mff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

public class SpectrumBuilder {
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
			System.out.println("Error: Couldn't load file.");
			return null;
		}
		return data.toArray(new Point[data.size()]);
	}
}