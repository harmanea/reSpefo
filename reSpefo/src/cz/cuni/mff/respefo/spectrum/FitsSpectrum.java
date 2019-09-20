package cz.cuni.mff.respefo.spectrum;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.IntStream;

import cz.cuni.mff.respefo.Version;
import cz.cuni.mff.respefo.component.RvCorrection;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.SpefoException;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Standard;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;

public class FitsSpectrum extends Spectrum {
	private static final String[] FILE_EXTENSIONS = { "fits", "fit", "fts" };

	private Header header;
	private LocalDateTime date;

	public FitsSpectrum(String fileName) throws SpefoException, FitsException {
		super(fileName);
		
		try (Fits f = new Fits(fileName)) {
			BasicHDU<?>[] HDUs = f.read();

			if (HDUs.length == 0) {
				throw new SpefoException("There are no HDUs in the file.");
			} else if (HDUs.length > 1) {
				LOGGER.log(Level.INFO, "There are more than one HDUs in the file. The first ImageHDU will be chosen.");
			}

			ImageHDU imageHdu = (ImageHDU) Arrays.stream(HDUs).filter(hdu -> hdu instanceof ImageHDU).findFirst()
					.orElseThrow(() -> new SpefoException("No ImageHDU in the FITS file."));

			Object data = imageHdu.getKernel();
			if (data == null || !data.getClass().isArray()) {
				throw new SpefoException("The HDU does not contain array data.");
			}
			
			header = imageHdu.getHeader();

			int nDims = ArrayUtils.nDims(data);
			if (nDims == 1) {
				ySeries = getSeriesFromData(data, imageHdu.getBitPix());
				xSeries = getSeriesFromCData();
			} else if (nDims == 2) {
				int length = Array.getLength(data);
				if (length != 2) {
					throw new SpefoException("The 2-D data array is too long in the first dimension.");
				}

				getBothSeriesFromData(data, imageHdu.getBitPix());
			} else {
				throw new SpefoException("The data array is " + nDims + "-dimensional.");
			}

			double bZero = header.getDoubleValue(Standard.BZERO, 0);
			double bScale = header.getDoubleValue(Standard.BSCALE, 1);

			if (bZero != 0 || bScale != 1) {
				ySeries = ArrayUtils.applyBScale(ySeries, bZero, bScale);
			}

			parseDate();
		} catch (IOException | ClassCastException exception) {
			LOGGER.log(Level.WARNING, "Error while reading file", exception);
			throw new SpefoException(exception.getClass().getName() + " occurred!");
		}
	}

	private double[] getSeriesFromData(Object data, int bitPix) throws SpefoException {
		switch (bitPix) {
		case BasicHDU.BITPIX_DOUBLE:
			return (double[]) data;
		case BasicHDU.BITPIX_FLOAT:
			return IntStream.range(0, ((float[]) data).length).mapToDouble(j -> ((float[]) data)[j]).toArray();
		case BasicHDU.BITPIX_INT:
			return IntStream.range(0, ((int[]) data).length).mapToDouble(j -> ((int[]) data)[j]).toArray();
		case BasicHDU.BITPIX_SHORT:
			return IntStream.range(0, ((short[]) data).length).mapToDouble(j -> ((short[]) data)[j]).toArray();
		case BasicHDU.BITPIX_LONG:
			return IntStream.range(0, ((long[]) data).length).mapToDouble(j -> ((long[]) data)[j]).toArray();
		case BasicHDU.BITPIX_BYTE:
			return IntStream.range(0, ((byte[]) data).length).mapToDouble(j -> ((byte[]) data)[j]).toArray();
		default:
			throw new SpefoException("Data is not of a valid value type.");
		}
	}

	private double[] getSeriesFromCData() {
		double CRPIX = header.getDoubleValue("CRPIX1", 1);
		double CDELT = header.getDoubleValue("CDELT1", 1);
		double CRVAL = header.getDoubleValue("CRVAL1", 0);

		return ArrayUtils.fillArray(ySeries.length, (1 - CRPIX) * CDELT + CRVAL, CDELT);
	}

