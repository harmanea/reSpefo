package cz.cuni.mff.respefo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.IntStream;

import org.eclipse.swt.SWT;

import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.Message;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageHDU;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;

public class FitsSpectrum extends Spectrum {
	private static final String[] FILE_EXTENSIONS = {"fits", "fit", "fts"};
	
	private Header header;

	public FitsSpectrum(String fileName) throws SpefoException, FitsException {
		super(fileName);
		LOGGER.log(Level.FINEST, "Creating a new FitsSpectrum (" + name + ")");
		
		try (Fits f = new Fits(fileName)) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			BasicHDU<?>[] HDUs = f.read();
			
			if (HDUs.length == 0) {
				throw new SpefoException("There are no HDUs in the file.");
			} else if (HDUs.length > 1) {
				LOGGER.log(Level.INFO, "There are more than one HDUs in the file. The first ImageHDU will be chosen.");
			}
			
			ImageHDU imageHdu = (ImageHDU) Arrays.stream(HDUs).filter(hdu -> hdu instanceof ImageHDU)
					.findFirst().orElseThrow(() -> new SpefoException("No ImageHDU in the FITS file."));
			
			Object data = imageHdu.getKernel();
			
			if (data == null || !data.getClass().isArray()) {
				throw new SpefoException("The HDU does not contain array data.");
			}
			
			int nDims = 1 + data.getClass().getName().lastIndexOf('[');
			
			if (nDims > 1) {
				throw new SpefoException("The data array is " + nDims + "-dimensional.");
			}
			
			switch (imageHdu.getBitPix()) {
			case BasicHDU.BITPIX_DOUBLE:
				ySeries = (double[]) data;
				break;
			case BasicHDU.BITPIX_FLOAT:
				ySeries = IntStream.range(0, ((float[]) data).length).mapToDouble(j -> ((float[]) data)[j]).toArray();
				break;
			case BasicHDU.BITPIX_INT:
				ySeries = IntStream.range(0, ((int[]) data).length).mapToDouble(j -> ((int[]) data)[j]).toArray();
				break;
			case BasicHDU.BITPIX_SHORT:
				ySeries = IntStream.range(0, ((short[]) data).length).mapToDouble(j -> ((short[]) data)[j]).toArray();
				break;
			case BasicHDU.BITPIX_LONG:
				ySeries = IntStream.range(0, ((long[]) data).length).mapToDouble(j -> ((long[]) data)[j]).toArray();
				break;
			case BasicHDU.BITPIX_BYTE:
				ySeries = IntStream.range(0, ((byte[]) data).length).mapToDouble(j -> ((byte[]) data)[j]).toArray();
				break;
			default:
				throw new SpefoException("Data is not of a valid value type.");
			}
			
			double CRPIX = imageHdu.getHeader().getDoubleValue("CRPIX1", 1);
			double CDELT = imageHdu.getHeader().getDoubleValue("CDELT1", 1);
			double CRVAL = imageHdu.getHeader().getDoubleValue("CRVAL1", 0);
			
			String ctype = imageHdu.getHeader().getStringValue("CTYPE1");
			String bunit = imageHdu.getHeader().getStringValue("BUNIT");
			
			if (ctype != null) {
				xLabel = ctype;
			}
			
			if (bunit != null) {
				yLabel = bunit;
			}
			
			xSeries = ArrayUtils.fillArray(ySeries.length, (1 - CRPIX) * CDELT + CRVAL, CDELT);
			
			header = imageHdu.getHeader();
		} catch (IOException | ClassCastException exception) {
			LOGGER.log(Level.WARNING, "Error while reading file", exception);
			throw new SpefoException(exception.getClass().getName() + " occurred!");
		}
	}
	
	@Override
	public String[] getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	@Override
	public boolean exportToAscii(String fileName) {
		if (Message.question("By saving a FITS file to an ASCII file you lose the header information. Do you want to dump the header into a separate file?") == SWT.YES) {
			String headerFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".header";
			try (PrintStream ps = new PrintStream(headerFile)) {
				header.dumpHeader(ps);
				
				if (ps.checkError()) {
					throw new IOException();
				}
			} catch (IOException exception) {
				LOGGER.log(Level.WARNING, "Error while dumping the header", exception);
			}
		}
		
		try (PrintWriter writer = new PrintWriter(fileName)) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			
			for (int i = 0; i < getSize(); i++) {
				writer.print(getX(i));
				writer.print("  ");
				writer.println(getY(i));
			}
			
			if (writer.checkError()) {
				LOGGER.log(Level.WARNING, "Error while writing to file");
				return false;
			} else {
				LOGGER.log(Level.FINER, "Closing file (" + xSeries.length + "lines written)");
				return true;
			}
			
		} catch (FileNotFoundException exception) {
			LOGGER.log(Level.WARNING, "Error while writing to file", exception);
			return false;
		}
	}

	// TODO this could be more elegant
	private static final List<String> ignoredKeys = Arrays.asList(new String[]{"", "END", "BITPIX", "NAXIS", "NAXIS1", "EXTEND", "CRPIX1", "CRVAL1", "CDELT1", "SIMPLE"});
	
	@Override
	public boolean exportToFits(String fileName) {
		double[] data = getYSeries();

		BasicHDU<?> hdu;
		try (Fits fits = new Fits(); BufferedFile bf = new BufferedFile(fileName, "rw")) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			hdu = FitsFactory.hduFactory(data);
			
			Cursor<String, HeaderCard> cursor = header.iterator();
			while (cursor.hasNext()) {
				HeaderCard card = (HeaderCard) cursor.next();
				if (!ignoredKeys.contains(card.getKey())) {
					try {
						double value = Double.parseDouble(card.getValue());
						
						if (Double.isFinite(value) && value == Math.rint(value)) { // is integer
							LOGGER.log(Level.FINEST, value + " is an integer");
							hdu.addValue(card.getKey(), (int) value, card.getComment()); 
						} else {
							LOGGER.log(Level.FINEST, value + " is a double");
							hdu.addValue(card.getKey(), value, card.getComment());
						}
					} catch (NumberFormatException | NullPointerException e) {
						LOGGER.log(Level.FINEST, card.getValue() + " is a string");
						hdu.addValue(card.getKey(), card.getValue(), card.getComment());
					}
				} else {
					LOGGER.log(Level.FINEST, card.getKey() + " was ignored");
				}
			}
			
			hdu.addValue("CRPIX1", 1, "Reference pixel");
			hdu.addValue("CRVAL1", getX(0), "Coordinate at reference pixel");
			hdu.addValue("CDELT1", getX(1) - getX(0), "Coordinate increment");
			fits.addHDU(hdu);
			try {
				fits.getHDU(0).addValue("SIMPLE", true, "Created by reSpefo v" + Version.toFullString() + " on " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			} catch (IOException e) {
				LOGGER.log(Level.FINEST, "Couldn't change the SIMPLE value", e);
			}

			fits.write(bf);
			
			LOGGER.log(Level.FINER, "Closing file");
			return true;
		} catch (FitsException | IOException e) {
			LOGGER.log(Level.WARNING, "Error while writing to file", e);
			return false;
		}
	}

	public Header getHeader() {
		return header;
	}
	
	public int getExpTime() {
		int exp = header.getBigDecimalValue("EXPTIME").intValue();
		return exp;
	}
	
	public String getDate() {
		String date = header.getStringValue("DATE-OBS");
		return date;
	}
}
