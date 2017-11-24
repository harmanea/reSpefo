package cz.cuni.mff.respefo;

public class Spectrum {
	private Point[] data;

	private double max_Y, min_Y;

	/**
	 * 
	 * @param data array representing the Spectrum data
	 */
	public Spectrum(Point[] data) {
		this.data = data;
		double max_Y = Double.MIN_VALUE;
		double min_Y = Double.MAX_VALUE;
		double Y;
		for (Point p : this.data) {
			Y = p.getY();
			if (Y > max_Y) {
				max_Y = Y;
			} else if (Y < min_Y) {
				min_Y = Y;
			}
		}
		this.max_Y = max_Y;
		this.min_Y = min_Y;
	}
	
	/**
	 * Gets the number of points in the spectrum
	 * 
	 * @return {@code int} size of the spectrum
	 */
	public int size() {
		return data.length;
	}

	/**
	 * Gets the highest X value of any Point
	 * 
	 * @return {@code double} maximum X value of all points
	 */
	public double getMaxX() {
		return data[data.length - 1].getX();
	}

	/**
	 * Gets the lowest X value of any Point
	 * 
	 * @return {@code double} minimum X value of all points
	 */
	public double getMinX() {
		return data[0].getX();
	}

	/**
	 * Gets the highest Y value of any Point
	 * 
	 * @return {@code double} maximum Y value of all points
	 */
	public double getMaxY() {
		return max_Y;
	}

	/**
	 * Gets the lowest Y value of any Point
	 * 
	 * @return {@code double} minimum Y value of all points
	 */
	public double getMinY() {
		return min_Y;
	}

	/**
	 * Gets the X value of the Point at specified index.
	 * <p>
	 * Note: Throws {@code IndexOutOfBoundsException} if the specified index is lower than 0 or larger than the number of points in the spectrum
	 * 
	 * @param at index of the Point
	 * @return {@code double} X value of the Point
	 * @throws IndexOutOfBoundsException
	 */
	public double getX(int at) throws IndexOutOfBoundsException {
		if ((at >= 0) && (at < data.length)) {
			return data[at].getX();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Gets the Y value of the Point at specified index.
	 * <p>
	 * Note: Throws {@code IndexOutOfBoundsException} if the specified index is lower than 0 or larger than the number of points in the spectrum
	 * 
	 * @param at index of the Point
	 * @return {@code double} Y value of the Point
	 * @throws IndexOutOfBoundsException
	 */
	public double getY(int at) throws IndexOutOfBoundsException {
		if ((at >= 0) && (at < data.length)) {
			return data[at].getY();
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	/**
	 * Gets the array {@code Point[]} representing the spectrum data.
	 * 
	 * @return array {@code Point[]} representing the spectrum data
	 */
	public Point[] getData() {
		return data;
	}
}
