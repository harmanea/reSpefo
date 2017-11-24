package cz.cuni.mff;

import java.io.PrintWriter;

public class SpectrumPrinter {
	public static void printToASCIIFIle(PrintWriter writer, Point[] data) {
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