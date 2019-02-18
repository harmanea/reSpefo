package cz.cuni.mff.respefo;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;

/**
 * A base class for all formats.
 */
public abstract class Spectrum {
	protected String name;
	
	protected double[] xSeries;
	protected double[] ySeries;
	
	protected String xLabel;
	protected String yLabel;
	
	protected static final Logger LOGGER = Logger.getLogger(ReSpefo.class.getName());
	
	protected Spectrum(String fileName) {
		name = Paths.get(fileName).getFileName().toString();
		int pos = name.lastIndexOf('.');
		if (pos > 0 && pos < name.length() - 1) {
			name = name.substring(0, pos);
		}
		
		xLabel = "xAxis";
		yLabel = "yAxis";
	}
	
	protected Spectrum(String fileName, double[] xSeries, double[] ySeries) {
		this(fileName);
		this.xSeries = xSeries;
		this.ySeries = ySeries;
	}
	
	/**
	 * Returns the name of the spectrum.
	 * 
	 * This should correspond to the original filename.
	 * @return name of the spectrum
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the file extensions associated with the class.
	 * 
	 * @return array of file extensions
	 */
	public abstract String[] getFileExtensions();

	/**
	 * Returns the number of points that the spectrum holds.
	 * 
	 * Should be zero for an empty spectrum.
	 * @return length of the spectrum
	 */
	public int getSize() {
		return xSeries.length;
	}

	/**
	 * Returns the x value at the specified index.
	 * 
	 * Throws an exception if the index is smaller than zero or greater than the spectrum's size.
	 * @param index location of the x value
	 * @return x value at the specified index
	 * @throws IndexOutOfBoundsException if the index is smaller than zero or greater than the spectrum's size
	 */
	public double getX(int index) throws IndexOutOfBoundsException {
		if (index >= 0 && index < xSeries.length) {
			return xSeries[index];
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Returns the y value at the specified index.
	 * 
	 * Throws an exception if the index is smaller than zero or greater than the spectrum's size.
	 * @param index location of the y value
	 * @return y value at the specified index
	 * @throws IndexOutOfBoundsException if the index is smaller than zero or greater than the spectrum's size
	 */
	public double getY(int index) throws IndexOutOfBoundsException {
		if (index >= 0 && index < ySeries.length) {
			return ySeries[index];
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Returns the x series.
	 * 
	 * @return the x series
	 */
	public double[] getXSeries() {
		return xSeries.clone();
	}

	/**
	 * Returns the x series values that are between the specified arguments.
	 * 
	 * @param from lower bound
	 * @param to upper bound
	 * @return the trimmed x series
	 */
	public double[] getTrimmedXSeries(double from, double to) {
		int lowerIndex = Arrays.binarySearch(xSeries, from);
		int upperIndex = Arrays.binarySearch(xSeries, to);
		
		if (lowerIndex < 0) {
			lowerIndex *= -1;
			lowerIndex -= 1;
		} else if (lowerIndex >= xSeries.length) {
			lowerIndex = xSeries.length - 1;
		}
		
		if (upperIndex < 0) {
			upperIndex *= -1;
			upperIndex -= 1;
		} else if (upperIndex >= xSeries.length) {
			upperIndex = xSeries.length - 1;
		}
		
		return Arrays.copyOfRange(xSeries, lowerIndex, upperIndex);
	}
	
	/**
	 * Sets the spectrum x values.
	 * @param xSeries new x values
	 */
	public void setXSeries(double[] xSeries) {
		this.xSeries = xSeries;
	}

	/**
	 * Returns the y series.
	 * 
	 * @return the y series
	 */
	public double[] getYSeries() {
		return ySeries.clone();
	}

	/**
	 * Returns the y series values of the points whose x values are between the specified arguments.
	 * @param from lower bound
	 * @param to upper bound
	 * @return the trimmed y series
	 */
	public double[] getTrimmedYSeries(double from, double to) {
		int lowerIndex = Arrays.binarySearch(xSeries, from);
		int upperIndex = Arrays.binarySearch(xSeries, to);
		
		if (lowerIndex < 0) {
			lowerIndex *= -1;
			lowerIndex -= 1;
		} else if (lowerIndex >= ySeries.length) {
			lowerIndex = ySeries.length - 1;
		}
		
		if (upperIndex < 0) {
			upperIndex *= -1;
			upperIndex -= 1;
		} else if (upperIndex >= ySeries.length) {
			upperIndex = ySeries.length - 1;
		}
		
		return Arrays.copyOfRange(ySeries, lowerIndex, upperIndex);
	}
	
	/**
	 * Sets the spectrum y values.
	 * @param ySeries new y values
	 */
	public void setYSeries(double[] ySeries) {
		this.ySeries = ySeries;
	}
	
	/**
	 * @return the xLabel
	 */
	public String getxLabel() {
		return xLabel;
	}

	/**
	 * @param xLabel the xLabel to set
	 */
	public void setxLabel(String xLabel) {
		this.xLabel = xLabel;
	}

	/**
	 * @return the yLabel
	 */
	public String getyLabel() {
		return yLabel;
	}

	/**
	 * @param yLabel the yLabel to set
	 */
	public void setyLabel(String yLabel) {
		this.yLabel = yLabel;
	}

	/**
	 * Exports and saves the spectrum to a ASCII file.
	 * @param fileName the name of the new file
	 * @return True if export was successful, False if it failed
	 */
	public abstract boolean exportToAscii(String fileName);
	
	/**
	 * Exports and saves the spectrum to a FITS file.
	 * @param fileName the name of the new file
	 * @return True if export was successful, False if it failed
	 */
	public abstract boolean exportToFits(String fileName);

	/**
	 * Returns a new Spectrum instance imported from the specified file.
	 * 
	 * This method creates an instance of a derived class based on the file extension.
	 * @param fileName the name of the spectrum file
	 * @return the imported spectrum
	 * @throws SpefoException if an error occurred while loading file
	 */
	public static final Spectrum createFromFile(String fileName) throws SpefoException {
		String extension = Util.getFileExtension(fileName);
		
		Spectrum result = null;
		
		// TODO specialized exceptions?
		switch (extension) {
		case "":
		case "txt":
		case "ascii":
			result = new AsciiSpectrum(fileName);
			break;
		case "fits":
		case "fit":
		case "fts":
			try {
				result = new FitsSpectrum(fileName);
			} catch (FitsException e) {
				MessageBox mb = new MessageBox(ReSpefo.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				mb.setMessage("File is broken. Would you like to try to repair it?");
				if (mb.open() == SWT.YES) {
					try {
						FitsFactory.setAllowHeaderRepairs(true);
						result = new FitsSpectrum(fileName);
					} catch(FitsException f) {
						throw new SpefoException("Couldn't repair file.");
					} finally {
						FitsFactory.setAllowHeaderRepairs(false);
					}
				} else {
					throw new SpefoException("File is broken.");
				}
			}
			break;
		case "uui":
		case "rui":
		case "rci":
		case "rfi":
			result = new OldSpefoSpectrum(fileName);
			break;
		default:
			LOGGER.log(Level.WARNING, "Not a valid file extension");
			throw new SpefoException("Not a valid file extension");
		}
		
		return result;
	}
}
