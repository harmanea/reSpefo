package cz.cuni.mff.respefo.spectrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.IntStream;

import cz.cuni.mff.respefo.Version;
import cz.cuni.mff.respefo.util.ArrayUtils;
import cz.cuni.mff.respefo.util.FileUtils;
import cz.cuni.mff.respefo.util.MathUtils;
import cz.cuni.mff.respefo.util.SpefoException;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;

public class OldSpefoSpectrum extends Spectrum {
	private static final String[] FILE_EXTENSIONS = { "uui", "rui", "rci", "rfi" };

	private static final int HEADER_SIZE_IN_BYTES = 400;
	private static final char SQUARECHAR = 254;

	private String remark;
	private String usedCal;
	private short starStep;
	private double[] dispCoef;
	private double minTransp;
	private double maxInt;
	private double[] filterWidth;
	private int reserve;
	private short rectNum;
	private int[] rectX;
	private short[] rectY;
	private double rvCorr;

	public OldSpefoSpectrum(String fileName) throws SpefoException {
		super(fileName);

		try {
			byte[] data = Files.readAllBytes(Paths.get(fileName));
			processHeader(data);
			processBody(data);

			if (FileUtils.getFileExtension(fileName).equals("uui")) {
				String conFileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".con";
				File conFile = new File(conFileName);

				if (conFile.exists()) {
					readConFile(conFile);
				}
			}

			// apply rectification
			if (rectNum > 0) {
				double[] continuum = MathUtils.intep(Arrays.stream(rectX).asDoubleStream().toArray(),
						IntStream.range(0, rectY.length).mapToDouble(index -> rectY[index]).toArray(), xSeries);
				ySeries = ArrayUtils.divideArrayValues(ySeries, continuum);
			} else if (FileUtils.getFileExtension(fileName).equals("rui")) {
				ySeries = Arrays.stream(ySeries).map(value -> value / maxInt).toArray();
			}

			// calculate x-values using Taylor polynomials
			xSeries = Arrays.stream(xSeries).map(index -> MathUtils.indexToLambda(index, dispCoef)).toArray();

		} catch (IOException exception) {
			LOGGER.log(Level.WARNING, "Error while reading file", exception);
			throw new SpefoException("IOException occurred!");
		} catch (Exception exception) {
			LOGGER.log(Level.WARNING, "Error while reading file", exception);
			throw new SpefoException("General error while reading file.");
		}
	}

	private void processHeader(byte[] data) throws SpefoException {
		if (data.length < HEADER_SIZE_IN_BYTES) {
			throw new SpefoException("Header is too short");
		}

		byte[] bytes = Arrays.copyOfRange(data, 0, 30);
		remark = new String(bytes);
		remark = remark.replaceAll("\00", "").trim();

		bytes = Arrays.copyOfRange(data, 30, 38);
		usedCal = new String(bytes);
		usedCal = usedCal.replaceAll("\00", "");

		bytes = Arrays.copyOfRange(data, 38, 40);
		starStep = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

		dispCoef = new double[7];
		for (int i = 0; i < 7; i++) {
			bytes = Arrays.copyOfRange(data, 40 + 10 * i, 50 + 10 * i);
			dispCoef[i] = MathUtils.pascalExtendedToDouble(bytes);
			if (Double.isInfinite(dispCoef[i])) {
				dispCoef[i] = 0;
			}
		}
		rvCorr = (dispCoef[6] - 1) * MathUtils.SPEED_OF_LIGHT;

		bytes = Arrays.copyOfRange(data, 110, 120);
		minTransp = MathUtils.pascalExtendedToDouble(bytes);

		bytes = Arrays.copyOfRange(data, 120, 130);
		maxInt = MathUtils.pascalExtendedToDouble(bytes);

		filterWidth = new double[4];
		for (int i = 0; i < 4; i++) {
			bytes = Arrays.copyOfRange(data, 130 + 6 * i, 136 + 6 * i);
			filterWidth[i] = MathUtils.pascalRealToDouble(bytes);
		}

		bytes = Arrays.copyOfRange(data, 154, 158);
		reserve = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

		bytes = Arrays.copyOfRange(data, 158, 160);
		rectNum = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
		if (rectNum < 0) {
			rectNum = (short) -rectNum;
		}

		rectX = new int[rectNum];
		for (int i = 0; i < rectNum; i++) {
			bytes = Arrays.copyOfRange(data, 160 + 4 * i, 164 + 4 * i);
			rectX[i] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
		}

		rectY = new short[rectNum];
		for (int i = 0; i < rectNum; i++) {
			bytes = Arrays.copyOfRange(data, 320 + 2 * i, 322 + 2 * i);
			rectY[i] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
		}
	}