	private void getBothSeriesFromData(Object data, int bitPix) throws SpefoException {
		switch (bitPix) {
		case BasicHDU.BITPIX_DOUBLE:
			xSeries = getSeriesFromData(((double[][]) data)[0], bitPix);
			ySeries = getSeriesFromData(((double[][]) data)[1], bitPix);
			break;
		case BasicHDU.BITPIX_FLOAT:
			xSeries = getSeriesFromData(((float[][]) data)[0], bitPix);
			ySeries = getSeriesFromData(((float[][]) data)[1], bitPix);
			break;
		case BasicHDU.BITPIX_INT:
			xSeries = getSeriesFromData(((int[][]) data)[0], bitPix);
			ySeries = getSeriesFromData(((int[][]) data)[1], bitPix);
			break;
		case BasicHDU.BITPIX_SHORT:
			xSeries = getSeriesFromData(((short[][]) data)[0], bitPix);
			ySeries = getSeriesFromData(((short[][]) data)[1], bitPix);
			break;
		case BasicHDU.BITPIX_LONG:
			xSeries = getSeriesFromData(((long[][]) data)[0], bitPix);
			ySeries = getSeriesFromData(((long[][]) data)[1], bitPix);
			break;
		case BasicHDU.BITPIX_BYTE:
			xSeries = getSeriesFromData(((byte[][]) data)[0], bitPix);
			ySeries = getSeriesFromData(((byte[][]) data)[1], bitPix);
			break;
		default:
			throw new SpefoException("Data is not of a valid value type.");
		}
	}

	@Override
	public String[] getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	@Override
	public boolean exportToAscii(String fileName) {
		if (Message.question(
				"By saving a FITS file to an ASCII file you lose the header information. Do you want to dump the header into a separate file?")) {
			String headerFile = FileUtils.stripFileExtension(fileName) + ".header";
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
			writer.println(getName());

			for (int i = 0; i < getSize(); i++) {
				writer.print(MathUtils.formatDouble(getX(i), 4, 4));
				writer.print("  ");
				writer.println(MathUtils.formatDouble(getY(i), 1, 4));
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

	private static final Set<String> ignoredKeys = new HashSet<>(Arrays.asList(new String[] { "", "END", "BITPIX",
			"NAXIS", "NAXIS1", "EXTEND", "CRPIX1", "CRVAL1", "CDELT1", "BZERO", "BSCALE", "SIMPLE" }));

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
				fits.getHDU(0).addValue("SIMPLE", true, "Created by reSpefo v" + Version.toFullString() + " on "
						+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
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

	public double getExpTime() {
		return header.getBigDecimalValue("EXPTIME").doubleValue();
	}

	public String getLstDate() {
		if (date.equals(LocalDateTime.MIN)) {
			return "0000 00 00 00 00 00";
		} else {
			return date.format(DateTimeFormatter.ofPattern("yyyy MM dd HH mm ss"));
		}
	}

	public RvCorrection getRvCorrection() {
		double rvCorr = header.getDoubleValue("VHELIO", Double.NaN);
		if (!Double.isNaN(rvCorr)) {
			return new RvCorrection(header.containsKey("BJD") ? RvCorrection.BARYCENTRIC : RvCorrection.HELIOCENTRIC,
					rvCorr);
		}

		return null;
	}
	
	public void applyRvCorrection(double rvCorr) throws SpefoException {
		xSeries = Arrays.stream(xSeries).map(value -> value + rvCorr*(value / MathUtils.SPEED_OF_LIGHT)).toArray();
		
		try {
			header.addValue("VHELIO", rvCorr, "Heliocentric correction");
		} catch (HeaderCardException exception) {
			throw new SpefoException(exception.getMessage());
		}
	}

	public double getJulianDate() {
		return header.getDoubleValue("JD", Double.NaN);
	}

	private void parseDate() {
		String dateValue = header.getStringValue(Standard.DATE_OBS);
		if (parseDateTime(dateValue)) {
			return;
		}

		String timeValue = header.getStringValue("UT");
		if (parseDateAndTime(dateValue, timeValue)) {
			return;
		}

		long tmStart = (long) header.getDoubleValue("TM-START", 0);
		if (parseDateAndTmStart(dateValue, tmStart)) {
			return;
		}

		date = LocalDateTime.MIN;
	}

	private boolean parseDateTime(String dateTimeValue) {
		try {
			date = LocalDateTime.parse(dateTimeValue);
			return true;

		} catch (Exception exception) {
			return false;
		}
	}

	private boolean parseDateAndTime(String dateValue, String timeValue) {
		try {
			LocalDate localDate = LocalDate.parse(dateValue);
			LocalTime localTime = LocalTime.parse(timeValue);
			date = localDate.atTime(localTime);

			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	private boolean parseDateAndTmStart(String dateValue, long tmStart) {
		try {
			LocalDate localDate = LocalDate.parse(dateValue);
			LocalTime localTime = LocalTime.ofSecondOfDay(tmStart);
			date = localDate.atTime(localTime);

			return true;
		} catch (Exception exception) {
			return false;
		}
	}
}
