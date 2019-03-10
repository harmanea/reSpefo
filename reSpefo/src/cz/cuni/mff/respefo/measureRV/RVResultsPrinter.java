package cz.cuni.mff.respefo.measureRV;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.Spectrum;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.util.MathUtils;

class RVResultsPrinter {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	private RVResults results;
	
	/**
	 * Creates a new instance and assign it with results
	 * @param results
	 */
	public RVResultsPrinter(RVResults results) {
		this.results = results;
	}
	
	/**
	 * Prints the results for a spectrum
	 * @param spectrum
	 * @return True if successful, False if not
	 */
	public boolean printResults(Spectrum spectrum) {	
		File file = new File(ReSpefo.getFilterPath(), spectrum.getName() + ".rvr"); 
		for (int num = 1; file.exists(); num++) {
		    file = new File(ReSpefo.getFilterPath(), spectrum.getName() + "(" + num + ").rvr");
		}
		
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println("# Summary of radial velocities measured on " + spectrum.getName());
			writer.println("_This file was generated automatically, do not edit!_");
			writer.println();
			writer.println(results.getRvCorr().toRvResultLine());
			writer.println();
		
			for (String category : results.getCategories()) {
				writer.println("## Results for category " + category + ":");
				writer.println("    rv    \t radius \t   lambda    \tname\tcomment");

				ArrayList<Double> values = new ArrayList<>();
				
				for (RVResult result : results.getResultsOfCategory(category)) {
					writer.println(MathUtils.formatDouble(result.rV, 4, 4) + '\t' +
							MathUtils.formatDouble(result.radius, 4, 4, false) + '\t' +
							MathUtils.formatDouble(result.l0, 8, 4, false) + '\t' +
							result.name + '\t' +
							result.comment);
					
					values.add(result.rV);
				}
				if (values.size() > 1) {
					double average = values.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
					writer.println("Average RV: " + MathUtils.round(average, 4) + "\t (" + values.size() + " values)");
					writer.println("rms: " + MathUtils.round(MathUtils.rms(values.stream().mapToDouble(Double::doubleValue).toArray(), average),4));
				}
				writer.println();
			}
			
		} catch (FileNotFoundException | SpefoException exception) {
			LOGGER.log(Level.WARNING, "Couldn't save to file.", exception);
			return false;
		}
		
		return true;
	}
}
