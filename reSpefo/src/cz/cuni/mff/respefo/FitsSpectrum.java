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
import org.eclipse.swt.widgets.MessageBox;

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

	public FitsSpectrum(String fileName) throws SpefoException {
		super(fileName);
		LOGGER.log(Level.FINEST, "Creating a new FitsSpectrum (" + name + ")");
		
		try (Fits f = new Fits(fileName)) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			BasicHDU<?>[] HDUs = f.read();
			
			for (int i = 0; i < HDUs.length; i++) {
				if (HDUs[i] instanceof ImageHDU) {
					Object data = HDUs[i].getKernel();

					// TODO this could be more elegant
					switch (HDUs[i].getBitPix()) {
					case BasicHDU.BITPIX_DOUBLE:
						if (data instanceof double[]) {
							ySeries = (double[]) data;
						} else {
							LOGGER.log(Level.WARNING, "[double] more than 1D array");
							throw new SpefoException("[double] more than 1D array");
						}
						break;
					case BasicHDU.BITPIX_FLOAT:
						if (data instanceof float[]) {
							ySeries = IntStream.range(0, ((float[]) data).length).mapToDouble(j -> ((float[]) data)[j]).toArray();
						} else {
							LOGGER.log(Level.WARNING, "[float] more than 1D array");
							throw new SpefoException("[float] more than 1D array");
						}
						break;
					case BasicHDU.BITPIX_INT:
						if (data instanceof int[]) {
							ySeries = IntStream.range(0, ((int[]) data).length).mapToDouble(j -> ((int[]) data)[j]).toArray();
						} else {
							LOGGER.log(Level.WARNING, "[int] more than 1D array");
							throw new SpefoException("[int] more than 1D array");
						}
						break;
					case BasicHDU.BITPIX_SHORT:
						if (data instanceof short[]) {
							ySeries = IntStream.range(0, ((short[]) data).length).mapToDouble(j -> ((short[]) data)[j]).toArray();
						} else {
							LOGGER.log(Level.WARNING, "[short] more than 1D array");
							throw new SpefoException("[short] more than 1D array");
						}
						break;
					case BasicHDU.BITPIX_LONG:
						if (data instanceof long[]) {
							ySeries = IntStream.range(0, ((long[]) data).length).mapToDouble(j -> ((long[]) data)[j]).toArray();
						} else {
							LOGGER.log(Level.WARNING, "[long] more than 1D array");
							throw new SpefoException("[long] more than 1D array");
						}
						break;
					case BasicHDU.BITPIX_BYTE:
						if (data instanceof byte[]) {
							ySeries = IntStream.range(0, ((byte[]) data).length).mapToDouble(j -> ((byte[]) data)[j]).toArray();
						} else {
							LOGGER.log(Level.WARNING, "[byte] more than 1D array");
							throw new SpefoException("[byte] more than 1D array");
						}
					default:
						LOGGER.log(Level.WARNING, "Not a valid value type");
						throw new SpefoException("not a valid value type");
					}
									
					double CRPIX = HDUs[i].getHeader().getDoubleValue("CRPIX" + (i + 1), 1);
					double CDELT = HDUs[i].getHeader().getDoubleValue("CDELT" + (i + 1), 1);
					double CRVAL = HDUs[i].getHeader().getDoubleValue("CRVAL" + (i + 1), 0);
					
					String ctype = HDUs[i].getHeader().getStringValue("CTYPE1");
					String bunit = HDUs[i].getHeader().getStringValue("BUNIT");
					
					if (ctype != null) {
						xLabel = ctype;
					}
					
					if (bunit != null) {
						yLabel = bunit;
					}
					
					xSeries = Util.fillArray(ySeries.length, (1 - CRPIX) * CDELT + CRVAL, CDELT);
					
					header = HDUs[i].getHeader();
					
					LOGGER.log(Level.FINER, "Closing file");
					return;
				}
			}
			
			LOGGER.log(Level.WARNING, "No ImageHDU in the FITS file");
			throw new SpefoException("No ImageHDU in the FITS file");
		} catch (IOException | FitsException | ClassCastException e) {
			LOGGER.log(Level.WARNING, "Error while reading file", e);
			// TODO specialized exception?
			throw new SpefoException(e.getClass().getName() + " occurred!");
		}
	}
	
	@Override
	public String[] getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	@Override
	public boolean exportToAscii(String fileName) {
		MessageBox mb = new MessageBox(ReSpefo.getShell(),SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		mb.setMessage("By saving a FITS file to an ASCII file you lose the header information. Do you want to dump the header into a separate file?");
		if (mb.open() == SWT.YES) {
			String headerFile = fileName.substring(0, fileName.lastIndexOf('.')) + ".header";
			try (PrintStream ps = new PrintStream(headerFile)) {
				header.dumpHeader(ps);
				
				if (ps.checkError()) {
					throw new IOException();
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Error while dumping the header", e);
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
			
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "Error while writing to file", e);
			return false;
		}
	}

	// TODO this could be more elegant
	private static List<String> ignoredKeys = Arrays.asList(new String[]{"", "END", "BITPIX", "NAXIS", "NAXIS1", "EXTEND", "CRPIX1", "CRVAL1", "CDELT1", "SIMPLE"});
	
	@Override
	public boolean exportToFits(String fileName) {
		double[] data = getYSeries();

		BasicHDU<?> hdu;
		try (Fits f = new Fits(); BufferedFile bf = new BufferedFile(fileName, "rw")) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			hdu = FitsFactory.hduFactory(data);
			
			Cursor<String, HeaderCard> c = header.iterator();
			while (c.hasNext()) {
				HeaderCard card = (HeaderCard) c.next();
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
			f.addHDU(hdu);
			try {
				f.getHDU(0).addValue("SIMPLE", true, "Created by reSpefo v" + ReSpefo.version + " on " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			} catch (IOException e) {
				LOGGER.log(Level.FINEST, "Couldn't change the SIMPLE value", e);
			}

			f.write(bf);
			
			LOGGER.log(Level.FINER, "Closing file");
			return true;
		} catch (FitsException | IOException e) {
			LOGGER.log(Level.WARNING, "Error while writing to file", e);
			return false;
		}
	}

}
