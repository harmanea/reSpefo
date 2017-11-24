package cz.cuni.mff.respefo;

import java.io.PrintWriter;

public class SpectrumPrinter {
	
	/**
	 * Exports the provided spectrum data into an ordinary ASCII file defined by the writer parameter.
	 * <p>
	 * Note: This function does NOT close the provided {@code PrintWriter}.
	 * 
	 * @param writer the {@code PrintWriter} to export to
	 * @param data the spectrum data to be exported
	 */
	public static void exportToASCIIFIle(PrintWriter writer, Point[] data) {
		for (Point p : data) {
			writer.print(p.getX());
			writer.print("  ");
			writer.println(p.getY());
		}
		if (writer.checkError()) {
			System.out.println("Error ocurred while printing to file.");
		}
	}
}