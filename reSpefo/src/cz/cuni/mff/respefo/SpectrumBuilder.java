package cz.cuni.mff.respefo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SpectrumBuilder {
	
	/**
	 * Imports the spectrum from an ordinary ASCII file with the specified file path.
	 * 
	 * @param file {@code String} path to the file
	 * @return returns the {@code Spectrum} object or {@code null} if it encounters any errors
	 */
	public static Spectrum importFromASCIIFile(String file) {
		double[] XSeries;
		double[] YSeries;

		try {
			List<String> lines = Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);

			int n = lines.size();

			XSeries = new double[n];
			YSeries = new double[n];

			double X, Y;
			String[] tokens;

			int i = 0;

			for (String line : lines) {
				tokens = line.trim().replaceAll(" +", " ").split(" ");

				X = Double.valueOf(tokens[0]);
				Y = Double.valueOf(tokens[1]);

				XSeries[i] = X;
				YSeries[i++] = Y;
			}
		} catch (Exception e) {
			return null;
		}
		return new Spectrum(XSeries, YSeries);
	}
}