	private void processBody(byte[] data) {
		ArrayList<Double> yList = new ArrayList<>();
		for (int i = HEADER_SIZE_IN_BYTES; i < data.length; i += 2) {
			byte[] bytes = Arrays.copyOfRange(data, i, i + 2);
			short num = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

			yList.add((double) num);
		}
		ySeries = yList.stream().mapToDouble(Double::doubleValue).toArray();
		xSeries = ArrayUtils.fillArray(ySeries.length, 0, 1);
	}

	private void readConFile(File conFile) throws Exception {
		byte[] conData = Files.readAllBytes(conFile.toPath());
		if (conData.length < 400) {
			throw new SpefoException(".con file is too short");
		}

		char firstChar = (char) (((char) conData[1]) & 0x00FF);
		boolean extended = firstChar == SQUARECHAR;
		LOGGER.log(Level.FINE, "Extended: " + extended);

		byte[] bytes;
		int maxPoints, offset;
		if (extended) {
			bytes = Arrays.copyOfRange(conData, 2, 4);
			maxPoints = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
			offset = 16;
		} else {
			maxPoints = 100;
			offset = 0;
		}

		bytes = Arrays.copyOfRange(conData, 0 + offset, 30 + offset);
		String conRemark = new String(bytes);
		remark = conRemark.replaceAll("\00", "");

		bytes = Arrays.copyOfRange(conData, 30 + offset, 32 + offset);
		rectNum = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();

		rectX = new int[rectNum];
		rectY = new short[rectNum];

		for (int i = 0; i < rectNum; ++i) {
			bytes = Arrays.copyOfRange(conData, 32 + offset + 4 * i, 36 + offset + 4 * i);
			rectX[i] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

			bytes = Arrays.copyOfRange(conData, (extended ? maxPoints * 4 + 48 : 432) + 2 * i,
					(extended ? maxPoints * 4 + 52 : 434) + 2 * i);
			rectY[i] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
		}
	}

	@Override
	public String[] getFileExtensions() {
		return FILE_EXTENSIONS;
	}

	@Override
	public boolean exportToAscii(String fileName) {
		try (PrintWriter writer = new PrintWriter(fileName)) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			writer.println(remark);

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

		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "Error while writing to file", e);
			return false;
		}
	}

	@Override
	public boolean exportToFits(String fileName) {
		try (Fits fits = new Fits(); BufferedFile bf = new BufferedFile(fileName, "rw")) {
			
			BasicHDU<?> hdu;
			if (ArrayUtils.valuesHaveSameDifference(xSeries)) {
				hdu = FitsFactory.hduFactory(getYSeries());
				
				hdu.addValue("CRPIX1", 1, "Reference pixel");
				hdu.addValue("CRVAL1", getX(0), "Coordinate at reference pixel");
				hdu.addValue("CDELT1", getX(1) - getX(0), "Coordinate increment");
			} else {
				hdu = FitsFactory.hduFactory(new double[][] { xSeries, ySeries });
			}
			
			fits.addHDU(hdu);
			try {
				fits.getHDU(0).addValue("SIMPLE", true, "Created by reSpefo " + Version.toFullString() + " on "
						+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
			} catch (IOException exception) {
				LOGGER.log(Level.FINEST, "Couldn't change the SIMPLE value", exception);
			}

			fits.write(bf);

			return true;
		} catch (FitsException | IOException exception) {
			LOGGER.log(Level.WARNING, "Error while writing to file", exception);
			return false;
		}
	}

	public String getRemark() {
		return remark;
	}

	public String getUsedCal() {
		return usedCal;
	}

	public short getStarStep() {
		return starStep;
	}

	public double[] getDispCoef() {
		return dispCoef;
	}

	public double getMinTransp() {
		return minTransp;
	}

	public double getMaxInt() {
		return maxInt;
	}

	public double[] getFilterWidth() {
		return filterWidth;
	}

	public int getReserve() {
		return reserve;
	}

	public short getRectNum() {
		return rectNum;
	}

	public int[] getRectX() {
		return rectX;
	}

	public short[] getRectY() {
		return rectY;
	}

	public double getRvCorr() {
		return rvCorr;
	}
}
