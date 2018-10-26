package cz.cuni.mff.respefo.legacy;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.respefo.ReSpefo;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.HeaderCard;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;

@Deprecated
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
	public static void exportToASCIIFIle(PrintWriter writer, OldSpectrum spectrum) throws IOException {
		for (int i = 0; i < spectrum.size(); i++) {
			writer.print(spectrum.getX(i));
			writer.print("  ");
			writer.println(spectrum.getY(i));
		}
		if (writer.checkError()) {
			throw new IOException();
		}
		
	}

	public static void exportToFitsFile(String file, OldSpectrum spectrum) throws FitsException, IOException {
	
		double[] data = spectrum.getYSeries();

		Fits f = new Fits();
		BasicHDU<?> hdu = FitsFactory.hduFactory(data);
		
		List<String> forbiddenKeys = Arrays.asList(new String[]{"", "END", "BITPIX", "NAXIS", "NAXIS1", "EXTEND", "CRPIX1", "CRVAL1", "CDELT1", "SIMPLE"});
		
		if (spectrum.getType() == OldSpectrum.Type.FITS) {
			Cursor<String, HeaderCard> c = spectrum.getHeader().iterator();
			while (c.hasNext()) {
				HeaderCard card = (HeaderCard) c.next();
				if (!forbiddenKeys.contains(card.getKey())) {
					try {
						double value = Double.parseDouble(card.getValue());
						
						if (value == Math.rint(value)) {
							hdu.addValue(card.getKey(), (int) value, card.getComment()); 
						} else {
							hdu.addValue(card.getKey(), value, card.getComment());
						}
					} catch (NumberFormatException | NullPointerException e) {
						hdu.addValue(card.getKey(), card.getValue(), card.getComment());
					}
				}
			}
		}
		
		// hdu.addValue("BITPIX", -64, "bits per data value");
		hdu.addValue("CRPIX1", 1, "Reference pixel");
		hdu.addValue("CRVAL1", spectrum.getX(0), "Coordinate at reference pixel");
		hdu.addValue("CDELT1", spectrum.getX(1) - spectrum.getX(0), "Coordinate increment");
		f.addHDU(hdu);
		f.getHDU(0).addValue("SIMPLE", true, "Created by reSpefo v" + ReSpefo.version + " on " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));

		BufferedFile bf = new BufferedFile(file, "rw");
		f.write(bf);
		bf.close();
		f.close();
	}

	private SpectrumPrinter() {
	}
}