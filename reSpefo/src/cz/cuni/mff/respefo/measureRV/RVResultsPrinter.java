package cz.cuni.mff.respefo.measureRV;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.Util;

class RVResultsPrinter {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private RVResults results;
	
	public RVResultsPrinter(RVResults results) {
		this.results = results;
	}
	
	public boolean printResults(Spectrum spectrum) {	
		File file = new File(ReSpefo.getFilterPath(), spectrum.getName() + ".rvr"); 
		for (int num = 1; file.exists(); num++) {
		    file = new File(ReSpefo.getFilterPath(), spectrum.getName() + "(" + num + ").rvr");
		}
		
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println("# Summary of radial velocities measured on " + spectrum.getName());
			writer.println("_This file was generated automatically, please do not edit_");
			writer.println();
			writer.println("Heliocentric correction: " + "[not implemented yet]");
			writer.println();
		
			for (String category : results.getCategories()) {
				writer.println("## Results for category " + category + ":");
				writer.println("rv\tradius\tlambda\tname\tcomment");
				double sum = 0;
				int count = 0;
				ArrayList<Double> values = new ArrayList<>();
				for (RVResult result : results.getResultsOfCategory(category)) {
					writer.println(result.rV + "\t"
							+ result.radius + "\t"
							+ result.l0 + "\t"
							+ result.name + "\t"
							+ result.comment);
					values.add(result.rV);
					sum += result.rV;
					count++;
				}
				if (count > 1) {
					writer.println("Average RV: " + (sum / count));
					writer.println("rms: " + Util.rms(values.stream().mapToDouble(Double::doubleValue).toArray(), (sum / count)));
				}
				writer.println();
			}
			
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "Couldn't save to file.", e);
			return false;
		}
		
		return true;
	}
}
