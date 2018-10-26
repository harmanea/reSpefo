package cz.cuni.mff.respefo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;

public class AsciiSpectrum extends Spectrum {
	private static final String[] FILE_EXTENSIONS = {"txt", "ascii", ""};

	public AsciiSpectrum(String fileName) throws SpefoException {
		super(fileName);
		LOGGER.log(Level.FINEST, "Creating a new AsciiSpectrum (" + name + ")");
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			
			String line;
			String[] tokens;
			
			ArrayList<Double> xList = new ArrayList<>();
			ArrayList<Double> yList = new ArrayList<>();
			double x, y;
			
			while ((line = br.readLine()) != null) {
				tokens = line.trim().replaceAll(" +", " ").split(" ");
				
				try {
					x = Double.valueOf(tokens[0]);
					y = Double.valueOf(tokens[1]);
					
					xList.add(x);
					yList.add(y);
				} catch (NumberFormatException e) {
					LOGGER.log(Level.FINEST, "Skipped line (" + line + ")");
					continue;
				}
			}

			xSeries = xList.stream().mapToDouble(Double::doubleValue).toArray();
			ySeries = yList.stream().mapToDouble(Double::doubleValue).toArray();
			
			LOGGER.log(Level.FINER, "Closing file (" + xSeries.length + " lines loaded)");
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Error while reading file", e);
			// TODO specialized exception?
			throw new SpefoException("IOException occurred!");
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

	@Override
	public boolean exportToFits(String fileName) {
		double[] data = getYSeries();

		BasicHDU<?> hdu;
		try (Fits f = new Fits(); BufferedFile bf = new BufferedFile(fileName, "rw")) {
			LOGGER.log(Level.FINER, "Opened a file (" + fileName + ")");
			hdu = FitsFactory.hduFactory(data);
			
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
