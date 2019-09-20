package cz.cuni.mff.respefo.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.SpefoException;

public class RVResultsPrinter {
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
	 * @return True if successful, False otherwise
	 */
	public boolean printResults(Spectrum spectrum) {	
		File file = FileUtils.firstUniqueFileName(FileUtils.getFilterPath() + File.separator + spectrum.getName(), "rvr");
		
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			writer.println("# Summary of radial velocities measured on " + spectrum.getName());
			writer.println("_This file was generated automatically, do not edit!_");
			writer.println();
			writer.println(results.getRvCorr().toRvResultLine());
			writer.println();
		
			for (String category : results.getCategories()) {
				writer.println("## Results for category " + category + ":");
				writer.println("    rv      radius     lambda     name comment");

				ArrayList<Double> values = new ArrayList<>();
				
				for (RVResult result : results.getResultsOfCategory(category)) {
					writer.println(MathUtils.formatDouble(result.rV, 4, 4) + ' ' +
							MathUtils.formatDouble(result.radius, 4, 4, false) + ' ' +
							MathUtils.formatDouble(result.l0, 8, 4, false) + ' ' +
							result.name.replace(" ", "_") + ' ' +
							result.comment);
					
					values.add(result.rV);
				}
				if (values.size() > 1) {
					double mean = results.getRvOfCategory(category);
					
					if (values.size() < 5) {
						writer.println(String.format("mean RV: %f", + MathUtils.round(mean, 4)));
					} else {
						writer.println(String.format("robust mean RV: %f (%d values)", MathUtils.round(mean, 4), values.size()));
					}
					
					writer.println("rmse: " + MathUtils.round(MathUtils.rmse(values.stream().mapToDouble(Double::doubleValue).toArray(), mean),4));
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
