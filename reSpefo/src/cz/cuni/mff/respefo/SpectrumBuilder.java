package cz.cuni.mff.respefo;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SpectrumBuilder {
	
	/**
	 * Imports the spectrum from an ordinary ASCII file with the specified file path. Only points with X coordinate in the specified range will be loaded.
	 * 
	 * @param file {@code String} path to the file
	 * @param fromX minimal {@code double} value of the X coordinate
	 * @param toX maximal {@code double} value of the X coordinate
	 * @return returns the {@code Spectrum} object or {@code null} if it encounters any errors
	 */
	public static Spectrum importFromASCIIFile(String file, double fromX, double toX) {
		double[] XSeries;
		double[] YSeries;
		
		String name;

		int i;
		
		try {
			List<String> lines = Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);

			int n = lines.size();

			XSeries = new double[n];
			YSeries = new double[n];

			double X, Y;
			String[] tokens;

			i = 0;

			if (fromX <= toX) {
				for (String line : lines) {
					tokens = line.trim().replaceAll(" +", " ").split(" ");
	
					X = Double.valueOf(tokens[0]);
					if ((X > fromX) && (X < toX)) {
						Y = Double.valueOf(tokens[1]);
		
						XSeries[i] = X;
						YSeries[i++] = Y;
					}
				}
			}
			
			name = file.substring(file.lastIndexOf(File.separatorChar) + 1);
			name = name.substring(0, name.lastIndexOf('.'));
		} catch (Exception e) {
			return null;
		}
		return new Spectrum(Arrays.copyOfRange(XSeries, 0, i), Arrays.copyOfRange(YSeries, 0, i), name);
	}
	
	/**
	 * Imports the spectrum from an ordinary ASCII file with the specified file path.
	 * 
	 * @param file {@code String} path to the file
	 * @return returns the {@code Spectrum} object or {@code null} if it encounters any errors
	 */
	public static Spectrum importFromASCIIFile(String file) {
		return importFromASCIIFile(file, Double.MIN_VALUE, Double.MAX_VALUE);
	}
}