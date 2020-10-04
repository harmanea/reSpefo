package cz.cuni.mff.respefo.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.dialog.OverwriteDialog;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.StringUtils;

public class EWResultsPrinter {
	private static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	public static boolean printResults(Spectrum spectrum, Measurements measurements, MeasurementResults results) {
		boolean append = false;
		
		String fileNamePart = FileUtils.getFilterPath() + File.separator + spectrum.getName();
		File file = new File(fileNamePart + ".eqw");
		if (file.exists()) {
			int choice = new OverwriteDialog(ReSpefo.getShell()).open();
			if (choice == 0) {
				return false;

			} else if (choice == 2) {
				append = true;

			} else if (choice == 3) {
				file = FileUtils.firstUniqueFileName(fileNamePart, "eqw");
			}
		}
		
		try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, append)))) {
			if (!append) {
				writer.println("# Summary of equivalent widths etc. measured on " + spectrum.getName());
				writer.println("_This file was generated automatically, do not edit!_");
			}
			
			for (int i = 0; i < measurements.getCount(); i++) {
				Measurement measurement = measurements.getAt(i);
				MeasurementResult result = results.get(i);
				
				double ew = spectrum.getX(result.getRight()) - spectrum.getX(result.getLeft());
				for (int index = result.getLeft(); index < result.getRight(); index++) {
					ew -= area(spectrum, index, index + 1);
				}
				
				writer.println();
				writer.println(measurement.getName());
				writer.println("  EW: " + MathUtils.formatDouble(ew, 4, 4));
				
				if (result.getCategories().contains("Ic")) {
					int index = result.getPoints().get(result.getCategories().indexOf("Ic"));
					double halfIntensity = (1 + spectrum.getY(index)) / 2;
					
					int leftIndex = -1;
					for (int j = index; j > result.getLeft(); j--) {
						if (spectrum.getY(j) > halfIntensity) {
							leftIndex = j;
							break;
						}
					}
					
					int rightIndex = -1;
					for (int j = index; j < result.getRight(); j++) {
						if (spectrum.getY(j) > halfIntensity) {
							rightIndex = j;
							break;
						}
					}
					
					if (leftIndex >= 0 && rightIndex >= 0) {
						double leftValue = MathUtils.linearInterpolation(spectrum.getY(leftIndex), spectrum.getX(leftIndex), spectrum.getY(leftIndex + 1), spectrum.getX(leftIndex + 1), halfIntensity);
						double rightValue = MathUtils.linearInterpolation(spectrum.getY(rightIndex - 1), spectrum.getX(rightIndex - 1), spectrum.getY(rightIndex), spectrum.getX(rightIndex), halfIntensity);
						
						double fwhm = rightValue - leftValue;
						writer.println("FWHM: " + MathUtils.formatDouble(fwhm, 4, 4));
					} else {
						LOGGER.log(Level.WARNING, "Couldn't compute FWHM, half intensity: " + halfIntensity);
					}
				}
				
				for (int j = 0; j < result.size(); j++) {
					writer.println(StringUtils.trimmedOrPaddedString(result.getCategories().get(j), 4) + ": " 
							+ MathUtils.formatDouble(spectrum.getY(result.getPoints().get(j)), 4, 4));
				}
			}
		
			return true;
			
		} catch (IOException exception) {
			Message.error(ReSpefo.getShell(), "Couldn't save to file.", exception);
			return false;
		}
	}
	
	private static double area(Spectrum spectrum, int left, int right) {
		double leftBase = spectrum.getY(left);
		double rightBase = spectrum.getY(right);
		double height = spectrum.getX(right) - spectrum.getX(left);
		
		return (leftBase + rightBase) * height / 2;
	}
}
