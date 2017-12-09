package cz.cuni.mff.respefo;

public class Spectrum {
	private double[] XSeries;
	private double[] YSeries;

	private double max_Y, min_Y;
	
	private String name;

	/**
	 * 
	 * @param XSeries {@code double[]} array representing the XSeries
	 * @param YSeries {@code double[]} array representing the YSeries
	 * @param name {@code String} name of the Spectrum
	 */
	public Spectrum(double[] XSeries, double[] YSeries, String name) {
		this.XSeries = XSeries;
		this.YSeries = YSeries;
		double max_Y = Double.MIN_VALUE;
		double min_Y = Double.MAX_VALUE;
		for (int i = 0; i < YSeries.length; i++) {
			if (YSeries[i] > max_Y) {
				max_Y = YSeries[i];
			} else if (YSeries[i] < min_Y) {
				min_Y = YSeries[i];
			}
		}
		this.max_Y = max_Y;
		this.min_Y = min_Y;
		
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
	 * Gets the highest X value in the series
	 * 
	 * @return {@code double} maximum X value
	 */
	public double getMaxX() {
		return XSeries[XSeries.length - 1];
	}

	/**
	 * Gets the lowest X value in the series
	 * 
	 * @return {@code double} minimum X value
	 */
	public double getMinX() {
		return XSeries[0];
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
	 * Gets the array {@code double[]} representing the YSeries.
	 * 
	 * @return array {@code Point[]} representing the YSeries
	 */
	public double[] getYSeries() {
		return YSeries;
	}
}
