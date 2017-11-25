package cz.cuni.mff.respefo;

import java.io.IOException;
import java.io.PrintWriter;

public class SpectrumPrinter {
	
	/**
	 * Exports the provided spectrum data into an ordinary ASCII file defined by the writer parameter.
	 * <p>
	 * Note: This function does NOT close the provided {@code PrintWriter}.
	 * 
	 * @param writer {@code PrintWriter} to export to
	 * @param spectrum {@code Spectrum} object to be exported
	 * @throws IOException if a problem occurred while printing to file
	 */
	public static void exportToASCIIFIle(PrintWriter writer, Spectrum spectrum) throws IOException {
		for (int i = 0; i < spectrum.size(); i++) {
			writer.print(spectrum.getX(i));
			writer.print("  ");
			writer.println(spectrum.getY(i));
		}
		if (writer.checkError()) {
			throw new IOException();
		}
	}
}