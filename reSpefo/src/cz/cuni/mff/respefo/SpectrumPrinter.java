package cz.cuni.mff.respefo;

import java.io.IOException;
import java.io.PrintWriter;

import nom.tam.fits.AsciiTable;
import nom.tam.fits.AsciiTableHDU;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.TableHDU;
import nom.tam.util.BufferedFile;

public final class SpectrumPrinter {

	/**
	 * Exports the provided spectrum data into an ordinary ASCII file defined by the
	 * writer parameter.
	 * <p>
	 * Note: This function does NOT close the provided {@code PrintWriter}.
	 * 
	 * @param writer
	 *            {@code PrintWriter} to export to
	 * @param spectrum
	 *            {@code Spectrum} object to be exported
	 * @throws IOException
	 *             if a problem occurred while printing to file
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

	public static void exportToFitsFile(String file, Spectrum spectrum) throws FitsException, IOException {
		//AsciiTableHDU atb = new AsciiTableHDU(new Header(), new AsciiTable());
		//atb.addColumn(spectrum.getXSeries());
		//atb.addColumn(spectrum.getYSeries());
		
		double[] data = spectrum.getYSeries();

		Fits f = new Fits();
		BasicHDU hdu = FitsFactory.hduFactory(data);
		hdu.addValue("SIMPLE", true, "Created by reSpefo");
		hdu.addValue("CRPIX1", 1, "Reference pixel");
		hdu.addValue("CRVAL1", spectrum.getX(0), "Coordinate at reference pixel");
		hdu.addValue("CRDELT1", spectrum.getX(1) - spectrum.getX(0), "Coordinate increment");
		f.addHDU(hdu);

		BufferedFile bf = new BufferedFile(file, "rw");
		f.write(bf);
		bf.close();
		f.close();
	}

	private SpectrumPrinter() {
	}
}