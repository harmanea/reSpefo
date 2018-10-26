package cz.cuni.mff.respefo.legacy;

import java.util.Arrays;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Header;

/**
 * Class representing a spectrum file
 *
 */
@Deprecated
public class OldSpectrum {
	private double[] XSeries;
	private double[] YSeries;

	private double max_X, min_X;
	private double max_Y, min_Y;
	
	private String name;
	
	private Type type;
	
	private Header header;

	public enum Type {
		FITS, ASCII, RUI, UUI
	}

	/**
	 * 
	 * @param XSeries {@code double[]} array representing the XSeries
	 * @param YSeries {@code double[]} array representing the YSeries
	 * @param name {@code String} name of the Spectrum
	 */
	public OldSpectrum(double[] XSeries, double[] YSeries, String name) {
		this.XSeries = XSeries;
		this.YSeries = YSeries;
		
		double max_Y = Double.MIN_VALUE;
		double min_Y = Double.MAX_VALUE;
		
		double max_X = Double.MIN_VALUE;
		double min_X = Double.MAX_VALUE;
		
		for (int i = 0; i < YSeries.length; i++) {
			if (XSeries[i] > max_X) {
				max_X = XSeries[i];
			} else if (XSeries[i] < min_X) {
				min_X = XSeries[i];
			}
			if (YSeries[i] > max_Y) {
				max_Y = YSeries[i];
			} else if (YSeries[i] < min_Y) {
				min_Y = YSeries[i];
			}
		}
		
		this.max_Y = max_Y;
		this.min_Y = min_Y;
		
		this.max_X = max_X;
		this.min_X = min_X;
		
		this.name = name;
	}
	
	/**
	 * Gets the length of the series
	 * 
	 * @return {@code int} size of the spectrum
	 */
	public int size() {
		return YSeries.length;
	}
	
	
	/**
	 * Gets the name of the spectrum
	 * 
	 * @return {@code String} name of the spectrum
	 */
	public String name() {
		return name;
	}
	
	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Gets the highest X value in the series
	 * 
	 * @return {@code double} maximum X value
	 */
	public double getMaxX() {
		return max_X;
	}

	/**
	 * Gets the lowest X value in the series
	 * 
	 * @return {@code double} minimum X value
	 */
	public double getMinX() {
		return min_X;
	}

	/**
	 * Gets the highest Y value in the series
	 * 
	 * @return {@code double} maximum Y value
	 */
	public double getMaxY() {
		return max_Y;
	}

	/**
	 * Gets the lowest Y value in the series
	 * 
	 * @return {@code double} minimum Y value
	 */
	public double getMinY() {
		return min_Y;
	}

	/**
	 * Gets the X value at the specified index.
	 * <p>
	 * Note: Throws {@code IndexOutOfBoundsException} if the specified index is lower than 0 or larger than the number of points in the spectrum
	 * 
	 * @param at index of the point
	 * @return {@code double} X value
	 * @throws IndexOutOfBoundsException
	 */
	public double getX(int at) throws IndexOutOfBoundsException {
		if ((at >= 0) && (at < XSeries.length)) {
			return XSeries[at];
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Gets the Y value at the specified index.
	 * <p>
	 * Note: Throws {@code IndexOutOfBoundsException} if the specified index is lower than 0 or larger than the number of points in the spectrum
	 * 
	 * @param at index of the point
	 * @return {@code double} Y value
	 * @throws IndexOutOfBoundsException
	 */
	public double getY(int at) throws IndexOutOfBoundsException {
		if ((at >= 0) && (at < YSeries.length)) {
			return YSeries[at];
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * Gets the array {@code double[]} representing the XSeries.
	 * 
	 * @return array {@code Point[]} representing the XSeries
	 */
	public double[] getXSeries() {
		return XSeries;
	}
	
	/**
	 * Gets the XSeries trimmed by the specified arguments
	 * 
	 * @param from {@code double} lower bound value
	 * @param to {@code double} upper bound value
	 * @return {@code double[]} representing the trimmed XSeries
	 */
	public double[] getTrimmedXSeries(double from, double to) {
		int lo_index = Arrays.binarySearch(XSeries, from);
		int hi_index = Arrays.binarySearch(XSeries, to);
		
		if (lo_index < 0) {
			lo_index *= -1;
			lo_index -= 1;
		} else if (lo_index >= XSeries.length) {
			lo_index = XSeries.length - 1;
		}
		
		if (hi_index < 0) {
			hi_index *= -1;
			hi_index -= 1;
		} else if (hi_index >= XSeries.length) {
			hi_index = XSeries.length - 1;
		}
		
		return Arrays.copyOfRange(XSeries, lo_index, hi_index);
	}
	
	/**
	 * Gets the array {@code double[]} representing the YSeries.
	 * 
	 * @return array {@code Point[]} representing the YSeries
	 */
	public double[] getYSeries() {
		return YSeries;
	}
	
	/**
	 * Gets the YSeries trimmed by the specified arguments respective to the XSeries
	 * 
	 * @param from {@code double} lower bound value for the XSeries
	 * @param to {@code double} upper bound value for the XSeries
	 * @return {@code double[]} representing the trimmed YSeries respective to the XSeries
	 */
	public double[] getTrimmedYSeries(double from, double to) {
		int lo_index = Arrays.binarySearch(XSeries, from);
		int hi_index = Arrays.binarySearch(XSeries, to);
		
		if (lo_index < 0) {
			lo_index *= -1;
			lo_index -= 2;
		} else if (lo_index >= XSeries.length) {
			lo_index = XSeries.length - 1;
		}
		
		if (hi_index < 0) {
			hi_index *= -1;
			hi_index -= 2;
		} else if (hi_index >= XSeries.length) {
			hi_index = XSeries.length - 1;
		}
		
		return Arrays.copyOfRange(YSeries, lo_index, hi_index);
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}
}
